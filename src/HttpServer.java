import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class HttpServer
{
    /**
    private int port;
    private String rootDir;
    private String indexFile;
    private boolean index;
    private ArrayList<InetAddress> accept;
    private ArrayList<InetAddress> reject;
    */
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        int port = args.length == 1 ? Integer.parseInt(args[0]) : 80;

        Scanner sc = new Scanner(System.in);
        System.out.println("Application :");
        System.out.println("1 - Lecture requete http");
        System.out.println("2 - Envoyer une reponse");
        System.out.println("3 - page HTML");

        int choix = sc.nextInt();
        switch (choix) {
            case 1:
                lireRequete(port);
                break;
            case 2:
                envoyerReponse(port);
                break;
            case 3:
                requeteHtml("ressource/config.xml");
                break;
            default:
                System.out.println("pas un choix disponible");
                return;

        }

    }

    public static void lireRequete(int port) throws IOException {
        final ServerSocket server = new ServerSocket(port);
        System.out.println("Listening for connection on port  " + port + " ....");
        final Socket clientSocket = server.accept();
        while (true) {

            InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
            while (!line.isEmpty()) {
                System.out.println(line);
                line = reader.readLine();
            }
        }

    }

    private static void envoyerReponse(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        System.out.println("Listening for connection on port  " + port + "  ....");
        while (true) {
            try (Socket socket = server.accept()) {
                Date today = new Date();
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
                socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
                socket.close();
            }
        }

    }

    private static void requeteHtml(String nomfichier) throws IOException, ParserConfigurationException, SAXException {
        //On crée une instance de lecture XML qui va lire le fichier XML:
        LectureXML lectureXml = new LectureXML(nomfichier);
        //paramétrage du port par rapport au fichier xml:
        final ServerSocket server = new ServerSocket(lectureXml.getPort());
        System.out.println("Lecture de la connection au port: " + lectureXml.getPort() + " ....");


        while (true) {
            try (Socket socket = server.accept()) {
                // C'EST ICI QUE CA TE TEST:
                //On récupére l'adresse ip machine de la personne qui se connecte:
                InetAddress adresse =  socket.getInetAddress();
                //on récupere le masque:
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(adresse);
                int masque = networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength();
                //on récupère l'adresse reseau de la personne:
                InetAddress adresseReseau = ipReseau(adresse.getHostAddress(), masque);
                //On récupe l'adresse ip a rejetée:
                ArrayList<InetAddress> rejectedAdresse = (ArrayList<InetAddress>) lectureXml.getReject();
                //192.168.56.0: ip reseau anas.
                //on vérifie que l'adresse ip n'est pas l'adresse qu'on rejette:

                if(!rejectedAdresse.contains(adresseReseau))
                {
                    //on lit ce que la requete du server:
                    InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                    BufferedReader reader = new BufferedReader(isr);
                    String line = reader.readLine();

                    //on paramètre le fichier de base par rapport au fichier xml :
                    String configXml = lectureXml.getRoot()/*+lectureXml.getIndexFile()*/;

                    //on lit la requête:
                    System.out.println(line);
                    if (line.contains("GET") && !line.equals("GET / HTTP/1.1")) {
                        //on paramètre par rapport au root lu dans le fichier XML:
                        configXml =  lectureXml.getRoot()+line.substring(5, line.length() - 9);
                    }


                    if (!configXml.equals("web/favicon.ico")) {
                        //System.out.println(nomFichier);
                        File f = new File(configXml);
                        //on vérifie si le nom du fichier n'existe pas
                        if(!f.exists())
                        {
                            //si il n'existe pas on ouvre une page d'erreur
                            f = new File("web/error/error.html");
                        }
                        //on vérifie si le fichier est un repertoire et que l'index est true:
                        if(f.isDirectory() && lectureXml.isIndex()){
                            String affichage = afficherArborescence(f,nomfichier);
                            String httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
                            socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
                            socket.getOutputStream().write(affichage.getBytes("UTF-8"));
                        }
                        //sinon on vérifie que le fichier est un répertoire:
                        else{
                            if(f.isDirectory() && !lectureXml.isIndex())
                            {

                                f = new File("web/error/error502.html");
                            }

                            byte[] b = null;

                            b = Files.readAllBytes(f.toPath());

                            String httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
                            socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
                            socket.getOutputStream().write(b);
                        }
                    }

                }
                //si c'est l'adresse que l'on rejette:
                else
                {

                    File f = new File("web/error/error502.html");
                    byte[] b = null;

                    b = Files.readAllBytes(f.toPath());

                    String httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
                    socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
                    socket.getOutputStream().write(b);



                }
            }
        }
        }




    /**
     * méthode qui permet de transformer une adresse ip machine en adresse ip réseau
     * @param ip l'adresse ip machine
     * @param prefmasque le masque
     * @return ip reseau
     */
    public static InetAddress ipReseau(String ip, int prefmasque)
    {
        //pour savoir le masque :
        //System.out.println("le masque : " + prefmasque);
        //convertir le masque entier en un tableau de 32bits
        int masque = 0xffffffff << (32 - prefmasque);
        int valeur = masque;
        byte[] bytes_masque = new byte[]{
                (byte)(valeur >>> 24), (byte)(valeur >> 16 & 0xff), (byte)(valeur >> 8 & 0xff), (byte)(valeur & 0xff) };

        try
        {
            //Convertir l'adresse IP en long
            long ipl = ipToLong(ip);

            //Convertir l'IP en un tableau de 32bits
            byte[] bytes_ip = new byte[]{
                    (byte) ((ipl >> 24) & 0xFF),
                    (byte) ((ipl >> 16) & 0xFF),
                    (byte) ((ipl >> 8 ) & 0xFF),
                    (byte) (ipl & 0xFF)};

            //Le ET logique entre l'adresse IP et le masque
            byte[] bytes_reseau = new byte[]{
                    (byte) (bytes_ip[0] & bytes_masque[0]),
                    (byte) (bytes_ip[1] & bytes_masque[1]),
                    (byte) (bytes_ip[2] & bytes_masque[2]),
                    (byte) (bytes_ip[3] & bytes_masque[3]),
            };
            //adresse réseau obtenue
            InetAddress adr_reseau = InetAddress.getByAddress(bytes_reseau);
            //pour savoir l'adresse reseau:
            //System.out.println("Adresse réseau =\t"+adr_reseau.getHostAddress());
            return adr_reseau;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * méthode qui transforme une ip en un long
     * @param ipAddress ip a transformé
     * @return retourne le long
     */
    public static long ipToLong(String ipAddress) {
        long result = 0;
        String[] ipAddressInArray = ipAddress.split("\\.");

        for (int i = 3; i >= 0; i--) {
            long ip = Long.parseLong(ipAddressInArray[3 - i]);
            result |= ip << (i * 8);
        }
        return result;
    }

    /**
    * methode afficherArborescence qui affiche l'arborescence a partir du dossier courant de la requete
    * @param chemin Chemin du dossier dont on veut afficher l'arborescence
    * @return une String contenant le code HTML de la page d'arborescence
    */
    public static String afficherArborescence(File chemin, String xmlfichier) throws ParserConfigurationException, SAXException {

        LectureXML lectureXML = new LectureXML(xmlfichier);
        String repertoireSup = chemin.getPath().replaceFirst(lectureXML.getRoot().substring(0, lectureXML.getRoot().length()-1),"")+"\\..";
        StringBuilder arborescence = new StringBuilder("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n</head>\n<body>\n<h1>Arborescence de "+chemin.getPath()+"</h1>\n<ul>\n<li><a href=\""+repertoireSup+"\"><img src=\"images/dossier.png\" width=\"20\">..</a></li><br>\n");


        if(lectureXML.getRoot().equals(chemin.getPath()+"/")) arborescence = new StringBuilder("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n</head>\n<body>\n<h1>Arborescence de "+chemin.getPath()+"\\</h1>\n<ul><br>\n");
        File[] files = chemin.listFiles();
        for(File file : files){
            String path = file.getPath().replaceFirst(lectureXML.getRoot().substring(0, lectureXML.getRoot().length()-1),"");
            if(file.isDirectory()) arborescence.append("<li><a href=\""+path+"\"><img src=\"images/dossier.png\" width=\"20\">"+file.getName()+"</a></li><br>\n");
            else arborescence.append("<li><a href=\""+path+"\"><img src=\"images/fichier.png\" width=\"20\">"+file.getName()+"</a></li><br>\n");
        }
        arborescence.append("</ul>\n</body>\n</html>");
        return arborescence.toString();
    }

}

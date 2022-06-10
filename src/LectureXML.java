import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class LectureXML
{
    //attribut que l'on va récupérer dans le fichier XML
    private int port;
    private String root;
    private String indexFile;
    private boolean index;
    private ArrayList<InetAddress> reject;

    public LectureXML(String nomFichier) throws ParserConfigurationException, SAXException{
        /**this.port = 80;
        this.root = "/";
        this.index = false;
        this.reject = null;*/
            try
            {
                // On cree une instance de File qui prend en parametre le nom duf fichier de configuration XML
                File file = new File(nomFichier);


                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(file);

                document.getDocumentElement().normalize();

                // on lit le contenu de la balise port
                try {
                    this.port = Integer.parseInt(document.getElementsByTagName("port").item(0).getTextContent());
                }
                // si elle est vide on instancie port a 80
                catch (NumberFormatException e){
                    this.port = 80;
                }

                // on lit le contenu de la balise <root>
                this.root = document.getElementsByTagName("root").item(0).getTextContent();
                // si elle est vide le root est le dossier web
                if(this.root.equals("")) this.root = "web/";

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
    }

    public int getPort(){
        return this.port;
    }


    public String getRoot(){
        return this.root;
    }


    public static void main(String[] args) {
        try {
            LectureXML lx = new LectureXML("ressource/config.xml");
            System.out.println(lx.getPort());
            System.out.println(lx.getRoot());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}


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
import java.util.List;

// rajouter arraylist pour PLUSIEURS IP REJETÉE
public class LectureXML {
    //attribut que l'on va récupérer dans le fichier XML
    private int port;
    private String root;
    private String indexFile;
    private boolean index;
    //pour l'instant une seule adresse ip que l'on peut rejeter.
    private List<InetAddress> reject;

    public LectureXML(String nomFichier) throws ParserConfigurationException, SAXException {
        try {
            // On crée une instance de File qui prend en paramètre le nom duf fichier de configuration XML
            File file = new File(nomFichier);

            //On crée une instance de document à partir du document builder et du nom du fichier
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);

            //on récupère les documents présents dans notre fichier XML
            document.getDocumentElement().normalize();

            // on lit le contenu de la balise port
            try {
                this.port = Integer.parseInt(document.getElementsByTagName("port").item(0).getTextContent());
            }
            // si elle est vide on instancie port a 80
            catch (NumberFormatException e) {
                this.port = 80;
            }

            // on lit le contenu de la balise <root>
            this.root = document.getElementsByTagName("root").item(0).getTextContent();

            // si elle est vide le root est le dossier web
            if (this.root.equals("")) this.root = "web/";

            //on lit l'index file
            this.indexFile = document.getElementsByTagName("indexFile").item(0).getTextContent();

            //s'il est vide :
            if (this.indexFile.equals("")) this.indexFile = "index.html";

            //on lit l'index (si rien n'est présent le met automatiquement à false) :
            this.index = Boolean.parseBoolean(document.getElementsByTagName("index").item(0).getTextContent());

            try {
                NodeList node = document.getElementsByTagName("reject");
                this.reject = new ArrayList<>(node.getLength());
                for(int i = 0; i<node.getLength(); i++)
                {
                    this.reject.add(i,InetAddress.getByName(node.item(i).getTextContent()));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getter du port
     *
     * @return le port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * getter du root
     *
     * @return le root
     */
    public String getRoot() {
        return this.root;
    }

    /**
     * getter du indexfile
     *
     * @return le indexfile
     */
    public String getIndexFile() {
        return indexFile;
    }

    /**
     * getter du index
     *
     * @return boolean
     */
    public boolean isIndex() {
        return index;
    }

    /**
     * Getter de l'arrayList
     * @return
     */
    public List<InetAddress> getReject()
    {
        return reject;
    }
}
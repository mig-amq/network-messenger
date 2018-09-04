package messenger;

import messenger.objects.GlobalHolder;
import messenger.objects.Room;
import messenger.objects.SerializedObject;
import messenger.objects.types.ObjectType;
import messenger.objects.types.RoomType;
import messenger.sockets.ClientHandler;
import messenger.sockets.Server;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public final class Model {

    private static Model instance = null;
    private Server server;
    private ClientHandler client;

    private String clientPassword, clientUsername, clientAddress;
    private int clientPort, serverPort;

    private GlobalHolder holder;

    public static Model getInstance() {
        if (instance == null) {
            synchronized (Model.class) {
                if (instance == null)
                    instance = new Model();
            }
        }

        return instance;
    }

    private Model() {
        this.setHolder(new GlobalHolder());
        this.getHolder().getRooms().add(new Room("Global Room", RoomType.REGULAR));
        this.loadSettings();
        this.createFileLoc();
    }

    public void createFileLoc() {
        File f = new File("files/");
        File cf = new File("client_files/");

        if (!f.exists()) {
            try {
                Files.createDirectory(f.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!cf.exists()) {
            try {
                Files.createDirectory(cf.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void loadSettings () {
        File f = new File("settings.xml");

        if (!f.exists()) {
            try {
                Files.copy(getClass().getResourceAsStream("resources/default_settings.xml"), f.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(f);
            doc.normalizeDocument();
            NodeList settingsList = doc.getElementsByTagName("settings");

            for(int i = 0; i < settingsList.getLength(); ++i) {
                Node rootNode = settingsList.item(i);
                if (rootNode.getNodeType() == 1) {
                    Element elem = (Element)rootNode;
                    Element client = (Element)elem.getElementsByTagName("client").item(0);
                    Element server = (Element)elem.getElementsByTagName("server").item(0);

                    this.setClientUsername(elem.getElementsByTagName("username").item(0).getTextContent());
                    this.setClientAddress(client.getElementsByTagName("address").item(0).getTextContent());
                    this.setClientPassword(client.getElementsByTagName("password").item(0).getTextContent());
                    this.setClientPort(Integer.valueOf(client.getElementsByTagName("port").item(0).getTextContent()));

                    this.getHolder().setPassword(server.getElementsByTagName("password").item(0).getTextContent());
                    this.setServerPort(Integer.valueOf(client.getElementsByTagName("port").item(0).getTextContent()));

                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSettings (String type) {
        type = type.toLowerCase();

        File f = new File("settings.xml");

        if (!f.exists()) {
            try {
                Files.copy(getClass().getResourceAsStream("resources/default_settings.xml"), f.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(f);

            doc.normalizeDocument();
            NodeList settingsList = doc.getElementsByTagName("settings");

            for(int i = 0; i < settingsList.getLength(); ++i) {
                Node rootNode = settingsList.item(i);
                if (rootNode.getNodeType() == 1) {
                    Element elem = (Element)rootNode;
                    Element client = (Element)elem.getElementsByTagName("client").item(0);
                    Element server = (Element)elem.getElementsByTagName("server").item(0);

                    if (type.equals("client") || type.equals("all")) {
                        elem.getElementsByTagName("username").item(0).setTextContent(this.getClientUsername());
                        client.getElementsByTagName("address").item(0).setTextContent(this.getClientAddress());
                        client.getElementsByTagName("password").item(0).setTextContent(this.getClientPassword());
                        client.getElementsByTagName("port").item(0).setTextContent(String.valueOf(this.getClientPort()));
                    }

                    if (type.equals("server") || type.equals("all")){
                        server.getElementsByTagName("port").item(0).setTextContent(String.valueOf(this.getServerPort()));
                        server.getElementsByTagName("password").item(0).setTextContent(this.getHolder().getPassword());
                    }

                }
            }

            doc.normalizeDocument();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(f);
            transformer.transform(source, result);


        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public Server getServer() {
        return server;
    }

    public ClientHandler getClient() {
        return client;
    }

    public int getClientPort() {
        return clientPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public GlobalHolder getHolder() {
        return holder;
    }

    public void setClient(ClientHandler client) {
        this.client = client;
    }

    public void setServer(Server server) {
        this.server = server;
        this.server.setModel(this);
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setHolder(GlobalHolder holder) {
        this.holder = holder;
    }
}

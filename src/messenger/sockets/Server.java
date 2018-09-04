package messenger.sockets;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import messenger.Model;
import messenger.application.Landing;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Server {
    private Model model;
    private boolean started;
    private ServerSocket server;
    private Thread clientAccepter;
    private ArrayList<ServerHandler> handlers;

    private Landing landing;

    public Server(int serverPort) {
        this.setServer(null);
        this.setStarted(false);
        this.setClientAccepter(null);
        this.setHandlers(new ArrayList<>());
    }

    public Server(int port, Landing landing) {
        this(port);

        this.landing = landing;

        try {
            this.setServer(new ServerSocket(port));
            this.setStarted(true);

            this.landing.addLog("*** Server started @ port " + port + " ***\n");
        } catch (IOException e) {
            this.landing.addLog("*** Server cannot be started @ port " + port + " ***\n");
            this.setStarted(false);
            this.landing.shutServer();
        }
    }

    public void accept () {
        if (this.getServer() != null && !this.getServer().isClosed()) {
            this.setClientAccepter(new Thread(() -> {
                this.landing.addLog("Accepting sockets @ port " + this.getServer().getLocalPort() + "\n");

                while (this.isStarted() && !this.getServer().isClosed()) {
                    try {
                        if (this.isStarted()) {
                            Socket client = this.getServer().accept();

                            synchronized (this.getHandlers()) {
                               this.getHandlers().add(new ServerHandler(this, client, this.getModel()));

                               this.getHandlers().get(this.getHandlers().size() - 1).start();
                            }

                            this.landing.addLog("A client logged in...\n");
                       }
                    } catch (IOException e) {
                        this.landing.addLog("Did not receive a client...\n");
                    }
                }
            }));

            this.getClientAccepter().start();
        }
    }

    public void off () {
        try {
            this.setStarted(false);

            for (ServerHandler handler : this.getHandlers())
                handler.off();

            this.getHandlers().clear();

            this.landing.addLog("]\n*** Server Shutdown ***\n");
            this.getServer().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ServerHandler> getHandlers() {
        return handlers;
    }

    public Thread getClientAccepter() {
        return clientAccepter;
    }

    public ServerSocket getServer() {
        return server;
    }

    public boolean isStarted() {
        return started;
    }

    public Model getModel() {
        return model;
    }

    public void setServer(ServerSocket server) {
        this.server = server;
    }

    public void setHandlers(ArrayList<ServerHandler> handlers) {
        this.handlers = handlers;
    }

    public void setClientAccepter(Thread clientAccepter) {
        this.clientAccepter = clientAccepter;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setModel(Model model) {
        this.model = model;
    }
}

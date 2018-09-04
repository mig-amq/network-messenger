package messenger.sockets;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import messenger.objects.*;
import messenger.objects.types.CommandType;
import messenger.objects.types.DataType;
import messenger.objects.types.ObjectType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class ClientHandler extends Thread {
    private Socket client;
    private boolean started;
    private final LinkedList<SocketData> outgoing, incoming;
    private boolean hasOutgoing, hasIncoming;

    public ClientHandler (){
        outgoing = new LinkedList<>();
        incoming = new LinkedList<>();

        hasOutgoing = false;
        hasIncoming = false;
    }

    public ClientHandler (Socket socket) {
        this();
        this.setClient(socket);
        this.setStarted(true);
    }

    public void addMessage (SocketData data) {
        synchronized (outgoing) {
            hasOutgoing = true;
            outgoing.push(data);
        }
    }

    @Override
    public void run () {
        try {
            SocketData data;

            ObjectOutputStream out = new ObjectOutputStream(this.getClient().getOutputStream());
            ObjectInputStream ois;

            while (this.isStarted() && this.getClient() != null && !this.getClient().isClosed()) {

                if (this.getClient().getInputStream().available() > 0) {
                    ois = new ObjectInputStream(this.getClient().getInputStream());

                    if ((data = (SocketData) ois.readObject()) != null) {
                        synchronized (incoming) {
                            incoming.push(data);
                            hasIncoming = !incoming.isEmpty();

                            // Uncomment for debugging
                            /*System.out.println();
                            System.out.println("Receiving Data from Server: ");
                            System.out.println("SocketData Type: " + data.getType());
                            System.out.println("Data Object Type: " + data.getData().getObjectType());

                            if (data.getType() == DataType.COMMAND) {
                                System.out.println(" Command Type: " + data.getCommand());
                                if (data.getData().getObjectType() == ObjectType.HOLDER) {
                                    System.out.println(" Data: ");

                                    System.out.println(" Users: ");
                                    for (String user : ((GlobalHolder) data.getData()).getUsers())
                                        System.out.println(" > " + user);

                                    System.out.println(" Rooms: ");
                                    for (Room room : ((GlobalHolder) data.getData()).getRooms()) {
                                        System.out.println("  > " + room);
                                        System.out.println("    > Users");
                                        for(String user : room.getUsers())
                                            System.out.println("    - " + user);
                                    }
                                }
                            } else {
                                System.out.print("sender=" + ((Message) data.getData()).getSender());
                                System.out.print("receiver=" + ((Message) data.getData()).getReceiver());
                                System.out.print("message=" + ((Message) data.getData()).getMessage());
                                System.out.println("room=" + ((Message) data.getData()).getRoom().getName());
                            }

                            System.out.println("Incoming Data: " + this.getIncoming().size());
                            System.out.println();*/
                        }
                    }
                }
                if (hasOutgoing) {

                    synchronized (outgoing) {
                        data = outgoing.pop();
                        hasOutgoing = !outgoing.isEmpty();
                    }

                    if (data.getType() == DataType.MESSAGE) {
                        System.out.println("Sending to Server: ");
                        System.out.print("sender=" + ((Message) data.getData()).getSender());
                        System.out.print("receiver=" + ((Message) data.getData()).getReceiver());
                        System.out.print("message=" + ((Message) data.getData()).getMessage());
                        System.out.println("room=" + ((Message) data.getData()).getRoom().getName());
                    }

                    if (!this.getClient().isClosed() && this.getClient().isConnected()) {
                        out.reset();
                        out.writeObject(data);
                        out.flush();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void off () {
        try {

            if (this.getClient() != null) {
                this.setStarted(false);
                this.getClient().close();
            }

            this.setClient(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    private boolean hasOutgoing () {
        return hasOutgoing;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public LinkedList<SocketData> getIncoming() {
        return incoming;
    }

    public LinkedList<SocketData> getOutgoing() {
        return outgoing;
    }

    public void setHasMessage(boolean hasMessage) {
        this.hasOutgoing = hasMessage;
    }

    public void setHasIncoming(boolean hasIncoming) {
        this.hasIncoming = hasIncoming;
    }

    public boolean hasIncoming() {
        return hasIncoming;
    }
}

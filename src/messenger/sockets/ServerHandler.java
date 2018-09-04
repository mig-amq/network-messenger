package messenger.sockets;

import messenger.Model;
import messenger.objects.*;
import messenger.objects.File;
import messenger.objects.types.CommandType;
import messenger.objects.types.DataType;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class ServerHandler extends Thread {
    private Model model;
    private Server server;
    private Socket client;
    private boolean started;
    private String clientName;

    public ServerHandler(Server server, Socket client, Model model) {
        this.setServer(server);
        this.setClient(client);
        this.setStarted(true);
        this.setModel(model);
    }

    @Override
    public void run() {
        try {
            SocketData data;
            ObjectInputStream ois =
                    ois = new ObjectInputStream(this.getClient().getInputStream());

            while (this.isStarted() && this.getServer().isStarted() && !this.getClient().isClosed()) {
                if (this.getClient().getInputStream().available() > 0) {

                    if ((data = (SocketData) ois.readObject()) != null) {
                        System.out.println();
                        System.out.println("Receiving Data from Client: ");
                        System.out.println("SocketData Type: " + data.getType());
                        System.out.println("Data Object Type: " + data.getData().getObjectType());
                        if (data.getType() == DataType.MESSAGE) {
                            this.sendData(data);

                            System.out.print("sender=" + ((Message) data.getData()).getSender());
                            System.out.print("receiver=" + ((Message) data.getData()).getReceiver());
                            System.out.print("message=" + ((Message) data.getData()).getMessage());
                            System.out.println("room=" + ((Message) data.getData()).getRoom().getName());
                        } else if (data.getType() == DataType.COMMAND) {
                            CommandType command = data.getCommand();
                            System.out.println("Command: " + data.getCommand());
                            switch (command) {
                                case ADD_USER:
                                    this.setClientName(data.getSender());
                                    synchronized (this.getModel().getHolder()) {
                                        this.getModel().getHolder().getUsers().add(this.getClientName());
                                        data.setCommand(CommandType.GET_HOLDER);
                                        data.setData(this.getModel().getHolder());
                                        this.sendData(data);
                                    }
                                    break;
                                case REMOVE_USER:
                                    synchronized (this.getModel().getHolder()) {
                                        this.getModel().getHolder().removeUser(data.getSender());
                                        this.getServer().getHandlers().remove(this);

                                        data.setData(this.getModel().getHolder());
                                        this.sendData(data  );
                                    }

                                    if (this.getClientName().equals(data.getSender()))
                                        this.off();
                                    break;
                                case GET_HOLDER:
                                    synchronized (this.getModel().getHolder()){
                                        data.setCommand(CommandType.GET_HOLDER);
                                        data.setData(this.getModel().getHolder());
                                        this.sendData(data);
                                    }
                                    break;
                                case UPDATE_HOLDER:
                                    synchronized (this.getModel().getHolder()) {
                                        this.getModel().setHolder((GlobalHolder) data.getData());
                                        data.setCommand(CommandType.GET_HOLDER);
                                        data.setData(this.getModel().getHolder());
                                        this.sendData(data);
                                    }
                                    break;
                                case FILE_SEND:
                                    this.upload((File) data.getData());
                                    SocketData dTemp = new SocketData();

                                    synchronized (this.getModel().getHolder()) {
                                        dTemp.setCommand(CommandType.GET_HOLDER);
                                        dTemp.setData(this.getModel().getHolder());
                                        this.sendData(dTemp);
                                        System.out.println("UPDATE FILE");
                                    }
                                    break;
                                case DOWNLOAD:
                                    this.download(data);
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void download (SocketData data) {
        File d = (File) data.getData();

        synchronized (this.getModel().getHolder()) {
            java.io.File f = new java.io.File("files/" + d.getFileName() + "." + d.getExtension());

            if (f.exists() && f.isFile()) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    int pos = 0;

                    ((File) data.getData()).setBytes(new byte[(int) f.length()]);
                    try (BufferedInputStream in = new BufferedInputStream(fis)) {
                        byte[] buff = new byte[(f.length() >= 8192) ? 8192 : (int) f.length()];
                        int len;
                        while ((len = in.read(buff)) != -1) {
                            for (int i = 0; i < len; i++) {
                                ((File) data.getData()).getBytes()[i + 8192 * pos] = buff[i];
                            }

                            pos++;
                        }

                        this.sendData(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void upload(File data) {
        synchronized (this.getModel().getHolder()) {
            java.io.File f = new java.io.File("files/" + data.getFileName() + "." + data.getExtension());
            long num = 0;

            do {
                if (f.exists()) {
                    f = new java.io.File("files/" + data.getFileName().concat("_" + String.valueOf(num)) + "." + data.getExtension());
                    num++;
                }
            } while (f.exists());

            data.setFileLocation("files/");

            if (num > 0)
                data.setFileName(data.getFileName().concat("_" + String.valueOf(--num)));

            try {
                Files.createFile(f.toPath());
                FileChannel channel = new RandomAccessFile(f, "rw").getChannel();
                MappedByteBuffer mb = channel.map(FileChannel.MapMode.READ_WRITE, 0, data.getBytes().length);

                mb.put(data.getBytes());

                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            data.setBytes(null);
            for (Room r : this.getModel().getHolder().getRooms())
                if (r.getId().equals(data.getRoom().getId())) {
                    r.getFiles().add(data);
                } else {
                    for (Room g : r.getGroups())
                        if (g.getId().equals(data.getRoom().getId()))
                            g.getFiles().add(data);
                }

            System.out.println("Done Uploading");
        }
    }

    private void sendData (SocketData data) {
        this.setStarted(false);

        boolean isRemove = data.getCommand().equals(CommandType.REMOVE_USER);
        if (isRemove)
            data.setCommand(CommandType.GET_HOLDER);

        for (ServerHandler handler : this.getServer().getHandlers()) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(handler.getClient().getOutputStream());

                out.reset();
                out.writeObject(data);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (isRemove)
            this.off();

        this.setStarted(true);
    }

    public void off () {
        try {
            this.getClient().close();
            this.setStarted(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    public boolean isStarted() {
        return started;
    }

    public String getClientName() {
        return clientName;
    }

    public Server getServer() {
        return server;
    }

    public Socket getClient() {
        return client;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }
}

package messenger.application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import messenger.Model;
import messenger.objects.*;
import messenger.objects.types.CommandType;
import messenger.objects.types.DataType;
import messenger.objects.types.RoomType;
import messenger.sockets.ClientHandler;

import java.awt.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client extends WindowObject{
    private Thread clientListener;
    private boolean stopClientListener;
    private boolean loggedIn;

    @FXML
    private ListView<Room> lvRooms;

    @FXML
    private ListView<String> lvUsers;

    @FXML
    private Label lblWelcome;

    @FXML
    private Button btnJoin, btnCreate;

    private final ArrayList<Chat> chats = new ArrayList<>();

    @Override
    public void off() {
        if (!stopClientListener && this.getModel().getClient() != null && this.getModel().getClient().getClient() != null && !this.getModel().getClient().getClient().isClosed()) {

            if (loggedIn) {
                synchronized (this.getModel().getHolder()) {
                    SocketData socketData = new SocketData(this.getModel());
                    socketData.setSender(this.getModel().getClientUsername());
                    socketData.setCommand(CommandType.REMOVE_USER);

                    if (this.getModel().getClient() != null)
                        this.getModel().getClient().addMessage(socketData);

                    for (Chat c : chats)
                        c.off();

                    chats.clear();
                    stopClientListener = true;
                }
            }
        }

        this.getModel().getHolder().getUsers().clear();
        this.getModel().getHolder().getRooms().clear();

        if (this.getModel().getClient() != null)
            this.getModel().getClient().setStarted(false);

        this.getStage().close();
    }

    @Override
    public void init (Stage stage, Model model) {
        this.initObjects(stage, model);
        loggedIn = false;
        lblWelcome.setText(lblWelcome.getText() + " " + model.getClientUsername());

        try {
            this.getModel().setClient(new ClientHandler(new Socket(model.getClientAddress(), model.getClientPort())));
            this.getModel().getClient().start();

            initListener();
            clientListener.start();

            Message msg = new Message("GET_HOLDER", this.getModel().getClientUsername(), "SERVER");
            SocketData initData = new SocketData(msg, CommandType.GET_HOLDER);
            this.getModel().getClient().addMessage(initData);

            lvRooms.setOnMousePressed(event -> {
                if (event.getClickCount() == 2) {
                    btnJoin.fire();
                }
            });

            btnCreate.setOnMouseClicked(event -> {
                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.setTitle("New Room");
                dialog.setHeaderText("Make a new room!");

                ButtonType buttons = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(buttons, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                TextField room = new TextField();
                room.setPromptText("Room Name...");
                PasswordField password = new PasswordField();
                password.setPromptText("Room Key...");

                grid.add(new Label("Room Name:"), 0, 0);
                grid.add(room, 1, 0);
                grid.add(new Label("Room Key:"), 0, 1);
                grid.add(password, 1, 1);

                Node loginButton = dialog.getDialogPane().lookupButton(buttons);
                loginButton.setDisable(true);

                room.textProperty().addListener((observable, oldValue, newValue) -> {
                    loginButton.setDisable(newValue.trim().isEmpty());
                });

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == buttons)
                        return new Pair<>(room.getText(), password.getText());

                    return null;
                });

                dialog.getDialogPane().setContent(grid);
                Optional<Pair<String, String>> result = dialog.showAndWait();

                result.ifPresent(data -> {
                    Room r = new Room(data.getKey(), data.getValue());
                    r.setType(RoomType.REGULAR);

                    synchronized (this.getModel().getHolder()) {
                        this.getModel().getHolder().getRooms().add(r);
                        SocketData socketData = new SocketData(this.getModel());
                        socketData.setCommand(CommandType.UPDATE_HOLDER);
                        this.getModel().getClient().addMessage(socketData);
                    }
                });
            });

            btnJoin.setOnAction(event -> {
                if (!lvRooms.getSelectionModel().isEmpty()) {
                    AtomicBoolean allow = new AtomicBoolean(false);
                    String password = lvRooms.getSelectionModel().getSelectedItem().getPassword();

                    if (password.equals(""))
                        allow.set(true);
                    else {
                        Dialog<String> dialog = new Dialog<>();
                        dialog.setHeaderText("This room is locked!");
                        dialog.setContentText("Room Key: ");
                        dialog.setTitle("Locked Room");
                        ButtonType buttons = new ButtonType("Join", ButtonBar.ButtonData.OK_DONE);
                        dialog.getDialogPane().getButtonTypes().addAll(buttons, ButtonType.CANCEL);

                        GridPane grid = new GridPane();
                        grid.setHgap(10);
                        grid.setVgap(10);
                        grid.setPadding(new Insets(20, 150, 10, 10));

                        PasswordField passwordField = new PasswordField();
                        passwordField.setPromptText("Room Key...");

                        grid.add(new Label("Room Key:"), 0, 0);
                        grid.add(passwordField, 1, 0);

                        Node loginButton = dialog.getDialogPane().lookupButton(buttons);
                        loginButton.setDisable(true);

                        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
                            loginButton.setDisable(newValue.trim().isEmpty());
                        });

                        dialog.setResultConverter(dialogButton -> {
                            if (dialogButton == buttons) {
                                return passwordField.getText();
                            }
                            return null;
                        });

                        dialog.getDialogPane().setContent(grid);

                        Optional<String> result = dialog.showAndWait();
                        result.ifPresent(data -> {
                            if (data.equals(password))
                                allow.set(true);
                        });
                    }

                    if (allow.get()) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Chat.fxml"));

                        for (Room r : this.getModel().getHolder().getRooms()) {
                            if (lvRooms.getSelectionModel().getSelectedItem().getId().equals(r.getId())) {
                                r.getUsers().add(this.getModel().getClientUsername());
                            }
                        }

                        try {
                            Parent chatParent = loader.load();
                            Stage chatStage = new Stage(StageStyle.UNDECORATED);
                            chatStage.setScene(new Scene(chatParent));
                            chatStage.show();

                            loader.<Chat>getController().init(chatStage, this.getModel(), lvRooms.getSelectionModel().getSelectedItem());
                            chats.add(loader.getController());

                            synchronized (this.getModel().getHolder()) {
                                SocketData data = new SocketData(this.getModel().getClientUsername(), this.getModel().getHolder());
                                this.getModel().getClient().addMessage(data);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        this.showAlert("Oops! Wrong password", Alert.AlertType.INFORMATION);
                    }
                }
            });

            stopClientListener = false;
        } catch (IOException e) {
            this.showAlert("Error: Cannot connect to server.", Alert.AlertType.ERROR);
            this.off();
        }
    }

    public void initListener () {
        clientListener = new Thread (() -> {
            Message msg;
            SocketData data;
            boolean exists = false;
            long num;

            while (this.getModel().getClient() != null && this.getModel().getClient().isStarted() && !stopClientListener) {
                data = null;

                synchronized (this.getModel().getClient().getIncoming()) {
                    if (this.getModel().getClient().hasIncoming()) {
                        data = this.getModel().getClient().getIncoming().pop();
                        this.getModel().getClient().setHasIncoming(!this.getModel().getClient().getIncoming().isEmpty());

                        if (data != null) {
                            if (data.getType() == DataType.COMMAND) {
                                switch (data.getCommand()) {
                                    case DOWNLOAD:
                                        if (data.getSender().equals(this.getModel().getClientUsername())) {
                                            num = 0;
                                            java.io.File f = new java.io.File("client_files/" + ((File) data.getData()).getFileName() + "." + ((File) data.getData()).getExtension());

                                            do {
                                                if (f.exists()) {
                                                    f = new java.io.File("client_files/" + ((File) data.getData()).getFileName() + "_" +
                                                            String.valueOf(num) + "." + ((File) data.getData()).getExtension());
                                                    num++;
                                                }
                                            } while (f.exists());

                                            try {
                                                Files.createFile(f.toPath());
                                                FileChannel channel = new RandomAccessFile(f, "rw").getChannel();
                                                MappedByteBuffer mb = channel.map(FileChannel.MapMode.READ_WRITE, 0, ((File) data.getData()).getBytes().length);

                                                mb.put(((File) data.getData()).getBytes());

                                                channel.close();
                                                Desktop.getDesktop().open(f.getParentFile());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        }

                                        break;
                                    case GET_HOLDER:

                                        synchronized (this.getModel().getHolder().getRooms()) {
                                            this.getModel().getHolder().getRooms().clear();
                                            this.getModel().getHolder().getRooms().addAll(((GlobalHolder) data.getData()).getRooms());
                                        }

                                        synchronized (this.getModel().getHolder().getUsers()) {
                                            this.getModel().getHolder().getUsers().clear();
                                            this.getModel().getHolder().getUsers().addAll(((GlobalHolder) data.getData()).getUsers());
                                        }

                                        for (String user : this.getModel().getHolder().getUsers())
                                            if (user.equals(this.getModel().getClientUsername()))
                                                exists = true;

                                        if (!loggedIn && exists) {
                                            Platform.runLater(() -> {
                                                this.showAlert("Info: You username is already in use.", Alert.AlertType.INFORMATION);
                                                this.off();
                                            });
                                        } else if (!loggedIn) {

                                            System.out.println(this.getModel().getHolder().getPassword() + " " + ((GlobalHolder) data.getData()).getPassword());

                                            if (!this.getModel().getClientPassword().equals(((GlobalHolder) data.getData()).getPassword())) {
                                                Platform.runLater(() -> {
                                                    this.showAlert("Info: Incorrect server password.", Alert.AlertType.INFORMATION);
                                                    this.off();
                                                });
                                            } else {
                                                this.getModel().getHolder().setPassword(((GlobalHolder) data.getData()).getPassword());
                                            }

                                            loggedIn = true;

                                            data = new SocketData();
                                            data.setCommand(CommandType.ADD_USER);
                                            data.setSender(this.getModel().getClientUsername());
                                            this.getModel().getClient().addMessage(data);
                                        }

                                        if (loggedIn) {
                                           synchronized (this.getModel().getHolder()) {
                                               for (Room r : this.getModel().getHolder().getRooms()) {
                                                   for (Chat c : chats) {
                                                       if (c.getRoom().getId().equals(r.getId())) {
                                                           c.getRoom().setFiles(r.getFiles());
                                                           c.getRoom().setUsers(r.getUsers());
                                                           c.getRoom().setGroups(r.getGroups());

                                                           c.addMessage(new Message("UPDATE_CHAT", "Server", "all"));
                                                       }
                                                   }
                                               }
                                           }
                                        }

                                        this.updateView();
                                        break;
                                }
                            } else {
                                synchronized (this.getModel().getHolder()) {
                                    for (Chat c : chats) {
                                        if (((Message) data.getData()).getRoom().getType() == RoomType.GROUP ||
                                                ((Message) data.getData()).getRoom().getType() == RoomType.PERSONAL){
                                            if (c.getRoom().getId().equals(((Message) data.getData()).getRoom().getParent().getId())) {
                                                c.addMessage((Message) data.getData());
                                            }
                                        } else {
                                            if (c.getRoom().getId().equals(((Message) data.getData()).getRoom().getId())) {
                                                c.addMessage(((Message) data.getData()));
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }

                if (stopClientListener) {
                    this.getModel().setClient(null);
                }
            }
        });
    }

    private void updateView() {
        Platform.runLater(() -> {
            lvRooms.getItems().clear();
            lvUsers.getItems().clear();

            synchronized (lvRooms.getItems()) {
                for (Room r : this.getModel().getHolder().getRooms())
                    if (r.getType() != RoomType.PERSONAL && r.getType() != RoomType.GROUP)
                        lvRooms.getItems().add(r);
            }

            synchronized (lvUsers.getItems()) {
                lvUsers.getItems().addAll(this.getModel().getHolder().getUsers());
            }
        });
    }
}
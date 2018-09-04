package messenger.application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import messenger.Model;
import messenger.objects.Room;
import messenger.objects.types.RoomType;
import messenger.sockets.Server;

import java.io.IOException;

public class Landing extends WindowObject{

    private Thread serverListener;
    private boolean stopServerListener;
    private Client client;

    @FXML
    private MenuItem miSaveAll, miSaveServer, miSaveClient;

    @FXML
    private Button btnJoin, btnServer;

    @FXML
    private TextField txtUsername, txtAddress, txtPort, txtSPort;

    @FXML
    private PasswordField txtPassword, txtSPassword;

    @FXML
    private TextArea taLog;

    @Override
    public void off() {
        getStage().close();

        if (this.getModel().getClient() != null) {
            this.client.off();
        }

        Platform.exit();
        System.exit(0);
    }

    @Override
    public void init (Stage stage, Model model) {
        this.initObjects(stage, model);

        stopServerListener = true;
        txtUsername.setText(model.getClientUsername());
        txtAddress.setText(model.getClientAddress());
        txtPassword.setText(model.getClientPassword());
        txtPort.setText(String.valueOf(model.getClientPort()));

        txtPassword.setText(model.getHolder().getPassword());
        txtSPort.setText(String.valueOf(model.getServerPort()));

        miSaveAll.setOnAction(event -> this.save("all"));
        miSaveClient.setOnAction(event -> this.save("client"));
        miSaveServer.setOnAction(event -> this.save("server"));

        btnServer.setOnMouseClicked(event -> {
            if (this.getModel().getServer() != null && this.getModel().getServer().isStarted()) { // close server
                shutServer();
            } else {
                openServer();
            }
        });

        btnJoin.setOnMouseClicked(event -> {
            if (this.getModel().getClient() == null ||  (this.getModel().getClient() != null && !this.getModel().getClient().isStarted())) {
                if (stopServerListener) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Client.fxml"));

                    try {

                        this.getModel().setClientAddress(txtAddress.getText());
                        this.getModel().setClientUsername(txtUsername.getText());
                        this.getModel().setClientPassword(txtPassword.getText());
                        this.getModel().setClientPort(Integer.valueOf(txtPort.getText()));

                        Parent childRoot = fxmlLoader.load();
                        Stage clientStage= new Stage(StageStyle.UNDECORATED);
                        clientStage.setScene(new Scene(childRoot));
                        clientStage.show();

                        fxmlLoader.<Client>getController().init(clientStage, this.getModel());
                        client = fxmlLoader.getController();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.showAlert("Info: You can only be a server or a client, but not both.", Alert.AlertType.INFORMATION);
                }
            } else {
                this.showAlert("Info: Only one client view can be loaded at a time.", Alert.AlertType.INFORMATION);
            }
        });
    }

    public void save(String type) {
        this.getModel().setClientUsername(txtUsername.getText());
        this.getModel().setClientPassword(txtPassword.getText());
        this.getModel().setClientAddress(txtAddress.getText());
        this.getModel().setClientPort(Integer.valueOf(txtPort.getText()));

        this.getModel().getHolder().setPassword(txtSPassword.getText());
        this.getModel().setServerPort(Integer.valueOf(txtSPort.getText()));

        this.getModel().saveSettings(type);
    }

    public void shutServer () {
        this.getModel().getServer().off();
        btnServer.getStyleClass().remove("btnClose");
        btnServer.setText("Start Server");

        if (serverListener != null && !stopServerListener) {
            stopServerListener = true;
            serverListener = null;
        }

        btnJoin.setDisable(false);
        btnServer.setDisable(false);
        this.getModel().getHolder().getRooms().clear();
        this.getModel().getHolder().getUsers().clear();
        this.getModel().getHolder().getRooms().add(new Room("Global Room", RoomType.REGULAR));
        taLog.appendText("*** Server Closed ***\n");
    }

    public void openServer () {
        this.getModel().getHolder().setPassword(txtSPassword.getText());
        this.getModel().setServerPort(Integer.valueOf(txtSPort.getText()));
        this.getModel().setServer(new Server(this.getModel().getServerPort(), this));
        this.getModel().getServer().accept();
        this.getModel().getHolder().getRooms().clear();
        this.getModel().getHolder().getRooms().add(new Room("Global Room"));
        btnJoin.setDisable(true);
        btnServer.setDisable(false);
        btnServer.getStyleClass().add("btnClose");
        btnServer.setText("Close Server");
    }

    public void addLog(String log) {
        Platform.runLater(() -> taLog.appendText(log));
    }

}

package messenger.application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import messenger.Model;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class WindowObject implements Initializable {

    private double x, y;

    @FXML
    private Button btnClose;

    @FXML
    private AnchorPane pnlMain, pnlTool;

    private Model model;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initObjects (Stage stage, Model model) {

        this.setStage(stage);
        this.setModel(model);

        pnlTool.setOnMousePressed(event -> {
            x = this.getStage().getX() - event.getScreenX();
            y = this.getStage().getY() - event.getScreenY();
        });

        pnlTool.setOnMouseDragged(event -> {
            this.getStage().setX(x + event.getScreenX());
            this.getStage().setY(y + event.getScreenY());
        });

        btnClose.setOnMouseClicked(event -> this.off());

        this.getStage().setOnHiding(event -> this.off());
    }

    public void showAlert (String str, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);

        alert.setTitle("NM - Alert");

        if (alertType == Alert.AlertType.ERROR)
            alert.setHeaderText("Uh oh! AN error occurred");
        else if (alertType == Alert.AlertType.INFORMATION)
            alert.setHeaderText("Notice!");

        alert.setContentText(str);
        alert.showAndWait();
    }

    public abstract void off();
    public abstract void init(Stage stage, Model model);

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public AnchorPane getPnlMain() {
        return pnlMain;
    }

    public void setPnlMain(AnchorPane pnlMain) {
        this.pnlMain = pnlMain;
    }

    public AnchorPane getPnlTool() {
        return pnlTool;
    }

    public void setPnlTool(AnchorPane pnlTool) {
        this.pnlTool = pnlTool;
    }

    public Button getBtnClose() {
        return btnClose;
    }

    public void setBtnClose(Button btnClose) {
        this.btnClose = btnClose;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}

package messenger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import messenger.application.Landing;

public class Main extends Application {

    private Model model;

    @Override
    public void start(Stage primaryStage) throws Exception{
        model = Model.getInstance();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("application/fxml/Landing.fxml"));

        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Messenger");
        primaryStage.setScene(new Scene(root));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

        fxmlLoader.<Landing>getController().init(primaryStage, model);

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }



    public static void main(String[] args) {
        launch(args);
    }
}

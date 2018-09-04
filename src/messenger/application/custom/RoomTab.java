package messenger.application.custom;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import messenger.objects.Message;
import messenger.objects.Room;

public class RoomTab extends Tab{
    private Room room;
    private boolean initialized = false;

    private ScrollPane spContent;
    private AnchorPane pnlMessages;

    public RoomTab () { super(); }
    public RoomTab (Room room) {
        this();
        this.bind(room);
    }

    private void init() {
        spContent = new ScrollPane();
        spContent.setFitToWidth(true);
        spContent.setFitToHeight(true);
        spContent.setPrefHeight(350);
        spContent.setPrefWidth(450);

        pnlMessages = new AnchorPane();
        spContent.setContent(pnlMessages);

        spContent.vvalueProperty().bind(pnlMessages.heightProperty());

        this.setContent(spContent);

        initialized = true;
    }

    public void appendMessage(String user, Message msg) {
        if (this.getRoom() != null && msg.getRoom().getId().equals(this.getRoom().getId()) && initialized) {
            TextFlow prev = null;
            double prefX = 15, prefY = 25;

            for (Node n : pnlMessages.getChildren())
                if (n instanceof TextFlow)
                    prev = (TextFlow) n;

            Label sender  = new Label(msg.getSender() + ":");
            sender.setLayoutX(prefX);

            if (prev != null)
                sender.setLayoutY(prev.getLayoutY() + 30);
            else
                sender.setLayoutY(prefY);

            sender.getStyleClass().add("user-label");

            TextFlow tflow = new TextFlow();
            tflow.setMaxWidth(pnlMessages.getWidth() - 25);
            tflow.setLayoutX(prefX + 15);

            if (prev != null)
                tflow.setLayoutY(prev.getLayoutY() + prev.getHeight() + 30);
            else
                tflow.setLayoutY(prefY + 20);


            if (user.equals(msg.getSender()))
                tflow.getStyleClass().add("user");
            else
                tflow.getStyleClass().add("others");

            Text message = new Text(msg.getMessage());
            tflow.getChildren().add(message);

            Platform.runLater(() -> {
                pnlMessages.getChildren().add(tflow);
                pnlMessages.getChildren().add(sender);

                if (tflow.getLayoutY() + tflow.getHeight() >= pnlMessages.getHeight() - 30) {
                    pnlMessages.setMinHeight(tflow.getLayoutY() + tflow.getHeight() + 35);
                }
            });
        }
    }

    public void bind(Room room) {
        this.setRoom(room);

        this.setText(room.getName());
        this.init();
    }

    public Room getRoom() {
        return room;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}

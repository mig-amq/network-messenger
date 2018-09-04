package messenger.objects;

import messenger.objects.types.ObjectType;

public class Message extends SerializedObject {

    private static final long serialVersionUID = -6596366753382007641L;

    private Room room;
    private Room parent;
    private String message, sender, receiver;

    public Message () {
        this.setObjectType(ObjectType.MESSAGE);
    }

    public Message (Room room) {
        this();
        this.setRoom(room);
    }

    public Message (String message, String sender, String receiver) {
        this();
        this.setSender(sender);
        this.setMessage(message);
        this.setReceiver(receiver);
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setMessage(String message) {
        this.message = message.replaceAll("\\n", "").replaceAll("\\s+", " ");
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setParent(Room parent) {
        this.parent = parent;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public Room getRoom() {
        return room;
    }

    public Room getParent() {
        return parent;
    }
}

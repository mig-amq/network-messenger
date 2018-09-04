package messenger.objects;

import messenger.Model;
import messenger.objects.types.CommandType;
import messenger.objects.types.DataType;
import messenger.objects.types.ObjectType;

public class SocketData extends SerializedObject{

    private static final long serialVersionUID = 1810427112293839763L;

    private String sender;
    private DataType type;
    private CommandType command;
    private SerializedObject data;

    public SocketData () {
        this.setData(new Message());
        this.setType(DataType.MESSAGE);
        this.setCommand(CommandType.NONE);
        this.setObjectType(ObjectType.DATA);
    }

    public SocketData (Message msg, DataType type) {
        this();
        this.setData(msg);
        this.setType(type);
        this.setSender(msg.getSender());

        if (type == DataType.MESSAGE)
            this.setCommand(CommandType.NONE);
        else
            this.setCommand(CommandType.UPDATE_HOLDER);
    }

    public SocketData (String sender, GlobalHolder holder) {
        this();
        this.setData(holder);
        this.setType(DataType.COMMAND);
        this.setSender(sender);
        this.setCommand(CommandType.UPDATE_HOLDER);
    }

    public SocketData (Model model) {
        this(model.getClientUsername(), model.getHolder());
    }

    public SocketData (String sender, Room room) {
        this();
        this.setData(room);
        this.setSender(sender);
        this.setType(DataType.COMMAND);
        this.setCommand(CommandType.GET_ROOM);
    }

    public SocketData (Message msg, CommandType type) {
        this(msg, DataType.COMMAND);
        this.setCommand(type);
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public void setCommand(CommandType command) {
        this.command = command;

        if (this.getCommand() != CommandType.NONE)
            this.setType(DataType.COMMAND);
    }

    public void setData(SerializedObject data) {
        this.data = data;
    }

    public void setData (Model model) {
        this.setData(model.getHolder());
        this.setSender(model.getClientUsername());
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public CommandType getCommand() {
        return command;
    }

    public DataType getType() {
        return type;
    }

    public SerializedObject getData() {
        return data;
    }

    public String getSender() {
        return sender;
    }
}

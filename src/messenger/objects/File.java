package messenger.objects;

import messenger.objects.types.ObjectType;

import java.util.UUID;

public class File extends SerializedObject{
    private static final long serialVersionUID = 1260892443476647756L;

    private Room room; // can be omitted
    private byte[] bytes;
    private String id;
    private String fileName;
    private String extension;
    private String fileLocation;

    public File () {
        this.setObjectType(ObjectType.FILE);
        this.generateID();
    }

    public File (String f) {
        this();
        java.io.File file = new java.io.File(f);

        if (file.exists()) {
            this.setFileName(file.getAbsoluteFile().getName());
            this.setExtension(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1));

        }
    }

    private void generateID() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtension() {
        return extension;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public Room getRoom() {
        return room;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        return this.fileName + "." + this.extension;
    }
}

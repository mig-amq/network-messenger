package messenger.objects;

import messenger.objects.types.ObjectType;
import messenger.objects.types.RoomType;

import java.util.ArrayList;
import java.util.UUID;

public class Room extends SerializedObject {

    private static final long serialVersionUID = -7823242248247203004L;

    private String id;
    private Room parent;
    private RoomType type;
    private String password, name;
    private ArrayList<String> users;
    private ArrayList<Room> groups;
    private ArrayList<File> files;

    public Room () {
        this.setType(RoomType.REGULAR);
        this.setUsers(new ArrayList<>());
        this.setFiles(new ArrayList<>());
        this.setGroups(new ArrayList<>());
        this.setObjectType(ObjectType.ROOM);
        this.generateID();
    }

    public Room (String name, String password) {
        this();
        this.setName(name);
        this.setPassword(password);
    }

    public Room (Room parent, String name) {
        this(name);
        this.setParent(parent);
        this.setType(RoomType.GROUP);
    }
    public Room (String name) {
        this(name, "");
    }

    public Room (String name, RoomType type) {
        this(name);
        this.setType(type);
    }

    public Room (String name, String password, RoomType type) {
        this(name, password);
        this.setType(type);
    }

    public boolean hasPassword () {
        if (this.getPassword().equals(""))
            return false;

        return true;
    }

    private void generateID() {
        this.id = UUID.randomUUID().toString();
    }

    public synchronized void removeUser (String user) {
        this.getUsers().remove(user);

        for (int i = 0; i < this.getGroups().size(); i++)
            if (this.getGroups().get(i).getType() == RoomType.PERSONAL &&
                    this.getGroups().get(i).getUsers().size() <= 1)
                this.getGroups().remove(i);

        for (Room r : this.getGroups())
            r.getUsers().remove(user);

    }

    public synchronized boolean containsUser (String user) {
        for (String s : this.getUsers())
            if (s.equals(user))
                return true;

        return false;
    }

    public synchronized boolean containsGroup (String group) {
        for (Room r : this.getGroups())
            if (r.getName().equals(group))
                return true;

        return false;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public boolean containsGroup (Room group) {
        return containsGroup(group.getName());
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<Room> getGroups() {
        return groups;
    }

    public String getName() {
        return name;
    }

    public RoomType getType() {
        return type;
    }

    public Room getParent() {
        return parent;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGroups(ArrayList<Room> groups) {
        this.groups = groups;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public void setParent(Room parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public boolean hasGroup(Room room) {
        for (Room r : this.getGroups())
            if (r.getId().equals(room.getId()))
                return true;

        return false;
    }
}

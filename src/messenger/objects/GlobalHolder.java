package messenger.objects;

import messenger.objects.types.ObjectType;

import java.io.Serializable;
import java.util.ArrayList;

public class GlobalHolder extends SerializedObject{
    private static final long serialVersionUID = -4772206912135763226L;

    private String password;
    private ArrayList<Room> rooms;
    private ArrayList<String> users;

    public GlobalHolder () {
        this.setRooms(new ArrayList<>());
        this.setUsers(new ArrayList<>());
        this.setObjectType(ObjectType.HOLDER);
    }

    public boolean hasRoom (String roomId) {
        synchronized (this.getRooms()) {
            for(Room r : this.getRooms())
                if (r.getId().equals(roomId))
                    return true;
        }

        return false;
    }

    public void removeUser(String clientName) {
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = 0; j < rooms.get(i).getGroups().size(); i++)
                for (int k = 0; k < rooms.get(i).getGroups().get(j).getUsers().size(); k++)
                    if (rooms.get(i).getGroups().get(j).getUsers().get(k).equals(clientName))
                        rooms.get(i).getGroups().get(j).getUsers().remove(k);

            for (int k = 0; k < rooms.get(i).getUsers().size(); k++)
                if (rooms.get(i).getUsers().get(k).equals(clientName))
                    rooms.get(i).getUsers().remove(k);
        }

        for (int i = 0; i < users.size(); i++)
            if (users.get(i).equals(clientName))
                users.remove(i);
    }

    public boolean hasUser(String clientName) {
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = 0; j < rooms.get(i).getGroups().size(); i++)
                if(rooms.get(i).getGroups().get(j).getUsers().indexOf(clientName) != -1)
                    return true;

            if(rooms.get(i).getUsers().indexOf(clientName) != -1)
                return true;
        }

        return false;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public String getPassword() {
        return password;
    }
}

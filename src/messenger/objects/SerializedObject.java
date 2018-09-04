package messenger.objects;

import messenger.objects.types.ObjectType;

import java.io.Serializable;

public abstract class SerializedObject implements Serializable{

    private static final long serialVersionUID = -3831055770729245848L;

    private ObjectType objectType;

    public void setObjectType(ObjectType type) {
        this.objectType = type;
    }

    public ObjectType getObjectType() {
        return objectType;
    }
}

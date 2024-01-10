package org.example;

public class ObjectNotFoundException extends GoogleDataObjectImplException {
    public ObjectNotFoundException() {
        super("Object not found.");
    }
}

package org.example;

public class ObjectAlreadyExistsException extends GoogleDataObjectImplException{
    public ObjectAlreadyExistsException() {
        super("Object already exists.");
    }
}

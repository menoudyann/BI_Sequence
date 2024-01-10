package org.example;

public class NotEmptyObjectException extends GoogleDataObjectImplException{

    public NotEmptyObjectException() {
        super("Oject is not empty.");
    }
}

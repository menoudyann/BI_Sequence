package org.example;

public class Label {

    protected String name;
    protected float score;


    public Label(String name, float score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public float getScore() {
        return score;
    }

}
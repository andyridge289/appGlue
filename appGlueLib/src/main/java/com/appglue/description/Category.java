package com.appglue.description;

public class Category {

    public int id;
    public String name;

    public Category(String name) {
        this(-1, name);
    }

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }
}

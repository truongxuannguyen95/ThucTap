package com.example.nguyen.fileencryption.model;

public class Files {
    private String name, key;

    public Files(){}

    public Files(String name, String key) {
        this.name = name;
        this.key = key;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

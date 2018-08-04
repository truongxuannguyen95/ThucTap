package com.example.nguyen.fileencryption.model;

public class Files {
    private String name, key, keyData;

    public Files(){}

    public Files(String name, String key, String keyData) {
        this.name = name;
        this.key = key;
        this.keyData = keyData;
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

    public String getKeyData() {
        return keyData;
    }

    public void setKeyData(String keyData) {
        this.keyData = keyData;
    }
}

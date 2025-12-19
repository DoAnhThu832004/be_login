package com.devteria.identityservice.dto.request;

public class GenreCreationRequest {
    private String name;
    private String keyG;

    public GenreCreationRequest() {
    }

    public GenreCreationRequest(String name, String keyG) {
        this.name = name;
        this.keyG = keyG;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyG() {
        return keyG;
    }

    public void setKeyG(String keyG) {
        this.keyG = keyG;
    }
}

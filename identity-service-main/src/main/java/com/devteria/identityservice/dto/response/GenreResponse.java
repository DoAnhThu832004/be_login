package com.devteria.identityservice.dto.response;

public class GenreResponse {
    private String id;
    private String name;
    private String keyG;

    public GenreResponse() {
    }

    public GenreResponse(String id, String name, String keyG) {
        this.id = id;
        this.name = name;
        this.keyG = keyG;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

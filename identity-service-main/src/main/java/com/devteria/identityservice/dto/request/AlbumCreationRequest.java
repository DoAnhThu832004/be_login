package com.devteria.identityservice.dto.request;

public class AlbumCreationRequest {
    private String name;
    private String description;

    public AlbumCreationRequest() {
    }

    public AlbumCreationRequest(String name, String description, String imageUrlA) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

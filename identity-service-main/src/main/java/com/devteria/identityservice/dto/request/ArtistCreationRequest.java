package com.devteria.identityservice.dto.request;

public class ArtistCreationRequest {
    private String name;
    private String description;

    public ArtistCreationRequest() {
    }

    public ArtistCreationRequest(String name, String description) {
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

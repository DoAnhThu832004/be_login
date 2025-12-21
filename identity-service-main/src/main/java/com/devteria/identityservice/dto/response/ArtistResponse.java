package com.devteria.identityservice.dto.response;

public class ArtistResponse {
    private String id;
    private String name;
    private String description;
    private String imageUrlAr;

    public ArtistResponse() {
    }

    public ArtistResponse(String id, String name, String description, String imageUrlAr) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrlAr = imageUrlAr;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrlAr() {
        return imageUrlAr;
    }

    public void setImageUrlAr(String imageUrlAr) {
        this.imageUrlAr = imageUrlAr;
    }
}

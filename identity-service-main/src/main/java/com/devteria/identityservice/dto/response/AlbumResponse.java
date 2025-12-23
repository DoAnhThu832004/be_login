package com.devteria.identityservice.dto.response;

import com.devteria.identityservice.enums.Status;

import java.util.Set;

public class AlbumResponse {
    private String id;
    private String name;
    private String description;
    private Status status;
    private String imageUrlA;
    private Set<SongResponse> songs;
    public AlbumResponse() {
    }

    public AlbumResponse(String id, String name, String description, Status status, String imageUrlA) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.imageUrlA = imageUrlA;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getImageUrlA() {
        return imageUrlA;
    }

    public void setImageUrlA(String imageUrlA) {
        this.imageUrlA = imageUrlA;
    }

    public Set<SongResponse> getSongs() {
        return songs;
    }

    public void setSongs(Set<SongResponse> songs) {
        this.songs = songs;
    }
}

package com.devteria.identityservice.dto.response;

import java.util.Set;

public class ArtistResponse {
    private String id;
    private String name;
    private String description;
    private String imageUrlAr;
    private Set<SongResponse> songs;
    private Set<AlbumResponse> albums;

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

    public Set<SongResponse> getSongs() {
        return songs;
    }

    public void setSongs(Set<SongResponse> songs) {
        this.songs = songs;
    }

    public Set<AlbumResponse> getAlbums() {
        return albums;
    }

    public void setAlbums(Set<AlbumResponse> albums) {
        this.albums = albums;
    }
}

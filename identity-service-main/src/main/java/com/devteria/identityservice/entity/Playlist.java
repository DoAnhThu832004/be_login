package com.devteria.identityservice.entity;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String title;
    private String description;

    @ManyToMany
    @JoinTable(
            name = "playlist_song",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private Set<Song> songPlayList;

    public Playlist() {
    }

    public Playlist(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Song> getSongPlayList() {
        return songPlayList;
    }

    public void setSongPlayList(Set<Song> songPlayList) {
        this.songPlayList = songPlayList;
    }
}

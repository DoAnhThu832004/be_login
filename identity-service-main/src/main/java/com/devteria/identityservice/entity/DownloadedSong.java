package com.devteria.identityservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class DownloadedSong {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Song song;

    private LocalDateTime downloadedAt;

    public DownloadedSong() {
    }

    public DownloadedSong(String id, User user, Song song, LocalDateTime downloadedAt) {
        this.id = id;
        this.user = user;
        this.song = song;
        this.downloadedAt = downloadedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public LocalDateTime getDownloadedAt() {
        return downloadedAt;
    }

    public void setDownloadedAt(LocalDateTime downloadedAt) {
        this.downloadedAt = downloadedAt;
    }
}

package com.devteria.identityservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String text;
    private boolean edited = false;
    private LocalDateTime createAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song song;

    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now();
    }
    public Comment() {
    }

    public Comment(String id, String text, boolean edited, LocalDateTime createAt, User user, Song song) {
        this.id = id;
        this.text = text;
        this.edited = edited;
        this.createAt = createAt;
        this.user = user;
        this.song = song;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
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
}

package com.devteria.identityservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "followers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "artist_id"}) // Một User chỉ follow 1 Artist 1 lần
})
public class Follower {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    private LocalDateTime createAt;

    public Follower() {
    }

    public Follower(User user, Artist artist) {
        this.user = user;
        this.artist = artist;
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

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }
    @PrePersist
    public void onCreate() {
        this.createAt = LocalDateTime.now();
    }
}

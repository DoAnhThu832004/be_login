package com.devteria.identityservice.entity;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Year {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Integer year;
    @OneToMany(mappedBy = "year")
    private Set<Song> songs;

    public Year() {
    }

    public Year(String id, Integer year, Set<Song> songs) {
        this.id = id;
        this.year = year;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Set<Song> getSongs() {
        return songs;
    }

    public void setSongs(Set<Song> songs) {
        this.songs = songs;
    }
}

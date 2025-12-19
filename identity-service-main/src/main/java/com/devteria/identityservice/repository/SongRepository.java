package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song,String> {
    Optional<Song> findByName(String name);
}

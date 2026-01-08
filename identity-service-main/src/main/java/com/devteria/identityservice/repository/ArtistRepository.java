package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Artist;
import com.devteria.identityservice.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist,String> {
    Optional<Artist> findByName(String name);
    List<Artist> findByNameContainingIgnoreCase(String keyword);
}

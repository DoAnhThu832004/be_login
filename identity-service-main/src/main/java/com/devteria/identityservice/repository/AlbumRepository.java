package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Album;
import com.devteria.identityservice.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album,String> {
    List<Album> findByNameContainingIgnoreCase(String keyword);
}

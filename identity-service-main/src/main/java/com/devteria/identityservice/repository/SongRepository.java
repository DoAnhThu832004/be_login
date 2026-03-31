package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song,String> {
    Optional<Song> findByName(String name);
    Page<Song> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    List<Song> findTop10ByOrderByPlayCountDesc();
}

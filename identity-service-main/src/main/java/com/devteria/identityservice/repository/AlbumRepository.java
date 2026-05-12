package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Album;
import com.devteria.identityservice.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album,String> {
    @EntityGraph(attributePaths = {"songs"})
    Page<Album> findAll(Pageable pageable);
    @EntityGraph(attributePaths = {"songs"}) // Kết hợp tối ưu N+1 luôn
    Page<Album> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}

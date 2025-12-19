package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist,String> {
}

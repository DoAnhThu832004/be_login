package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Playlist;
import com.devteria.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist,String> {
    List<Playlist> findByUser(User user);
    @Query("SELECT p FROM Playlist p JOIN p.user u JOIN u.roles r WHERE r.name = 'ADMIN'")
    List<Playlist> findAllAdminPlaylists();
}

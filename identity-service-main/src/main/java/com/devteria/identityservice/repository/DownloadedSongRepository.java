package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.DownloadedSong;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DownloadedSongRepository extends JpaRepository<DownloadedSong, String> {
    List<DownloadedSong> findAllByUserOrderByDownloadedAtDesc(User user);
    boolean existsByUserAndSong(User user, Song song);
}

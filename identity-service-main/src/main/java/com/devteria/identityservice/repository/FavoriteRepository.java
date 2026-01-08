package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Favorite;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite,String> {
    Optional<Favorite> findByUserAndSong(User user, Song song);
    List<Favorite> findByUser(User user);
    boolean existsByUserAndSong(User user, Song song);
    List<Favorite> findAllByUser(User user);
}

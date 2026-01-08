package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Artist;
import com.devteria.identityservice.entity.Favorite;
import com.devteria.identityservice.entity.Follower;
import com.devteria.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowerRepository extends JpaRepository<Follower, String> {
    boolean existsByUserAndArtist(User user, Artist artist);
    Optional<Follower> findByUserAndArtist(User user, Artist artist);
    List<Follower> findByUser(User user); // Để lấy danh sách nghệ sĩ mình đang follow
    long countByArtist(Artist artist);
    List<Follower> findAllByUser(User user);
}

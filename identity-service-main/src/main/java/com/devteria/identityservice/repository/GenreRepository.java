package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre,String> {
    Optional<Genre> findByName(String name);
    Optional<Genre> findByKeyG(String keyG);
}

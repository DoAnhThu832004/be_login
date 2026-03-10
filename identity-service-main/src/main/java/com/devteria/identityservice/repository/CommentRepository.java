package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findAllBySongId(String songId);
    long countBySongId(String songId);
    List<Comment> findAllByUserId(String userId);
}

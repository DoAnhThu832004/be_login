package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.CommentCreationRequest;
import com.devteria.identityservice.dto.request.CommentUpdateRequest;
import com.devteria.identityservice.dto.response.CommentResponse;
import com.devteria.identityservice.entity.Comment;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.entity.User;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.CommentRepository;
import com.devteria.identityservice.repository.SongRepository;
import com.devteria.identityservice.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, SongRepository songRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.songRepository = songRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentResponse createComment(String songId, CommentCreationRequest request) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setUser(user);
        comment.setSong(song);

        return toCommentResponse(commentRepository.save(comment));
    }
    public List<CommentResponse> getCommentsBySong(String songId) {
        return commentRepository.findAllBySongId(songId).stream()
                .map(CommentService::toCommentResponse)
                .toList();
    }
    @Transactional
    public CommentResponse updateComment(String id, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // Kiểm tra quyền sở hữu
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!comment.getUser().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        mapRequestToComment(comment, request);
        comment.setEdited(true);

        return toCommentResponse(commentRepository.save(comment));
    }
    @Transactional
    public void deleteComment(String id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!comment.getUser().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        commentRepository.deleteById(id);
    }
    private void mapRequestToComment(Comment comment, CommentUpdateRequest request) {
        if (request == null) return;
        if (request.getText() != null && !request.getText().isEmpty()) {
            comment.setText(request.getText());
        }
    }

    public Comment toComment(CommentCreationRequest request) {
        if (request == null) return null;
        Comment comment = new Comment();
        comment.setText(request.getText());
        return comment;
    }

    public static CommentResponse toCommentResponse(Comment comment) {
        if (comment == null) return null;

        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setText(comment.getText());
        response.setEdited(comment.isEdited());
        response.setCreateAt(comment.getCreateAt());

        if (comment.getUser() != null) {
            response.setUsername(comment.getUser().getUsername());
            String currentUsername = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            response.setOwner(
                    comment.getUser().getUsername().equals(currentUsername)
            );
        }

        if (comment.getSong() != null) {
            // Sửa lỗi 2 & 3: Không setSongId và lấy tên bài hát bằng getName() thay vì getTitle()
            response.setSongTitle(comment.getSong().getName());
        }

        return response;
    }
}
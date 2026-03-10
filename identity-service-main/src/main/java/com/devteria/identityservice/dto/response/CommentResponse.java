package com.devteria.identityservice.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {
    private String id;
    private String text;
    private boolean edited;
    private LocalDateTime createAt;
    private String username;
    private String songTitle;
    private boolean owner;
    private List<CommentResponse> replies;
    private String parentId;

    public CommentResponse() {
    }

    public CommentResponse(String id, String text, boolean edited, LocalDateTime createAt, String username, String songTitle, boolean owner) {
        this.id = id;
        this.text = text;
        this.edited = edited;
        this.createAt = createAt;
        this.username = username;
        this.songTitle = songTitle;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public List<CommentResponse> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentResponse> replies) {
        this.replies = replies;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

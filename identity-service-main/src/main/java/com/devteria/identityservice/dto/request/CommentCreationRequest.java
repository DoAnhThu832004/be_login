package com.devteria.identityservice.dto.request;

public class CommentCreationRequest {
    private String text;
    private String parentId;

    public CommentCreationRequest() {
    }

    public CommentCreationRequest(String text, String parentId) {
        this.text = text;
        this.parentId = parentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

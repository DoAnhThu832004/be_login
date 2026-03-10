package com.devteria.identityservice.dto.request;

public class CommentCreationRequest {
    private String text;

    public CommentCreationRequest() {
    }

    public CommentCreationRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

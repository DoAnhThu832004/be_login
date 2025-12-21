package com.devteria.identityservice.dto.request;

import com.devteria.identityservice.enums.Status;

public class AlbumUpdateRequest {
    private String name;
    private String description;
    private Status status;

    public AlbumUpdateRequest() {
    }

    public AlbumUpdateRequest(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

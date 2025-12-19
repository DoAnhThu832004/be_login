package com.devteria.identityservice.dto.response;

public class YearResponse {
    private String id;
    private Integer year;

    public YearResponse() {
    }

    public YearResponse(String id, Integer year) {
        this.id = id;
        this.year = year;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}

package com.devteria.identityservice.dto.request;

public class YearRequest {
    private Integer year;

    public YearRequest() {
    }

    public YearRequest(Integer year) {
        this.year = year;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}

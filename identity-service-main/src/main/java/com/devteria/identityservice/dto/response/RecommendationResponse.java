package com.devteria.identityservice.dto.response;

import java.util.List;

/**
 * DTO trả về kết quả gợi ý nhạc.
 * Bao gồm metadata về nguồn gốc gợi ý và danh sách bài hát.
 */
public class RecommendationResponse {

    /**
     * Nguồn gốc của danh sách gợi ý:
     * - PERSONALIZED: Dựa trên lịch sử nghe nhạc cá nhân (Item-Based CF + MMR)
     * - COLD_START_GENRE: User mới, trả về Trending theo thể loại yêu thích đã chọn
     * - COLD_START_GLOBAL: User mới và chưa chọn thể loại, trả về Global Trending
     */
    private String source;

    /**
     * Tổng số bài hát trong danh sách.
     */
    private int totalCount;

    /**
     * Danh sách bài hát được gợi ý.
     */
    private List<SongResponse> songs;

    public RecommendationResponse() {}

    public RecommendationResponse(String source, List<SongResponse> songs) {
        this.source = source;
        this.songs = songs;
        this.totalCount = songs != null ? songs.size() : 0;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public List<SongResponse> getSongs() { return songs; }
    public void setSongs(List<SongResponse> songs) {
        this.songs = songs;
        this.totalCount = songs != null ? songs.size() : 0;
    }
}

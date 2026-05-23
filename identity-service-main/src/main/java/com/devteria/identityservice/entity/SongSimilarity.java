package com.devteria.identityservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Lưu trữ độ tương đồng đã được tính toán trước (pre-computed) giữa mọi cặp bài hát.
 * Bảng này được cập nhật bởi Cron Job offline lúc 2h sáng và được
 * đọc trực tiếp bởi API online để trả kết quả trong < 50ms.
 *
 * Cách tính: Adjusted Cosine Similarity dựa trên điểm mean-centered của users.
 */
@Entity
@Table(
        name = "song_similarity",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"song_a_id", "song_b_id"})
        },
        indexes = {
                @Index(name = "idx_song_a", columnList = "song_a_id"),
                @Index(name = "idx_song_b", columnList = "song_b_id")
        }
)
public class SongSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID của bài hát A trong cặp so sánh.
     * Luôn đảm bảo songAId < songBId (về mặt chuỗi) để tránh lưu trùng lặp hai chiều.
     */
    @Column(name = "song_a_id", nullable = false)
    private String songAId;

    /**
     * ID của bài hát B trong cặp so sánh.
     */
    @Column(name = "song_b_id", nullable = false)
    private String songBId;

    /**
     * Điểm độ tương đồng Adjusted Cosine, trong khoảng [-1.0, 1.0].
     * Chỉ lưu các cặp có score > 0.1 để tiết kiệm không gian.
     */
    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    /**
     * Số lượng người dùng đã nghe CẢ HAI bài hát.
     * Dùng để lọc ngưỡng: cặp có < 3 users chung sẽ bị bỏ qua.
     */
    @Column(name = "common_user_count", nullable = false)
    private Integer commonUserCount;

    /**
     * Thời điểm cặp này được tính toán và cập nhật lần cuối.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SongSimilarity() {}

    public SongSimilarity(String songAId, String songBId, Double similarityScore,
                          Integer commonUserCount, LocalDateTime updatedAt) {
        this.songAId = songAId;
        this.songBId = songBId;
        this.similarityScore = similarityScore;
        this.commonUserCount = commonUserCount;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSongAId() { return songAId; }
    public void setSongAId(String songAId) { this.songAId = songAId; }

    public String getSongBId() { return songBId; }
    public void setSongBId(String songBId) { this.songBId = songBId; }

    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }

    public Integer getCommonUserCount() { return commonUserCount; }
    public void setCommonUserCount(Integer commonUserCount) { this.commonUserCount = commonUserCount; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

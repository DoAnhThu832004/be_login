package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.response.HomeRecommendationResponse;
import com.devteria.identityservice.dto.response.RecommendationResponse;
import com.devteria.identityservice.service.RecommendationEngineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller cho Luồng Online — Trả về gợi ý nhạc real-time.
 *
 * Endpoints:
 *   GET /api/recommendations?userId={userId}&limit={limit}
 *       → Chỉ trả về bài hát (backward-compatible)
 *
 *   GET /api/recommendations/home?userId={userId}
 *       → Trả về đầy đủ: Songs + Artists + Albums + Playlists cho trang chủ
 *
 * Yêu cầu: JWT hợp lệ (Bearer token trong Authorization header).
 *
 * Flow (Songs):
 * 1. Kiểm tra Cold Start (user mới → Trending)
 * 2. Lấy User Profile từ lịch sử tương tác
 * 3. Tìm Candidate từ bảng song_similarity (đã pre-computed)
 * 4. Tính Predicted Score
 * 5. MMR Re-ranking (cân bằng Relevance + Diversity)
 * 6. Trả về danh sách Top K bài hát
 *
 * Flow (Home - Aggregation):
 * 1-5. Như trên nhưng lấy pool 50 bài
 * 6. Aggregate in-memory → Artists, Albums, Playlists
 * 7. Hydrate entities từ DB
 * 8. Trả về HomeRecommendationResponse
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationEngineService recommendationEngineService;

    public RecommendationController(RecommendationEngineService recommendationEngineService) {
        this.recommendationEngineService = recommendationEngineService;
    }

    /**
     * Lấy danh sách bài hát gợi ý cá nhân hóa cho user.
     * (API cũ — giữ nguyên để không breaking changes)
     *
     * @param userId ID của user (bắt buộc)
     * @param limit  Số bài hát muốn nhận (mặc định: 10, tối đa nên là 50)
     * @return RecommendationResponse với source và danh sách bài hát
     */
    @GetMapping
    public ApiResponse<RecommendationResponse> getRecommendations(
            @RequestParam("userId") String userId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        RecommendationResponse result = recommendationEngineService.getRecommendations(userId, limit);

        return ApiResponse.<RecommendationResponse>builder()
                .code(1000)
                .message("Lấy danh sách gợi ý thành công. Nguồn: " + result.getSource())
                .result(result)
                .build();
    }

    /**
     * Lấy dữ liệu đầy đủ cho trang chủ: Songs + Artists + Albums + Playlists.
     * Dùng Aggregation Pipeline để suy ra Artists/Albums/Playlists từ kết quả
     * gợi ý bài hát — không cần bảng DB mới, xử lý in-memory O(N) N=50.
     *
     * Cold Start (user mới):
     *   - Có preferredGenres → Trending theo thể loại yêu thích đã chọn khi đăng ký
     *   - Không có genre    → Global Trending + Admin Playlists
     *
     * @param userId ID của user (bắt buộc)
     * @return HomeRecommendationResponse chứa songs, artists, albums, playlists
     */
    @GetMapping("/home")
    public ApiResponse<HomeRecommendationResponse> getHomeRecommendations(
            @RequestParam("userId") String userId) {

        HomeRecommendationResponse result = recommendationEngineService.getHomeRecommendations(userId);

        return ApiResponse.<HomeRecommendationResponse>builder()
                .code(1000)
                .message("Lấy dữ liệu trang chủ thành công. Nguồn: " + result.getSource())
                .result(result)
                .build();
    }
}
package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.response.RecommendationResponse;
import com.devteria.identityservice.service.RecommendationEngineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller cho Luồng Online — Trả về gợi ý nhạc real-time.
 *
 * Endpoint: GET /identity/api/recommendations?userId={userId}&limit={limit}
 *
 * Yêu cầu: JWT hợp lệ (Bearer token trong Authorization header).
 *
 * Flow:
 * 1. Kiểm tra Cold Start (user mới → Trending)
 * 2. Lấy User Profile từ lịch sử tương tác
 * 3. Tìm Candidate từ bảng song_similarity (đã pre-computed)
 * 4. Tính Predicted Score
 * 5. MMR Re-ranking (cân bằng Relevance + Diversity)
 * 6. Trả về danh sách Top K bài hát
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
}
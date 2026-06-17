package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.response.HomeRecommendationResponse;
import com.devteria.identityservice.dto.response.RecommendationResponse;
import com.devteria.identityservice.service.RecommendationEngineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationEngineService recommendationEngineService;

    public RecommendationController(RecommendationEngineService recommendationEngineService) {
        this.recommendationEngineService = recommendationEngineService;
    }

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
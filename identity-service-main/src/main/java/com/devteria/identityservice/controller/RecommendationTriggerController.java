package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.service.InteractionAggregationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/recommendations")
public class RecommendationTriggerController {

    private final InteractionAggregationService aggregationService;

    public RecommendationTriggerController(InteractionAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @PostMapping("/trigger-aggregation")
    public ApiResponse<String> triggerAggregationJobManually() {
        aggregationService.calculateAndStoreInteractions();

        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(1000);
        response.setMessage("Tiến trình tổng hợp điểm số tương tác đã được thực thi");
        // Bổ sung dòng mã này để triệt tiêu hiện tượng null trên giao diện mạng
        response.setResult("MA_TRAN_DA_DUOC_CAP_NHAT");
        return response;
    }
}
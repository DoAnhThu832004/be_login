package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.service.InteractionAggregationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller Admin để kích hoạt thủ công các tác vụ offline.
 * Thông thường các tác vụ này chạy tự động lúc 2h sáng qua Cron Job.
 * Dùng để debug, test, hoặc force-refresh dữ liệu ngay lập tức.
 *
 * Yêu cầu quyền ADMIN.
 */
@RestController
@RequestMapping("/api/admin/recommendations")
public class RecommendationTriggerController {

    private final InteractionAggregationService aggregationService;

    public RecommendationTriggerController(InteractionAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    /**
     * Trigger toàn bộ pipeline offline:
     * 1. Sync raw interactions (LIKE từ Favorites, DOWNLOAD từ DownloadedSongs)
     * 2. Tính Adjusted Cosine Similarity
     * 3. Batch Insert vào song_similarity
     *
     * POST /identity/api/admin/recommendations/trigger-full-pipeline
     */
    @PostMapping("/trigger-full-pipeline")
    public ApiResponse<String> triggerFullPipeline() {
        aggregationService.runNightlyRecommendationJob();

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Pipeline offline đã chạy xong. Kiểm tra log để biết chi tiết.")
                .result("PIPELINE_COMPLETE")
                .build();
    }

    /**
     * Chỉ sync raw interactions (LIKE & DOWNLOAD) vào bảng user_interactions.
     * Không tính lại similarity.
     *
     * POST /identity/api/admin/recommendations/trigger-sync
     */
    @PostMapping("/trigger-sync")
    public ApiResponse<String> triggerSyncOnly() {
        aggregationService.syncRawInteractions();

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Sync dữ liệu tương tác thô hoàn tất.")
                .result("SYNC_COMPLETE")
                .build();
    }

    /**
     * Chỉ tính lại similarity (dùng dữ liệu user_interactions hiện có).
     * Không sync lại raw data. Hữu ích khi đã có đủ dữ liệu trong user_interactions.
     *
     * POST /identity/api/admin/recommendations/trigger-similarity
     */
    @PostMapping("/trigger-similarity")
    public ApiResponse<String> triggerSimilarityOnly() {
        aggregationService.computeAndStoreSimilarity();

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Tính toán Song Similarity hoàn tất. Bảng song_similarity đã được cập nhật.")
                .result("SIMILARITY_COMPLETE")
                .build();
    }

    /**
     * Endpoint cũ — giữ lại để backward compatibility.
     * @deprecated Dùng /trigger-full-pipeline thay thế.
     */
    @Deprecated
    @PostMapping("/trigger-aggregation")
    public ApiResponse<String> triggerAggregationJobManually() {
        aggregationService.syncRawInteractions();

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Tiến trình tổng hợp điểm số tương tác đã được thực thi")
                .result("MA_TRAN_DA_DUOC_CAP_NHAT")
                .build();
    }
}
package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.response.SongResponse; // Thêm import này
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.service.RecommendationEngineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationEngineService recommendationEngineService;

    // NẾU BẠN CÓ DÙNG SONG MAPPER (MapStruct), HÃY KHAI BÁO VÀ INJECT NÓ VÀO ĐÂY
    // private final SongMapper songMapper;

    public RecommendationController(RecommendationEngineService recommendationEngineService) {
        this.recommendationEngineService = recommendationEngineService;
    }

    // LƯU Ý: Đổi List<Song> thành List<SongResponse>
    @GetMapping("/users/{userId}")
    public ApiResponse<List<SongResponse>> getRecommendationsForUser(
            @PathVariable("userId") String userId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        // 1. Lấy danh sách Thực thể (Entity) từ thuật toán
        List<Song> recommendedSongs = recommendationEngineService.getPersonalizedRecommendations(userId, limit);

        // 2. Chuyển đổi Thực thể (Entity) sang Đối tượng truyền tải (DTO / Response)
        List<SongResponse> responseList = new ArrayList<>();

        for (Song song : recommendedSongs) {
            SongResponse dto = new SongResponse();
            dto.setId(song.getId());
            dto.setName(song.getName());
            dto.setDescription(song.getDescription());
            dto.setDuration(song.getDuration());
            dto.setReleasedDate(song.getReleasedDate());
            dto.setImageUrl(song.getImageUrl());
            dto.setAudioUrl(song.getAudioUrl());
            dto.setPlayCount(song.getPlayCount());

            // Nếu song có quan hệ với các bảng khác, bạn get tên ra (VD: tên ca sĩ)
            // dto.setArtistName(song.getArtist() != null ? song.getArtist().getName() : "Unknown");

            responseList.add(dto);
        }

        /* GHI CHÚ: Nếu project của bạn có sẵn hàm chuyển đổi (Ví dụ trong thư viện MapStruct),
        bạn có thể xóa vòng for ở trên và dùng lệnh này cho code ngắn gọn:
        List<SongResponse> responseList = recommendedSongs.stream()
                .map(songMapper::toSongResponse)
                .collect(Collectors.toList());
        */

        // 3. Đóng gói và trả về
        ApiResponse<List<SongResponse>> response = new ApiResponse<>();
        response.setCode(1000);
        response.setMessage("Truy xuất ma trận gợi ý cá nhân hóa thành công");
        response.setResult(responseList);

        return response;
    }
}
package com.devteria.identityservice.controller;

import com.cloudinary.Api;
import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.response.SongResponse;
import com.devteria.identityservice.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }
    @PostMapping("/{songId}")
    public ApiResponse<String> addFavorite(@PathVariable String songId) {
        favoriteService.addSongToFavorite(songId);
        return ApiResponse.<String>builder()
                .result("Add to favorites successfully")
                .build();
    }
    @DeleteMapping("/{songId}")
    public ApiResponse<String> removeFavorite(@PathVariable String songId) {
        favoriteService.removeSongFromFavorite(songId);
        return ApiResponse.<String>builder()
                .result("Removed from favorites successfully")
                .build();
    }
    @GetMapping("/my-favorites")
    public ApiResponse<List<SongResponse>> getMyFavorites() {
        List<SongResponse> result = favoriteService.getMyFavorites();

        ApiResponse<List<SongResponse>> response = new ApiResponse<>();
        response.setResult(result);
        return response;
    }
}

package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.PlaylistCreationRequest;
import com.devteria.identityservice.dto.request.PlaylistUpdateRequest;
import com.devteria.identityservice.dto.response.PlaylistResponse;
import com.devteria.identityservice.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }
    @PostMapping()
    public ApiResponse<PlaylistResponse> createPlaylist(@RequestBody @Valid PlaylistCreationRequest request) {
        return ApiResponse.<PlaylistResponse>builder()
                .result(playlistService.createPlaylist(request))
                .build();
    }
    @GetMapping
    public ApiResponse<List<PlaylistResponse>> getPlaylists() {
        return ApiResponse.<List<PlaylistResponse>>builder()
                .result(playlistService.getPlayLists())
                .build();
    }
    @GetMapping("/{playlistId}")
    public ApiResponse<PlaylistResponse> getPlaylist(@PathVariable("playlistId") String id) {
        return ApiResponse.<PlaylistResponse>builder()
                .result(playlistService.getPlaylist(id))
                .build();
    }
    @PutMapping("/{playlistId}")
    public ApiResponse<PlaylistResponse> updatePlaylist(@PathVariable("playlistId") String id, @RequestBody @Valid PlaylistUpdateRequest request) {
        return ApiResponse.<PlaylistResponse>builder()
                .result(playlistService.updatePlaylist(id,request))
                .build();
    }
    @DeleteMapping("/{playlistId}")
    public ApiResponse<String> deletePlaylist(@PathVariable("playlistId") String id) {
        playlistService.deletePlaylist(id);
        return ApiResponse.<String>builder()
                .result("Playlist updated successfully")
                .build();
    }
}

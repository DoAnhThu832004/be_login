package com.devteria.identityservice.controller;

import com.cloudinary.Api;
import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.SongCreationRequest;
import com.devteria.identityservice.dto.request.SongUpdateRequest;
import com.devteria.identityservice.dto.response.SongResponse;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.service.SongService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/songs")
public class SongController {
    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping
    public ApiResponse<SongResponse> createSong(@RequestBody @Valid SongCreationRequest request) {
        return ApiResponse.<SongResponse>builder()
                .result(songService.createSong(request))
                .build();
    }
    @GetMapping
    public ApiResponse<List<SongResponse>> getSongs() {
        return ApiResponse.<List<SongResponse>>builder()
                .result(songService.getSongs())
                .build();
    }
    @GetMapping("/{songId}")
    public ApiResponse<SongResponse> getSong(@PathVariable("songId") String id) {
        return ApiResponse.<SongResponse>builder()
                .result(songService.getSong(id))
                .build();
    }
    @GetMapping("/search")
    public ApiResponse<SongResponse> getSongByName(@RequestParam String name) {
        return ApiResponse.<SongResponse>builder()
                .result(songService.getSongByName(name))
                .build();
    }
    @PutMapping("/{songId}")
    public ApiResponse<SongResponse> updateSong(@PathVariable("songId") String id,@RequestBody @Valid SongUpdateRequest request) {
        return ApiResponse.<SongResponse>builder()
                .result(songService.updateSong(id,request))
                .build();
    }
    @DeleteMapping("/{songId}")
    public ApiResponse<String> deleteSong(@PathVariable("songId") String id) {
        songService.deleteSong(id);
        return ApiResponse.<String>builder()
                .result("Song has been deleted")
                .build();
    }
    @PostMapping("/{songId}/upload")
    public ApiResponse<SongResponse> uploadSongFiles(
            @PathVariable("songId") String id,
            @RequestParam("image") MultipartFile image,
            @RequestParam("audio") MultipartFile audio) {
        try {
            Song updated = songService.uploadSongFile(id, image, audio);
            return ApiResponse.<SongResponse>builder()
                    .result(SongService.toSongResponse(updated))
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }
}

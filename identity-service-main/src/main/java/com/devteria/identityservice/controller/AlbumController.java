package com.devteria.identityservice.controller;

import com.cloudinary.Api;
import com.devteria.identityservice.dto.request.AlbumCreationRequest;
import com.devteria.identityservice.dto.request.AlbumUpdateRequest;
import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.response.AlbumResponse;
import com.devteria.identityservice.entity.Album;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/albums")
public class AlbumController {
    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }
    @PostMapping
    ApiResponse<AlbumResponse> createAlbum(@RequestBody @Valid AlbumCreationRequest request) {
        return ApiResponse.<AlbumResponse>builder()
                .result(albumService.createAlbum(request))
                .build();
    }
    @GetMapping
    ApiResponse<List<AlbumResponse>> getAlbums() {
        return ApiResponse.<List<AlbumResponse>>builder()
                .result(albumService.getAlbums())
                .build();
    }
    @GetMapping("/{albumId}")
    ApiResponse<AlbumResponse> getAlbum(@PathVariable("albumId") String id) {
        return ApiResponse.<AlbumResponse>builder()
                .result(albumService.getAlbum(id))
                .build();
    }
    @PutMapping("/{albumId}")
    ApiResponse<AlbumResponse> updateAlbum(@PathVariable("albumId") String id,@RequestBody @Valid AlbumUpdateRequest request) {
        return ApiResponse.<AlbumResponse>builder()
                .result(albumService.updateAlbum(id,request))
                .build();
    }
    @DeleteMapping("/{albumId}")
    ApiResponse<String> deleteAlbum(@PathVariable("albumId") String id) {
        albumService.deleteAlbum(id);
        return ApiResponse.<String>builder()
                .result("Album has been deleted")
                .build();
    }
    @PostMapping("/{albumId}/upload")
    ApiResponse<AlbumResponse> uploadAlbumFiles(
            @PathVariable("albumId") String id,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            Album updated = albumService.uploadAlbumFiles(id,image);
            return ApiResponse.<AlbumResponse>builder()
                    .result(AlbumService.toAlbumResponse(updated))
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }
}

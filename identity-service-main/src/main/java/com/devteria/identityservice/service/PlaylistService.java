package com.devteria.identityservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.devteria.identityservice.dto.request.GenreUpdateRequest;
import com.devteria.identityservice.dto.request.PlaylistCreationRequest;
import com.devteria.identityservice.dto.request.PlaylistUpdateRequest;
import com.devteria.identityservice.dto.response.PlaylistResponse;
import com.devteria.identityservice.entity.Artist;
import com.devteria.identityservice.entity.Genre;
import com.devteria.identityservice.entity.Playlist;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.enums.Status;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.PlaylistRepository;
import com.devteria.identityservice.repository.SongRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final Cloudinary cloudinary;

    public PlaylistService(PlaylistRepository playlistRepository, SongRepository songRepository,Cloudinary cloudinary) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
        this.cloudinary = cloudinary;
    }
    public PlaylistResponse createPlaylist(PlaylistCreationRequest request) {
        Playlist playlist = toPlaylist(request);
        return toPlaylistResponse(playlistRepository.save(playlist));

    }
    public List<PlaylistResponse> getPlayLists() {
        return playlistRepository.findAll().stream()
                .map(PlaylistService::toPlaylistResponse)
                .toList();
    }
    public PlaylistResponse getPlaylist(String id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLAYLIST_NOT_EXISTED));
        return toPlaylistResponse(playlist);
    }
    public PlaylistResponse updatePlaylist(String id, PlaylistUpdateRequest request) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.PLAYLIST_NOT_EXISTED));
        mapRequestToPlaylist(playlist,request);
        return toPlaylistResponse(playlistRepository.save(playlist));
    }
    public void deletePlaylist(String id) {
        playlistRepository.deleteById(id);
    }
    private void mapRequestToPlaylist(Playlist playlist, PlaylistUpdateRequest request) {
        if(request == null) return;
        if(request.getTitle() != null && !request.getTitle().isEmpty()) {
            playlist.setTitle(request.getTitle());
        }
        if(request.getDescription() != null && !request.getDescription().isEmpty()) {
            playlist.setDescription(request.getDescription());
        }
    }
    public Playlist uploadPlaylistFiles(String playlistId, MultipartFile image) throws IOException {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAYLIST_NOT_EXISTED));
        Map uploadImage = cloudinary.uploader().upload(image.getBytes(),
                ObjectUtils.asMap("folder", "playlist_app/images"));

        playlist.setImageUrlP(uploadImage.get("secure_url").toString());

        return playlistRepository.save(playlist);
    }
    public Playlist toPlaylist(PlaylistCreationRequest request) {
        if(request == null) return null;
        Playlist playlist = new Playlist();
        playlist.setTitle(request.getTitle());
        playlist.setDescription(request.getDescription());
        return playlist;
    }
    public static PlaylistResponse toPlaylistResponse(Playlist playlist) {
        if(playlist == null) return null;
        PlaylistResponse response = new PlaylistResponse();
        response.setId(playlist.getId());
        response.setTitle(playlist.getTitle());
        response.setDescription(playlist.getDescription());
        response.setImageUrlP(playlist.getImageUrlP());
        return response;
    }
}

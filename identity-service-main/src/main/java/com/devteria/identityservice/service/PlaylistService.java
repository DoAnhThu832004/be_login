package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.GenreUpdateRequest;
import com.devteria.identityservice.dto.request.PlaylistCreationRequest;
import com.devteria.identityservice.dto.request.PlaylistUpdateRequest;
import com.devteria.identityservice.dto.response.PlaylistResponse;
import com.devteria.identityservice.entity.Genre;
import com.devteria.identityservice.entity.Playlist;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.PlaylistRepository;
import com.devteria.identityservice.repository.SongRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.swing.*;
import java.util.List;

@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    public PlaylistService(PlaylistRepository playlistRepository, SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
    }
    public PlaylistResponse createPlaylist(PlaylistCreationRequest request) {
        Playlist playlist = toPlaylist(request);
        return toPlaylistResponse(playlistRepository.save(playlist));

    }
    public List<PlaylistResponse> getPlayLists() {
        return playlistRepository.findAll().stream()
                .map(this::toPlaylistResponse)
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
        if(request.getTitle() == null && !request.getTitle().isEmpty()) {
            playlist.setTitle(request.getTitle());
        }
        if(request.getDescription() == null && !request.getDescription().isEmpty()) {
            playlist.setDescription(request.getDescription());
        }
    }
    public Playlist toPlaylist(PlaylistCreationRequest request) {
        if(request == null) return null;
        Playlist playlist = new Playlist();
        playlist.setTitle(request.getTitle());
        playlist.setDescription(request.getDescription());
        return playlist;
    }
    public PlaylistResponse toPlaylistResponse(Playlist playlist) {
        if(playlist == null) return null;
        PlaylistResponse response = new PlaylistResponse();
        response.setId(playlist.getId());
        response.setTitle(playlist.getTitle());
        response.setDescription(playlist.getDescription());
        return response;
    }
}

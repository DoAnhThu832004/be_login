package com.devteria.identityservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.devteria.identityservice.dto.request.SongCreationRequest;
import com.devteria.identityservice.dto.request.SongUpdateRequest;
import com.devteria.identityservice.dto.response.SongResponse;
import com.devteria.identityservice.entity.Artist;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.entity.User;
import com.devteria.identityservice.entity.Year;
import com.devteria.identityservice.enums.SongType;
import com.devteria.identityservice.enums.Status;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.FavoriteRepository;
import com.devteria.identityservice.repository.SongRepository;
import com.devteria.identityservice.repository.UserRepository;
import org.hibernate.mapping.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SongService {
    private final SongRepository songRepository;
    private final YearService yearService;
    private final Cloudinary cloudinary;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public SongService(SongRepository songRepository, YearService yearService, Cloudinary cloudinary,FavoriteRepository favoriteRepository,UserRepository userRepository) {
        this.songRepository = songRepository;
        this.yearService = yearService;
        this.cloudinary = cloudinary;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public SongResponse createSong(SongCreationRequest request) {
        Song song = new Song();
        song.setName(request.getName());
        song.setDescription(request.getDescription());
        song.setDuration(request.getDuration());
        song.setReleasedDate(request.getReleasedDate());
        song.setStatus(Status.DRAFT);
        song.setType(SongType.AUDIO);
        Year year = yearService.createYear(request.getReleasedDate().getYear());
        song.setYear(year);
        song = songRepository.save(song);
        return toSongResponse(song);
    }
    public List<SongResponse> getSongs() {
        // 1. Lấy toàn bộ bài hát (1 truy vấn DB)
        List<Song> songs = songRepository.findAll();

        // 2. Lấy danh sách ID các bài hát user đã thích (1 truy vấn DB)
        Set<String> likedSongIds = getLikedSongIdsOfCurrentUser();

        // 3. Map dữ liệu trong bộ nhớ (Memory) - Không gọi DB nữa
        return songs.stream()
                .map(song -> {
                    SongResponse response = toSongResponse(song);
                    // Tra cứu trong Set cực nhanh (O(1))
                    response.setFavorite(likedSongIds.contains(song.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
    public SongResponse getSong(String id) {
        Song song = songRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.SONG_NOT_EXISTED));
        SongResponse response = toSongResponse(song);
        response.setFavorite(isSongLikedByCurrentUser(song));
        return response;
    }
    public SongResponse getSongByName(String name) {
        Song song = songRepository.findByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));

        SongResponse response = toSongResponse(song);
        response.setFavorite(isSongLikedByCurrentUser(song));
        return response;
    }
    public List<SongResponse> searchSongs(String key) {
        if(key == null || key.trim().isEmpty()) {
            return List.of();
        }
        List<Song> songs = songRepository.findByNameContainingIgnoreCase(key);
        Set<String> likedSongIds = getLikedSongIdsOfCurrentUser();
        return songs.stream()
                .map(song -> {
                    SongResponse response = toSongResponse(song);
                    response.setFavorite(likedSongIds.contains(song.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteSong(String id) {
        songRepository.findById(id)
                        .orElseThrow(()-> new AppException(ErrorCode.SONG_NOT_EXISTED));
        songRepository.deleteById(id);
    }
    @Transactional
    public SongResponse updateSong(String id, SongUpdateRequest request) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));
        song.setName(request.getName());
        song.setDescription(request.getDescription());
        song.setStatus(request.getStatus());
        song.setDuration(request.getDuration());
        song.setType(request.getType());
        if(request.getReleasedDate() != null) {
            song.setReleasedDate(request.getReleasedDate());
            Year year = yearService.createYear(request.getReleasedDate().getYear());
            song.setYear(year);
        }
        Song updateSong = songRepository.save(song);
        return toSongResponse(updateSong);
    }
    public Song uploadSongFile(String songId, MultipartFile image,MultipartFile audio) throws IOException {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));
        if (image == null || image.isEmpty() || audio == null || audio.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Map uploadImage = cloudinary.uploader().upload(image.getBytes(),
                ObjectUtils.asMap("folder", "music_app/images"));
        Map uploadAudio = cloudinary.uploader().upload(audio.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video", // Cloudinary yêu cầu kiểu này cho file audio/mp3
                        "folder", "music_app/audios"
                ));
        song.setImageUrl(uploadImage.get("secure_url").toString());
        song.setAudioUrl(uploadAudio.get("secure_url").toString());
        song.setStatus(Status.PUBLISHED);

        return songRepository.save(song);

    }
    public static SongResponse toSongResponse(Song song) {
        if (song == null) return null;
        SongResponse response = new SongResponse();
        response.setId(song.getId());
        response.setName(song.getName());
        response.setDescription(song.getDescription());
        response.setStatus(song.getStatus());
        response.setDuration(song.getDuration());
        response.setReleasedDate(song.getReleasedDate());
        response.setType(song.getType());
        response.setImageUrl(song.getImageUrl());
        response.setAudioUrl(song.getAudioUrl());
        response.setFavorite(false);
        if (song.getArtists() != null && !song.getArtists().isEmpty()) {
            // Cách 1: Sử dụng Stream API (Hiện đại, an toàn)
            Artist firstArtist = song.getArtists().stream().findFirst().orElse(null);

            if (firstArtist != null) {
                response.setArtistName(firstArtist.getName());
            }

            // Cách 2 (Truyền thống - Dùng Iterator):
            // response.setArtistName(song.getArtists().iterator().next().getName());
        }
        return response;
    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }
    private Set<String> getLikedSongIdsOfCurrentUser() {
        User user = getCurrentUser();
        if (user == null) {
            return Collections.emptySet();
        }
        return favoriteRepository.findAllByUser(user).stream()
                .map(favorite -> favorite.getSong().getId())
                .collect(Collectors.toSet());
    }
    private boolean isSongLikedByCurrentUser(Song song) {
        User user = getCurrentUser();
        if (user == null) return false;
        return favoriteRepository.existsByUserAndSong(user, song);
    }
}

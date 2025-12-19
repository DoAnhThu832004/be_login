package com.devteria.identityservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.devteria.identityservice.dto.request.SongCreationRequest;
import com.devteria.identityservice.dto.request.SongUpdateRequest;
import com.devteria.identityservice.dto.response.SongResponse;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.entity.Year;
import com.devteria.identityservice.enums.SongType;
import com.devteria.identityservice.enums.Status;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.SongRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class SongService {
    private final SongRepository songRepository;
    private final YearService yearService;
    private final Cloudinary cloudinary;

    public SongService(SongRepository songRepository, YearService yearService, Cloudinary cloudinary) {
        this.songRepository = songRepository;
        this.yearService = yearService;
        this.cloudinary = cloudinary;
    }

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
        return songRepository.findAll()
                .stream()
                .map(SongService::toSongResponse)
                .toList();
    }
    public SongResponse getSong(String id) {
        Song song = songRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.SONG_NOT_EXISTED));
        return toSongResponse(song);
    }
    public SongResponse getSongByName(String name) {
        Song song = songRepository.findByName(name)
                .orElseThrow(()-> new AppException(ErrorCode.SONG_NOT_EXISTED));
        return toSongResponse(song);
    }
    public void deleteSong(String id) {
        songRepository.deleteById(id);
    }
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
        return toSongResponse(songRepository.save(song));
    }
    public Song uploadSongFile(String songId, MultipartFile image,MultipartFile audio) throws IOException {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));
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
        return response;
    }
}

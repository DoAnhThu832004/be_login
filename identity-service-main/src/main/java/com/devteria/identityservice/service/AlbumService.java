package com.devteria.identityservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.devteria.identityservice.dto.request.AlbumCreationRequest;
import com.devteria.identityservice.dto.request.AlbumUpdateRequest;
import com.devteria.identityservice.dto.request.PlaylistUpdateRequest;
import com.devteria.identityservice.dto.response.AlbumResponse;
import com.devteria.identityservice.dto.response.PlaylistResponse;
import com.devteria.identityservice.dto.response.SongResponse;
import com.devteria.identityservice.entity.Album;
import com.devteria.identityservice.entity.Artist;
import com.devteria.identityservice.entity.Playlist;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.enums.Status;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.AlbumRepository;
import com.devteria.identityservice.repository.ArtistRepository;
import com.devteria.identityservice.repository.SongRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final Cloudinary cloudinary;

    public AlbumService(AlbumRepository albumRepository,SongRepository songRepository,ArtistRepository artistRepository,Cloudinary cloudinary) {
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
        this.cloudinary = cloudinary;
    }
    public AlbumResponse createAlbum(AlbumCreationRequest request) {
        Album album = toAlbum(request);
        album.setStatus(Status.DRAFT);
        return toAlbumResponse(albumRepository.save(album));
    }
    public List<AlbumResponse> getAlbums() {
        return albumRepository.findAll().stream()
                .map(AlbumService::toAlbumResponse)
                .toList();
    }
    public AlbumResponse getAlbum(String id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        return toAlbumResponse(album);
    }
    public AlbumResponse updateAlbum(String id, AlbumUpdateRequest request) {
        Album album = albumRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        mapRequestToAlbum(album,request);
        return toAlbumResponse(albumRepository.save(album));
    }
    public void deleteAlbum(String id) {
        albumRepository.deleteById(id);
    }
    public void addSongToAlbum(String albumId, String songId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));
        song.setAlbum(album);
        album.getSongs().add(song);
        songRepository.save(song);
    }
    public void removeSongToAlbum(String albumId, String songId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));
        album.getSongs().remove(song);
        song.setAlbum(null);
        songRepository.save(song);
    }
    public void addArtistToAlbum(String albumId,String artistId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(()-> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(()-> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        artist.addAlbum(album);
        albumRepository.save(album);
    }
    public void removeArtistToAlbum(String albumId,String artistId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(()-> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(()-> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        artist.removeAlbum(album);
        albumRepository.save(album);
    }
    public Album uploadAlbumFiles(String albumId, MultipartFile image) throws IOException {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));
        Map uploadImage = cloudinary.uploader().upload(image.getBytes(),
                ObjectUtils.asMap("folder", "album_app/images"));

        album.setImageUrlA(uploadImage.get("secure_url").toString());
        album.setStatus(Status.PUBLISHED);

        return albumRepository.save(album);
    }
    private void mapRequestToAlbum(Album album, AlbumUpdateRequest request) {
        if(request == null) return;
        if(request.getName() != null && !request.getName().isEmpty()) {
            album.setName(request.getName());
        }
        if(request.getDescription() != null && !request.getDescription().isEmpty()) {
            album.setDescription(request.getDescription());
        }
        if(request.getStatus() != null) {
            album.setStatus(request.getStatus());
        }
    }
    public Album toAlbum(AlbumCreationRequest request) {
        if(request == null) return null;
        Album album = new Album();
        album.setName(request.getName());
        album.setDescription(request.getDescription());
        return album;
    }
    public static AlbumResponse toAlbumResponse(Album album) {
        if (album == null) return null;

        AlbumResponse response = new AlbumResponse();
        response.setId(album.getId());
        response.setName(album.getName());
        response.setDescription(album.getDescription());
        response.setStatus(album.getStatus());
        response.setImageUrlA(album.getImageUrlA());
        // Map songs -> SongResponse set
        if (album.getSongs() != null) {
            Set<SongResponse> songResponses = new HashSet<>();
            for (Song s : album.getSongs()) {
                SongResponse sr = SongService.toSongResponse(s);
                if (sr != null) songResponses.add(sr);
            }
            response.setSongs(songResponses);
        } else {
            response.setSongs(new HashSet<>());
        }
//        if (album.getArtist() != null) {
//            Set<String> artistNames = album.getArtist().stream()
//                    .map(Artist::getName) // Giả sử entity Artist có phương thức getName()
//                    .collect(Collectors.toSet());
//            response.setArtistNames(artistNames);
//        } else {
//            response.setArtistNames(new HashSet<>());
//        }

        return response;
    }
}

package com.devteria.identityservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.devteria.identityservice.dto.request.AlbumUpdateRequest;
import com.devteria.identityservice.dto.request.ArtistCreationRequest;
import com.devteria.identityservice.dto.request.ArtistUpdateRequest;
import com.devteria.identityservice.dto.response.AlbumResponse;
import com.devteria.identityservice.dto.response.ArtistResponse;
import com.devteria.identityservice.dto.response.SongResponse;
import com.devteria.identityservice.entity.Album;
import com.devteria.identityservice.entity.Artist;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.AlbumRepository;
import com.devteria.identityservice.repository.ArtistRepository;
import com.devteria.identityservice.repository.GenreRepository;
import com.devteria.identityservice.repository.SongRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ArtistService {
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final GenreRepository genreRepository;
    private final AlbumRepository albumRepository;
    private final Cloudinary cloudinary;

    public ArtistService(ArtistRepository artistRepository, SongRepository songRepository, GenreRepository genreRepository,AlbumRepository albumRepository,Cloudinary cloudinary) {
        this.artistRepository = artistRepository;
        this.songRepository = songRepository;
        this.genreRepository = genreRepository;
        this.albumRepository = albumRepository;
        this.cloudinary = cloudinary;
    }
    public ArtistResponse createArtist(ArtistCreationRequest request) {
        Artist artist = toArtist(request);
        return toArtistResponse(artistRepository.save(artist));
    }
    public List<ArtistResponse> getArtists() {
        return artistRepository.findAll().stream()
                .map(ArtistService::toArtistResponse)
                .toList();
    }
    public ArtistResponse getArtist(String id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        return toArtistResponse(artist);
    }
    public ArtistResponse getArtistName(String name) {
        Artist artist = artistRepository.findByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        return toArtistResponse(artist);
    }
    public ArtistResponse updateArtist(String id, ArtistUpdateRequest request) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        mapRequestToArtist(artist,request);
        return toArtistResponse(artistRepository.save(artist));
    }
    public void deleteArtist(String id) {
        artistRepository.deleteById(id);
    }
    public void addArtistToSong(String artistId, String songId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_EXISTED));
        artist.addSong(song);
        artistRepository.save(artist);
    }
    public void removeSongToArtist(String artistId,String songId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(()-> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        Song song = songRepository.findById(songId)
                .orElseThrow(()-> new AppException(ErrorCode.SONG_NOT_EXISTED));
        artist.removeSong(song);
        artistRepository.save(artist);
    }
    public void addAlbumToArtist(String artistId, String albumId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        artist.addAlbum(album);
        artistRepository.save(artist);
    }
    public void removeAlbumToArtist(String artistId, String albumId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_EXISTED));
        artist.removeAlbum(album);
        artistRepository.save(artist);
    }
    private void mapRequestToArtist(Artist artist, ArtistUpdateRequest request) {
        if(request == null) return;
        if(request.getName() != null && !request.getName().isEmpty()) {
            artist.setName(request.getName());
        }
        if(request.getDescription() != null && !request.getDescription().isEmpty()) {
            artist.setDescription(request.getDescription());
        }
    }
    public Artist toArtist(ArtistCreationRequest request) {
        if(request == null) return null;
        Artist artist = new Artist();
        artist.setName(request.getName());
        artist.setDescription(request.getDescription());
        return artist;
    }
    public static ArtistResponse toArtistResponse(Artist artist) {
        if (artist == null) return null;
        ArtistResponse response = new ArtistResponse();
        response.setId(artist.getId());
        response.setName(artist.getName());
        response.setDescription(artist.getDescription());
        response.setImageUrlAr(artist.getImageUrlAr());
        if (artist.getSong() != null) {
            Set<SongResponse> songResponses = new HashSet<>();
            for (Song song : artist.getSong()) {
                SongResponse sr = SongService.toSongResponse(song);
                if (sr != null) songResponses.add(sr);
            }
            response.setSongs(songResponses);
        } else {
            response.setSongs(new HashSet<>());
        }
        if (artist.getAlbums() != null) {
            Set<AlbumResponse> albumResponses = new HashSet<>();
            for (Album album : artist.getAlbums()) {
                AlbumResponse sr = AlbumService.toAlbumResponse(album);
                if (sr != null) albumResponses.add(sr);
            }
            response.setAlbums(albumResponses);
        } else {
            response.setAlbums(new HashSet<>());
        }
        return response;
    }
    public Artist uploadArtistFiles(String artistId, MultipartFile image) throws IOException {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_EXISTED));
        Map uploadImage = cloudinary.uploader().upload(image.getBytes(),
                ObjectUtils.asMap("folder", "artist_app/images"));

        artist.setImageUrlAr(uploadImage.get("secure_url").toString());

        return artistRepository.save(artist);
    }
}

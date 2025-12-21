package com.devteria.identityservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.devteria.identityservice.dto.request.AlbumUpdateRequest;
import com.devteria.identityservice.dto.request.ArtistCreationRequest;
import com.devteria.identityservice.dto.request.ArtistUpdateRequest;
import com.devteria.identityservice.dto.response.AlbumResponse;
import com.devteria.identityservice.dto.response.ArtistResponse;
import com.devteria.identityservice.entity.Album;
import com.devteria.identityservice.entity.Artist;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.ArtistRepository;
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
    private final Cloudinary cloudinary;

    public ArtistService(ArtistRepository artistRepository, Cloudinary cloudinary) {
        this.artistRepository = artistRepository;
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

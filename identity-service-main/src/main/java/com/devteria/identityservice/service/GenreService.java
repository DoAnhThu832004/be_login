package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.GenreCreationRequest;
import com.devteria.identityservice.dto.request.GenreUpdateRequest;
import com.devteria.identityservice.dto.response.GenreResponse;
import com.devteria.identityservice.entity.Genre;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.GenreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }
    public GenreResponse createGenre(GenreCreationRequest request) {
        Genre genre = toGenre(request);
        return toGenreResponse(genreRepository.save(genre));
    }
    public List<GenreResponse> getGenres() {
        return genreRepository.findAll().stream()
                .map(GenreService::toGenreResponse)
                .toList();
    }
    public GenreResponse getGenre(String id) {
        return toGenreResponse(genreRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.GENRE_NOT_EXISTED)));
    }
    public GenreResponse getGenreByName(String name) {
        return toGenreResponse(genreRepository.findByName(name).orElseThrow(()-> new AppException(ErrorCode.GENRE_NOT_EXISTED)));
    }
    public GenreResponse getGenreByKeyG(String keyG) {
        return toGenreResponse(genreRepository.findByKeyG(keyG).orElseThrow(()-> new AppException(ErrorCode.GENRE_NOT_EXISTED)));
    }
    public void deleteGenre(String id) {
        genreRepository.deleteById(id);
    }
    public GenreResponse updateGenre(String id, GenreUpdateRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.GENRE_NOT_EXISTED));
        mapRequestToGenre(genre,request);
        return toGenreResponse(genreRepository.save(genre));
    }
    private void mapRequestToGenre(Genre genre, GenreUpdateRequest request) {
        if (request == null) return;
        if (request.getName() != null && !request.getName().isEmpty()) {
            genre.setName(request.getName());
        }

        if (request.getKeyG() != null && !request.getKeyG().isEmpty()) {
            genre.setKeyG(request.getKeyG());
        }
    }
    public Genre toGenre(GenreCreationRequest request) {
        if(request == null) return null;
        Genre genre = new Genre();
        genre.setName(request.getName());
        genre.setKeyG(request.getKeyG());
        return genre;
    }
    public static GenreResponse toGenreResponse(Genre genre) {
        if(genre == null) return null;
        GenreResponse response = new GenreResponse();
        response.setId(genre.getId());
        response.setName(genre.getName());
        response.setKeyG(genre.getKeyG());
        return response;
    }
}

package com.devteria.identityservice.dto.response;

import java.util.List;

/**
 * DTO tổng hợp cho API trang chủ GET /api/recommendations/home.
 *
 * Chứa đầy đủ dữ liệu cần thiết để Frontend render trang chủ:
 * - recommendedSongs   : Top bài hát cá nhân hóa (Personalized) hoặc Trending (Cold Start)
 * - recommendedArtists : Top nghệ sĩ được suy ra từ kết quả gợi ý bài hát (Aggregation)
 * - recommendedAlbums  : Top album được suy ra từ kết quả gợi ý bài hát (Aggregation)
 * - recommendedPlaylists: Playlist hệ thống (Admin) liên quan đến thể loại user yêu thích
 *
 * source:
 * - PERSONALIZED       : Dựa trên lịch sử nghe nhạc cá nhân (Item-Based CF + MMR)
 * - COLD_START_GENRE   : User mới, trả về Trending theo thể loại yêu thích đã chọn
 * - COLD_START_GLOBAL  : User mới và chưa chọn thể loại, trả về Global Trending
 */
public class HomeRecommendationResponse {

    /**
     * Nguồn gốc của danh sách gợi ý.
     */
    private String source;

    /**
     * Top bài hát gợi ý (tối đa 10).
     */
    private List<SongResponse> recommendedSongs;

    /**
     * Top nghệ sĩ gợi ý (tối đa 5), được suy ra từ recommendedSongs.
     * Ví dụ: nếu nhiều bài trong top songs thuộc Sơn Tùng M-TP
     * thì nghệ sĩ này sẽ xuất hiện đầu tiên.
     */
    private List<ArtistResponse> recommendedArtists;

    /**
     * Top album gợi ý (tối đa 5), được suy ra từ recommendedSongs.
     */
    private List<AlbumResponse> recommendedAlbums;

    /**
     * Top playlist gợi ý (tối đa 5).
     * - Personalized: Playlist chứa nhiều bài trong top songs nhất.
     * - Cold Start: Playlist của Admin (System Playlist).
     */
    private List<PlaylistResponse> recommendedPlaylists;

    public HomeRecommendationResponse() {}

    public HomeRecommendationResponse(
            String source,
            List<SongResponse> recommendedSongs,
            List<ArtistResponse> recommendedArtists,
            List<AlbumResponse> recommendedAlbums,
            List<PlaylistResponse> recommendedPlaylists) {
        this.source = source;
        this.recommendedSongs = recommendedSongs;
        this.recommendedArtists = recommendedArtists;
        this.recommendedAlbums = recommendedAlbums;
        this.recommendedPlaylists = recommendedPlaylists;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public List<SongResponse> getRecommendedSongs() { return recommendedSongs; }
    public void setRecommendedSongs(List<SongResponse> recommendedSongs) { this.recommendedSongs = recommendedSongs; }

    public List<ArtistResponse> getRecommendedArtists() { return recommendedArtists; }
    public void setRecommendedArtists(List<ArtistResponse> recommendedArtists) { this.recommendedArtists = recommendedArtists; }

    public List<AlbumResponse> getRecommendedAlbums() { return recommendedAlbums; }
    public void setRecommendedAlbums(List<AlbumResponse> recommendedAlbums) { this.recommendedAlbums = recommendedAlbums; }

    public List<PlaylistResponse> getRecommendedPlaylists() { return recommendedPlaylists; }
    public void setRecommendedPlaylists(List<PlaylistResponse> recommendedPlaylists) { this.recommendedPlaylists = recommendedPlaylists; }
}

package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.response.GenreResponse;
import com.devteria.identityservice.dto.response.RecommendationResponse;
import com.devteria.identityservice.dto.response.SongResponse;
import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.entity.SongSimilarity;
import com.devteria.identityservice.entity.User;
import com.devteria.identityservice.entity.UserInteraction;
import com.devteria.identityservice.repository.SongRepository;
import com.devteria.identityservice.repository.SongSimilarityRepository;
import com.devteria.identityservice.repository.UserInteractionRepository;
import com.devteria.identityservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ===== LUỒNG ONLINE — Trả Về Gợi Ý Real-time =====
 *
 * Mục tiêu: Trả về danh sách gợi ý trong < 50ms bằng cách đọc từ bảng
 * song_similarity đã được tính toán sẵn bởi luồng Offline.
 *
 * Pipeline:
 * 2.1 Client gọi API GET /api/recommendations?userId=XYZ
 * 2.2 Cold Start Check: user chưa có lịch sử → trả về Trending
 * 2.3 Lấy User Profile: danh sách bài đã nghe + điểm tương tác
 * 2.4 Candidate Generation: tìm bài tương tự từ song_similarity
 * 2.5 Predicted Score: tính điểm dự đoán có trọng số
 * 2.6 MMR Re-ranking: cân bằng Relevance và Diversity
 * 2.7 Trả về Top K bài hát
 */
@Service
public class RecommendationEngineService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEngineService.class);

    /**
     * λ trong thuật toán MMR (Maximal Marginal Relevance).
     * λ = 0.7: ưu tiên 70% relevance, 30% diversity.
     * Tăng λ để ưu tiên điểm cao hơn, giảm λ để danh sách đa dạng hơn.
     */
    private static final double MMR_LAMBDA = 0.7;

    /**
     * Số lượng candidate tối đa lấy cho mỗi bài hát đã nghe.
     */
    private static final int CANDIDATES_PER_SONG = 20;

    private final UserRepository userRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final SongSimilarityRepository songSimilarityRepository;
    private final SongRepository songRepository;

    public RecommendationEngineService(
            UserRepository userRepository,
            UserInteractionRepository userInteractionRepository,
            SongSimilarityRepository songSimilarityRepository,
            SongRepository songRepository) {
        this.userRepository = userRepository;
        this.userInteractionRepository = userInteractionRepository;
        this.songSimilarityRepository = songSimilarityRepository;
        this.songRepository = songRepository;
    }

    // =========================================================
    // ENTRY POINT: Bước 2.1 — API Handler
    // =========================================================

    /**
     * Entry point chính của luồng Online.
     * Điều phối toàn bộ pipeline từ Cold Start đến MMR Re-ranking.
     *
     * @param userId ID của user cần gợi ý
     * @param limit  Số bài hát muốn trả về (default: 10)
     * @return RecommendationResponse chứa danh sách bài hát và metadata nguồn gợi ý
     */
    public RecommendationResponse getRecommendations(String userId, int limit) {
        log.info("=== [ONLINE] Bắt đầu lấy gợi ý cho user: {} ===", userId);

        // Bước 2.2: Cold Start Check
        boolean hasHistory = userInteractionRepository.existsByUserId(userId);
        if (!hasHistory) {
            log.info("  [COLD START] User {} chưa có lịch sử nghe nhạc.", userId);
            return handleColdStart(userId, limit);
        }

        return getPersonalizedRecommendations(userId, limit);
    }

    // =========================================================
    // BƯỚC 2.2: COLD START
    // =========================================================

    /**
     * Bước 2.2 — Xử lý Cold Start:
     * User chưa có lịch sử → trả về Trending theo thể loại yêu thích,
     * hoặc Global Trending nếu chưa chọn thể loại.
     */
    private RecommendationResponse handleColdStart(String userId, int limit) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Kiểm tra xem user đã chọn preferred genres khi đăng ký chưa
            if (user.getPreferredGenres() != null && !user.getPreferredGenres().isEmpty()) {
                log.info("  [COLD START] Trả về Trending theo {} thể loại yêu thích",
                        user.getPreferredGenres().size());

                // Lấy bài trending từ các genre yêu thích (gộp và deduplicate)
                Map<String, Song> songMap = new HashMap<>();
                for (var genre : user.getPreferredGenres()) {
                    List<Song> genreTrending = songRepository
                            .findTop10ByGenres_IdOrderByPlayCountDesc(genre.getId());
                    for (Song s : genreTrending) {
                        songMap.put(s.getId(), s);
                    }
                }

                List<Song> songs = new ArrayList<>(songMap.values());
                // Sắp xếp lại theo playCount và lấy top limit
                songs.sort(Comparator.comparingLong(s -> -(s.getPlayCount() != null ? s.getPlayCount() : 0L)));
                if (songs.size() > limit) songs = songs.subList(0, limit);

                return new RecommendationResponse("COLD_START_GENRE", toSongResponseList(songs));
            }
        }

        // Fallback: Global Trending
        log.info("  [COLD START] User chưa chọn thể loại. Trả về Global Trending.");
        List<Song> globalTrending = songRepository.findTop10ByOrderByPlayCountDesc();
        if (globalTrending.size() > limit) globalTrending = globalTrending.subList(0, limit);

        return new RecommendationResponse("COLD_START_GLOBAL", toSongResponseList(globalTrending));
    }

    // =========================================================
    // BƯỚC 2.3 → 2.7: PERSONALIZED RECOMMENDATIONS
    // =========================================================

    private RecommendationResponse getPersonalizedRecommendations(String userId, int limit) {

        // ---- Bước 2.3: Lấy User Profile ----
        List<UserInteraction> interactions = userInteractionRepository.findAllByUserId(userId);
        Map<String, Double> userProfile = buildUserProfile(interactions);
        Set<String> listenedSongs = userProfile.keySet();
        log.info("  [PROFILE] User đã tương tác với {} bài hát", listenedSongs.size());

        // ---- Bước 2.4: Candidate Generation ----
        Map<String, Double> candidateScores = generateCandidates(userProfile, listenedSongs);
        log.info("  [CANDIDATES] Tìm thấy {} bài hát ứng viên", candidateScores.size());

        if (candidateScores.isEmpty()) {
            // Không tìm thấy candidate nào trong song_similarity → fallback về trending
            log.warn("  [FALLBACK] Không có candidate. Có thể song_similarity chưa được tính. Trả về Trending.");
            List<Song> trending = songRepository.findTop10ByOrderByPlayCountDesc();
            return new RecommendationResponse("COLD_START_GLOBAL", toSongResponseList(trending));
        }

        // ---- Bước 2.5: Predicted Score (đã được tính trong generateCandidates) ----
        // Lấy thông tin đầy đủ của các bài hát candidate
        List<String> topCandidateIds = candidateScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit((long) limit * 5)  // Lấy gấp 5 lần để MMR có đủ để chọn
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Song> candidateSongs = songRepository.findByIdIn(topCandidateIds);

        // Tạo map để lookup nhanh theo ID
        Map<String, Song> songById = candidateSongs.stream()
                .collect(Collectors.toMap(Song::getId, s -> s));

        // ---- Bước 2.6: MMR Re-ranking ----
        log.info("  [MMR] Áp dụng MMR re-ranking với λ={}", MMR_LAMBDA);
        List<Song> rerankedSongs = mmrRerank(candidateScores, songById, limit);
        log.info("  [MMR] Chọn được {} bài sau re-ranking", rerankedSongs.size());

        // ---- Bước 2.7: Trả về kết quả ----
        return new RecommendationResponse("PERSONALIZED", toSongResponseList(rerankedSongs));
    }

    /**
     * Bước 2.3 — Xây dựng User Profile:
     * Gom nhóm các tương tác theo bài hát, tính tổng điểm.
     *
     * @return Map[songId → aggregatedScore]
     */
    private Map<String, Double> buildUserProfile(List<UserInteraction> interactions) {
        Map<String, Double> profile = new HashMap<>();
        for (UserInteraction ui : interactions) {
            if (ui.getSong() != null && ui.getRatingScore() != null) {
                String songId = ui.getSong().getId();
                profile.merge(songId, ui.getRatingScore().doubleValue(), Double::sum);
            }
        }
        return profile;
    }

    /**
     * Bước 2.4 + 2.5 — Candidate Generation + Predicted Score:
     * Với mỗi bài đã nghe, lấy các bài tương tự từ song_similarity.
     * Tính Predicted Score = Σ(userScore[heard] × similarity[heard][candidate])
     *                        / Σ(similarity[heard][candidate])
     *
     * @return Map[candidateSongId → predictedScore]
     */
    private Map<String, Double> generateCandidates(
            Map<String, Double> userProfile, Set<String> listenedSongs) {

        // numerator[songId] = Σ(userScore × similarity)
        Map<String, Double> numeratorMap = new HashMap<>();
        // denominator[songId] = Σ(|similarity|)
        Map<String, Double> denominatorMap = new HashMap<>();

        for (Map.Entry<String, Double> entry : userProfile.entrySet()) {
            String heardSongId = entry.getKey();
            double userScore = entry.getValue();

            // Lấy tất cả bài tương tự với heardSong từ bảng song_similarity
            List<SongSimilarity> similar = songSimilarityRepository
                    .findAllRelatedToSong(heardSongId);

            for (SongSimilarity ss : similar) {
                // Lấy ID của bài kia trong cặp (không phải bài đang xét)
                String candidateId = ss.getSongAId().equals(heardSongId)
                        ? ss.getSongBId()
                        : ss.getSongAId();

                // Loại bỏ bài user đã nghe rồi
                if (listenedSongs.contains(candidateId)) continue;

                double sim = ss.getSimilarityScore();
                numeratorMap.merge(candidateId, userScore * sim, Double::sum);
                denominatorMap.merge(candidateId, Math.abs(sim), Double::sum);
            }
        }

        // Tính predicted score cuối cùng
        Map<String, Double> predictedScores = new HashMap<>();
        for (String candidateId : numeratorMap.keySet()) {
            double denom = denominatorMap.getOrDefault(candidateId, 1.0);
            if (denom > 0) {
                predictedScores.put(candidateId, numeratorMap.get(candidateId) / denom);
            }
        }

        return predictedScores;
    }

    /**
     * Bước 2.6 — MMR Re-ranking (Maximal Marginal Relevance):
     * Chọn lần lượt bài có MMR score cao nhất.
     *
     * MMR(d) = λ × relevance(d) - (1-λ) × max_{s∈S} diversity_penalty(d, s)
     *
     * diversity_penalty(d, s) = Độ trùng lặp genre giữa bài d và bài s đã được chọn.
     * Công thức: |genres(d) ∩ genres(s)| / |genres(d) ∪ genres(s)| (Jaccard similarity)
     *
     * @param candidateScores Map[songId → predictedScore]
     * @param songById        Map để lookup Song entity theo ID
     * @param limit           Số bài hát muốn chọn
     * @return Danh sách bài đã được re-rank
     */
    private List<Song> mmrRerank(
            Map<String, Double> candidateScores,
            Map<String, Song> songById,
            int limit) {

        // Chuẩn hóa predicted score về [0, 1] để so sánh với Jaccard
        double maxScore = candidateScores.values().stream()
                .mapToDouble(Double::doubleValue).max().orElse(1.0);

        List<Song> selected = new ArrayList<>();
        Set<String> selectedIds = new HashSet<>();

        // Lấy pool ứng viên còn lại
        Set<String> remainingIds = new HashSet<>(songById.keySet());

        while (selected.size() < limit && !remainingIds.isEmpty()) {
            String bestId = null;
            double bestMmrScore = Double.NEGATIVE_INFINITY;

            for (String candidateId : remainingIds) {
                Song candidate = songById.get(candidateId);
                if (candidate == null) continue;

                // Relevance: normalized predicted score
                double relevance = candidateScores.getOrDefault(candidateId, 0.0) / maxScore;

                // Diversity: max Jaccard similarity với bài đã được chọn
                double maxSimilarityToSelected = 0.0;
                if (!selected.isEmpty()) {
                    for (Song s : selected) {
                        double jaccard = computeGenreJaccard(candidate, s);
                        maxSimilarityToSelected = Math.max(maxSimilarityToSelected, jaccard);
                    }
                }

                // MMR score
                double mmrScore = MMR_LAMBDA * relevance
                        - (1 - MMR_LAMBDA) * maxSimilarityToSelected;

                if (mmrScore > bestMmrScore) {
                    bestMmrScore = mmrScore;
                    bestId = candidateId;
                }
            }

            if (bestId != null) {
                Song chosenSong = songById.get(bestId);
                selected.add(chosenSong);
                selectedIds.add(bestId);
                remainingIds.remove(bestId);
                log.debug("  [MMR] Chọn bài: {} (MMR score: {:.4f})", chosenSong.getName(), bestMmrScore);
            } else {
                break;
            }
        }

        return selected;
    }

    /**
     * Tính Jaccard Similarity giữa 2 bài hát dựa trên tập genres.
     * Dùng để đo mức độ "trùng lặp thể loại" trong MMR.
     *
     * Jaccard(A, B) = |genres(A) ∩ genres(B)| / |genres(A) ∪ genres(B)|
     */
    private double computeGenreJaccard(Song a, Song b) {
        if (a.getGenres() == null || a.getGenres().isEmpty()
                || b.getGenres() == null || b.getGenres().isEmpty()) {
            return 0.0;
        }

        Set<String> genresA = a.getGenres().stream()
                .map(g -> g.getId()).collect(Collectors.toSet());
        Set<String> genresB = b.getGenres().stream()
                .map(g -> g.getId()).collect(Collectors.toSet());

        Set<String> intersection = new HashSet<>(genresA);
        intersection.retainAll(genresB);

        Set<String> union = new HashSet<>(genresA);
        union.addAll(genresB);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    // =========================================================
    // HELPER: Song Entity → SongResponse DTO
    // =========================================================

    /**
     * Chuyển đổi danh sách Song entity sang SongResponse DTO.
     */
    private List<SongResponse> toSongResponseList(List<Song> songs) {
        List<SongResponse> result = new ArrayList<>();
        for (Song song : songs) {
            SongResponse dto = new SongResponse();
            dto.setId(song.getId());
            dto.setName(song.getName());
            dto.setDescription(song.getDescription());
            dto.setDuration(song.getDuration());
            dto.setReleasedDate(song.getReleasedDate());
            dto.setImageUrl(song.getImageUrl());
            dto.setAudioUrl(song.getAudioUrl());
            dto.setPlayCount(song.getPlayCount());
            dto.setStatus(song.getStatus());
            dto.setType(song.getType());

            // Map tên nghệ sĩ đầu tiên nếu có
            if (song.getArtists() != null && !song.getArtists().isEmpty()) {
                String artistName = song.getArtists().iterator().next().getName();
                dto.setArtistName(artistName);
            }

            // Map danh sách genre
            if (song.getGenres() != null) {
                List<GenreResponse> genres = song.getGenres().stream()
                        .map(g -> {
                            GenreResponse gr = new GenreResponse();
                            gr.setId(g.getId());
                            gr.setName(g.getName());
                            gr.setKeyG(g.getKeyG());
                            return gr;
                        })
                        .collect(Collectors.toList());
                dto.setGenres(genres);
            }

            result.add(dto);
        }
        return result;
    }
}
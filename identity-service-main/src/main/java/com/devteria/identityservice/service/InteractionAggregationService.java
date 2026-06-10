package com.devteria.identityservice.service;

import com.devteria.identityservice.entity.Favorite;
import com.devteria.identityservice.entity.SongSimilarity;
import com.devteria.identityservice.entity.UserInteraction;
import com.devteria.identityservice.repository.DownloadedSongRepository;
import com.devteria.identityservice.repository.FavoriteRepository;
import com.devteria.identityservice.repository.SongSimilarityRepository;
import com.devteria.identityservice.repository.UserInteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ===== LUỒNG OFFLINE — Chạy Nền Ban Đêm =====
 *
 * Mục tiêu: Tính toán trước (pre-compute) độ tương đồng giữa tất cả cặp bài hát
 * và lưu vào bảng song_similarity. Khi API online gọi, chỉ cần đọc từ DB mà không
 * cần tính toán, đảm bảo thời gian phản hồi < 50ms.
 *
 * Lịch chạy: 2h sáng mỗi ngày (khi server ít tải nhất).
 *
 * Pipeline:
 * 1. Sync dữ liệu tương tác (PLAY=1đ, LIKE=3đ, DOWNLOAD=5đ) → bảng user_interactions
 * 2. Aggregate điểm theo (userId, songId)
 * 3. Mean-Centering: điểm_chuẩn = điểm_tổng - trung_bình_user
 * 4. Tính Adjusted Cosine Similarity cho mỗi cặp bài
 * 5. Batch Insert vào song_similarity (1000 records/batch)
 */
@Service
public class InteractionAggregationService {

    private static final Logger log = LoggerFactory.getLogger(InteractionAggregationService.class);

    // Thang điểm cho mỗi loại hành động
    private static final float SCORE_PLAY = 1.0f;
    private static final float SCORE_LIKE = 3.0f;
    private static final float SCORE_DOWNLOAD = 5.0f;

    // Ngưỡng tối thiểu: cặp bài phải có ít nhất 1 user nghe chung mới tính similarity.
    // Đặt = 1 để hoạt động ngay cả khi ít dữ liệu. Tăng lên 3+ khi đã có đủ user.
    private static final int MIN_COMMON_USERS = 1;

    // Chỉ lưu cặp có similarity score > ngưỡng này
    private static final double MIN_SIMILARITY_THRESHOLD = 0.1;

    // Số lượng records tối đa trong mỗi batch insert
    private static final int BATCH_SIZE = 1000;

    private final FavoriteRepository favoriteRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final SongSimilarityRepository songSimilarityRepository;
    private final DownloadedSongRepository downloadedSongRepository;

    public InteractionAggregationService(
            FavoriteRepository favoriteRepository,
            UserInteractionRepository userInteractionRepository,
            SongSimilarityRepository songSimilarityRepository,
            DownloadedSongRepository downloadedSongRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userInteractionRepository = userInteractionRepository;
        this.songSimilarityRepository = songSimilarityRepository;
        this.downloadedSongRepository = downloadedSongRepository;
    }

    // =========================================================
    // ENTRY POINT: Cron Job 2h sáng mỗi ngày
    // =========================================================

    /**
     * Bước 1.1 — Kích hoạt (Trigger):
     * Tự động chạy lúc 2h sáng mỗi ngày.
     * Cũng có thể gọi thủ công qua API admin.
     */
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void runNightlyRecommendationJob() {
        log.info("========================================================");
        log.info("  LUỒNG OFFLINE: Bắt đầu tác vụ tính toán gợi ý đêm nay");
        log.info("========================================================");
        long startTime = System.currentTimeMillis();

        try {
            // Phase 1: Sync raw interactions vào bảng user_interactions
            syncRawInteractions();

            // Phase 2-5: Tính similarity và lưu vào song_similarity
            computeAndStoreSimilarity();

        } catch (Exception e) {
            log.error("LỖI trong tác vụ offline recommendation!", e);
            throw e;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("========================================================");
        log.info("  LUỒNG OFFLINE: Hoàn tất sau {}ms (~{}s)", elapsed, elapsed / 1000);
        log.info("========================================================");
    }

    // =========================================================
    // PHASE 1: SYNC RAW INTERACTIONS
    // =========================================================

    /**
     * Bước 1.2 (phần 1) — Đồng bộ hóa dữ liệu thô:
     * Đọc từ bảng Favorites và DownloadedSongs rồi ghi vào user_interactions.
     * PLAY interactions được ghi trực tiếp bởi InteractionService khi user nghe nhạc.
     */
    @Transactional
    public void syncRawInteractions() {
        log.info("--- Phase 1: Sync raw interactions ---");

        // Sync LIKE từ bảng Favorites
        syncLikesFromFavorites();

        // Sync DOWNLOAD từ bảng DownloadedSongs
        syncDownloadsFromDownloadedSongs();

        log.info("--- Phase 1: Hoàn tất sync ---");
    }

    private void syncLikesFromFavorites() {
        // Xóa toàn bộ LIKE cũ để tính lại sạch
        userInteractionRepository.deleteByInteractionTypeOrNull("LIKE");

        List<Favorite> allFavorites = favoriteRepository.findAll();
        if (allFavorites.isEmpty()) {
            log.info("  [LIKE] Không có dữ liệu Favorites.");
            return;
        }

        List<UserInteraction> likeInteractions = new ArrayList<>();
        for (Favorite favorite : allFavorites) {
            UserInteraction interaction = new UserInteraction();
            interaction.setUser(favorite.getUser());
            interaction.setSong(favorite.getSong());
            interaction.setRatingScore(SCORE_LIKE);
            interaction.setInteractionType("LIKE");
            interaction.setUpdatedAt(LocalDateTime.now());
            likeInteractions.add(interaction);
        }

        userInteractionRepository.saveAll(likeInteractions);
        log.info("  [LIKE] Đã sync {} bản ghi LIKE từ Favorites", likeInteractions.size());
    }

    private void syncDownloadsFromDownloadedSongs() {
        // Xóa toàn bộ DOWNLOAD cũ để đồng bộ lại từ đầu (đảm bảo sạch và nhanh)
        userInteractionRepository.deleteByInteractionTypeOrNull("DOWNLOAD");

        var allDownloads = downloadedSongRepository.findAll();
        if (allDownloads.isEmpty()) {
            log.info("  [DOWNLOAD] Không có dữ liệu Downloads.");
            return;
        }

        List<UserInteraction> downloadInteractions = new ArrayList<>();
        for (var download : allDownloads) {
            UserInteraction interaction = new UserInteraction();
            interaction.setUser(download.getUser());
            interaction.setSong(download.getSong());
            interaction.setRatingScore(SCORE_DOWNLOAD);
            interaction.setInteractionType("DOWNLOAD");
            interaction.setUpdatedAt(download.getDownloadedAt());
            downloadInteractions.add(interaction);
        }

        // Batch Insert toàn bộ danh sách
        userInteractionRepository.saveAll(downloadInteractions);
        log.info("  [DOWNLOAD] Đã sync {} bản ghi DOWNLOAD thành công", downloadInteractions.size());
    }

    // =========================================================
    // PHASE 2-5: COMPUTE SIMILARITY
    // =========================================================

    /**
     * Tính toán toàn bộ ma trận similarity và lưu vào DB.
     * Có thể gọi độc lập qua API admin để tái tính mà không cần sync lại.
     */
    @Transactional
    public void computeAndStoreSimilarity() {
        log.info("--- Phase 2: Aggregation ---");

        // Bước 1.2: Aggregate điểm theo (userId, songId)
        Map<String, Map<String, Double>> userSongMatrix = aggregateScores();

        if (userSongMatrix.isEmpty()) {
            log.warn("Ma trận tương tác rỗng. Dừng tính toán similarity.");
            return;
        }
        log.info("  Tổng số users trong ma trận: {}", userSongMatrix.size());

        // Bước 1.3: Mean-Centering
        log.info("--- Phase 3: Mean-Centering ---");
        Map<String, Map<String, Double>> centeredMatrix = computeMeanCenteredMatrix(userSongMatrix);

        // Bước 1.4: Tính Adjusted Cosine Similarity
        log.info("--- Phase 4: Tính Adjusted Cosine Similarity ---");
        List<SongSimilarity> similarities = computeAdjustedCosineSimilarity(centeredMatrix);
        log.info("  Tìm thấy {} cặp bài có similarity > {}", similarities.size(), MIN_SIMILARITY_THRESHOLD);

        // Bước 1.5: Lưu vào DB bằng Batch Insert
        log.info("--- Phase 5: Batch Insert vào song_similarity ---");
        batchInsertSimilarities(similarities);
    }

    /**
     * Bước 1.2 — Gom nhóm dữ liệu (Aggregation):
     * Quét bảng user_interactions và tính tổng điểm theo (userId, songId).
     * Điểm được tính theo trọng số loại hành động.
     *
     * @return Map[userId → Map[songId → totalScore]]
     */
    private Map<String, Map<String, Double>> aggregateScores() {
        List<Object[]> rawData = userInteractionRepository.findAllRawInteractionData();
        Map<String, Map<String, Double>> matrix = new HashMap<>();

        for (Object[] row : rawData) {
            String userId = (String) row[0];
            String songId = (String) row[1];
            Float rawScore = row[2] != null ? ((Number) row[2]).floatValue() : 0f;
            String interactionType = (String) row[3];

            // Áp dụng trọng số theo loại tương tác
            double weightedScore = applyInteractionWeight(rawScore, interactionType);

            matrix.computeIfAbsent(userId, k -> new HashMap<>())
                    .merge(songId, weightedScore, Double::sum);
        }

        return matrix;
    }

    /**
     * Áp dụng trọng số hành động lên điểm thô.
     * PLAY=1đ, LIKE=3đ, DOWNLOAD=5đ.
     */
    private double applyInteractionWeight(float rawScore, String interactionType) {
        if (interactionType == null) return rawScore;
        return switch (interactionType.toUpperCase()) {
            case "PLAY", "LISTEN" -> SCORE_PLAY;
            case "LIKE" -> SCORE_LIKE;
            case "DOWNLOAD" -> SCORE_DOWNLOAD;
            default -> rawScore;
        };
    }

    /**
     * Bước 1.3 — Chuẩn hóa dữ liệu (Mean-Centering):
     * Tính điểm trung bình của mỗi user, sau đó lấy mỗi điểm trừ đi trung bình.
     * Loại bỏ bias giữa user "dễ dãi" và user "khó tính".
     *
     * @return Ma trận điểm đã chuẩn hóa: Map[userId → Map[songId → centeredScore]]
     */
    private Map<String, Map<String, Double>> computeMeanCenteredMatrix(
            Map<String, Map<String, Double>> userSongMatrix) {

        Map<String, Map<String, Double>> centeredMatrix = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> userEntry : userSongMatrix.entrySet()) {
            String userId = userEntry.getKey();
            Map<String, Double> songScores = userEntry.getValue();

            // Tính trung bình điểm của user này
            double mean = songScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            // Trừ trung bình để chuẩn hóa
            Map<String, Double> centeredScores = new HashMap<>();
            for (Map.Entry<String, Double> songEntry : songScores.entrySet()) {
                centeredScores.put(songEntry.getKey(), songEntry.getValue() - mean);
            }

            centeredMatrix.put(userId, centeredScores);
        }

        return centeredMatrix;
    }

    /**
     * Bước 1.4 — Tính độ tương đồng (Adjusted Cosine Similarity):
     * So sánh mọi cặp bài hát dựa trên vector điểm chuẩn hóa của những user
     * đã nghe CẢ HAI bài.
     *
     * Công thức:
     *   sim(A,B) = Σ(r_uA * r_uB) / sqrt(Σ(r_uA²) * Σ(r_uB²))
     * trong đó r_uX là điểm mean-centered của user u với bài X.
     *
     * Ngưỡng tương tác: bỏ qua cặp có < MIN_COMMON_USERS người nghe chung.
     */
    private List<SongSimilarity> computeAdjustedCosineSimilarity(
            Map<String, Map<String, Double>> centeredMatrix) {

        // Đảo chiều ma trận: songId → Map[userId → centeredScore]
        Map<String, Map<String, Double>> songUserMatrix = invertMatrix(centeredMatrix);
        List<String> allSongIds = new ArrayList<>(songUserMatrix.keySet());
        int totalSongs = allSongIds.size();
        log.info("  Tổng số bài hát trong ma trận: {}. Số cặp cần kiểm tra: ~{}",
                totalSongs, (long) totalSongs * (totalSongs - 1) / 2);

        List<SongSimilarity> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < totalSongs - 1; i++) {
            String songA = allSongIds.get(i);
            Map<String, Double> ratingsA = songUserMatrix.get(songA);

            for (int j = i + 1; j < totalSongs; j++) {
                String songB = allSongIds.get(j);
                Map<String, Double> ratingsB = songUserMatrix.get(songB);

                // Tìm tập users đã nghe CẢ HAI bài
                Set<String> commonUsers = new HashSet<>(ratingsA.keySet());
                commonUsers.retainAll(ratingsB.keySet());

                // Áp dụng ngưỡng tương tác
                if (commonUsers.size() < MIN_COMMON_USERS) continue;

                // Tính Adjusted Cosine
                double numerator = 0.0;
                double normA = 0.0;
                double normB = 0.0;

                for (String userId : commonUsers) {
                    double rA = ratingsA.get(userId);
                    double rB = ratingsB.get(userId);
                    numerator += rA * rB;
                    normA += rA * rA;
                    normB += rB * rB;
                }

                // Tránh chia cho 0
                if (normA == 0.0 || normB == 0.0) continue;

                double similarity = numerator / (Math.sqrt(normA) * Math.sqrt(normB));

                // Chỉ lưu cặp có similarity > ngưỡng
                if (similarity > MIN_SIMILARITY_THRESHOLD) {
                    results.add(new SongSimilarity(
                            songA, songB, similarity, commonUsers.size(), now));
                }
            }

            // Log tiến độ mỗi 100 bài
            if (i > 0 && i % 100 == 0) {
                log.info("  Tiến độ: {}/{} bài hát đã xử lý, {} cặp tìm thấy",
                        i, totalSongs, results.size());
            }
        }

        return results;
    }

    /**
     * Đảo chiều ma trận từ [userId → songId → score] thành [songId → userId → score].
     */
    private Map<String, Map<String, Double>> invertMatrix(Map<String, Map<String, Double>> userSongMatrix) {
        Map<String, Map<String, Double>> songUserMatrix = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> userEntry : userSongMatrix.entrySet()) {
            String userId = userEntry.getKey();
            for (Map.Entry<String, Double> songEntry : userEntry.getValue().entrySet()) {
                String songId = songEntry.getKey();
                double score = songEntry.getValue();
                songUserMatrix.computeIfAbsent(songId, k -> new HashMap<>()).put(userId, score);
            }
        }
        return songUserMatrix;
    }

    /**
     * Bước 1.5 — Lưu trữ (Persistence):
     * Xóa dữ liệu cũ trong transaction riêng, sau đó insert theo batch BATCH_SIZE records.
     *
     * Lý do tách transaction:
     * - truncateTable() dùng native DELETE, cần flush trước khi Hibernate
     *   thấy bảng sạch; nếu nằm trong cùng transaction với saveAll() sẽ
     *   gây xung đột Hibernate session cache (entity đã bị xóa nhưng vẫn
     *   được track → OptimisticLockException hoặc duplicate key).
     * - REQUIRES_NEW đảm bảo mỗi batch được commit độc lập, giúp tránh
     *   OutOfMemoryError khi insert số lượng lớn.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchInsertSimilarities(List<SongSimilarity> similarities) {
        // Xóa toàn bộ dữ liệu cũ và flush ngay để Hibernate nhận biết
        songSimilarityRepository.truncateTable();
        songSimilarityRepository.flush();
        log.info("  Đã xóa dữ liệu similarity cũ.");

        if (similarities.isEmpty()) {
            log.warn("  Danh sách similarity rỗng — bảng song_similarity sẽ trống.");
            log.warn("  Kiểm tra: có đủ user tương tác? MIN_COMMON_USERS={}, MIN_SIMILARITY_THRESHOLD={}",
                    MIN_COMMON_USERS, MIN_SIMILARITY_THRESHOLD);
            return;
        }

        int totalInserted = 0;
        int batchStart = 0;

        while (batchStart < similarities.size()) {
            int batchEnd = Math.min(batchStart + BATCH_SIZE, similarities.size());
            List<SongSimilarity> batch = similarities.subList(batchStart, batchEnd);

            songSimilarityRepository.saveAll(batch);
            songSimilarityRepository.flush();

            totalInserted += batch.size();
            batchStart = batchEnd;
            log.info("  Đã insert {}/{} cặp bài hát", totalInserted, similarities.size());
        }

        log.info("  Batch Insert hoàn tất: {} cặp bài hát đã được lưu.", totalInserted);
    }
}
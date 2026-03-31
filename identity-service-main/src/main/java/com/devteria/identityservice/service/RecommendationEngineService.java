package com.devteria.identityservice.service;

import com.devteria.identityservice.entity.Song;
import com.devteria.identityservice.repository.SongRepository;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.MemoryIDMigrator;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RecommendationEngineService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEngineService.class);

    private final JdbcTemplate jdbcTemplate;
    private final SongRepository songRepository;
    private final MemoryIDMigrator idMigrator;

    public RecommendationEngineService(JdbcTemplate jdbcTemplate, SongRepository songRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.songRepository = songRepository;
        this.idMigrator = new MemoryIDMigrator();
    }

    public List<Song> getPersonalizedRecommendations(String userUuid, int recommendationCount) {
        List<Song> recommendedSongs = new ArrayList<>();
        try {
            log.info("=== BẮT ĐẦU CHẠY THUẬT TOÁN GỢI Ý ===");

            // Bước 1: Xây dựng không gian dữ liệu
            DataModel dataModel = constructDataModel();
            log.info("Radar 1: Tổng số User trong Ma trận: {}", dataModel.getNumUsers());
            log.info("Radar 2: Tổng số Item trong Ma trận: {}", dataModel.getNumItems());

            if (dataModel.getNumUsers() == 0) {
                log.warn("CẢNH BÁO: Ma trận rỗng! Hãy chắc chắn bạn đã chạy API trigger-aggregation trước.");
                return recommendedSongs;
            }

            // Bước 2: Sử dụng LogLikelihood thay vì Pearson (Giải quyết triệt để lỗi đồng nhất điểm 5.0)
            ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(dataModel);
            Recommender recommender = new GenericItemBasedRecommender(dataModel, itemSimilarity);

            // Bước 3: Chuẩn hóa chuỗi đầu vào (tránh lỗi dư khoảng trắng hoặc khác biệt chữ hoa/thường)
            String normalizedUuid = userUuid.trim().toLowerCase();
            long mappedUserId = idMigrator.toLongID(normalizedUuid);
            log.info("Radar 3: User UUID truyền vào là [{}], mã hóa thành Long ID [{}]", normalizedUuid, mappedUserId);

            // Bước 4: Xác minh User có tồn tại trong Ma trận không
            try {
                PreferenceArray userPrefs = dataModel.getPreferencesFromUser(mappedUserId);
                log.info("Radar 4: Tìm thấy User trong hệ thống. User này đã thích {} bài hát.", userPrefs.length());
            } catch (NoSuchUserException e) {
                log.error("LỖI CHÍNH MẠNG: User UUID bạn nhập trên Postman KHÔNG HỀ CÓ bất kỳ bài hát yêu thích nào trong bảng user_interactions. Thuật toán từ chối dự đoán!");
                return recommendedSongs;
            }

            // Bước 5: Yêu cầu thuật toán dự đoán
            List<RecommendedItem> recommendations = recommender.recommend(mappedUserId, recommendationCount);
            log.info("Radar 5: Thuật toán nội suy thành công, tìm thấy {} kết quả phù hợp.", recommendations.size());

            // Bước 6: Dịch ngược kết quả
            for (RecommendedItem recommendation : recommendations) {
                String originalSongUuid = idMigrator.toStringID(recommendation.getItemID());
                log.info(" -> Đề xuất Item ID đã giải mã: {} (Độ tin cậy: {})", originalSongUuid, recommendation.getValue());

                if (originalSongUuid != null) {
                    Optional<Song> songOptional = songRepository.findById(originalSongUuid);
                    songOptional.ifPresent(recommendedSongs::add);
                }
            }

            log.info("=== KẾT THÚC TIẾN TRÌNH ===");

        } catch (TasteException exception) {
            log.error("Xảy ra lỗi toán học sâu trong Mahout", exception);
        }

        return recommendedSongs;
    }

    private DataModel constructDataModel() {
        String sqlQuery = "SELECT user_id, song_id, rating_score FROM user_interactions";
        List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(sqlQuery);

        FastByIDMap<PreferenceArray> userPreferencesMap = new FastByIDMap<>();

        for (Map<String, Object> rowData : queryResults) {
            // Chuyển in thường toàn bộ để đồng bộ hóa mã băm (Hash)
            String userIdString = ((String) rowData.get("user_id")).trim().toLowerCase();
            String songIdString = ((String) rowData.get("song_id")).trim().toLowerCase();
            float ratingValue = ((Number) rowData.get("rating_score")).floatValue();

            long numericalUserId = idMigrator.toLongID(userIdString);
            long numericalSongId = idMigrator.toLongID(songIdString);

            idMigrator.storeMapping(numericalUserId, userIdString);
            idMigrator.storeMapping(numericalSongId, songIdString);

            PreferenceArray existingPreferences = userPreferencesMap.get(numericalUserId);

            if (existingPreferences == null) {
                existingPreferences = new GenericUserPreferenceArray(1);
                existingPreferences.setUserID(0, numericalUserId);
                existingPreferences.setItemID(0, numericalSongId);
                existingPreferences.setValue(0, ratingValue);
                userPreferencesMap.put(numericalUserId, existingPreferences);
            } else {
                PreferenceArray expandedPreferences = new GenericUserPreferenceArray(existingPreferences.length() + 1);
                for (int index = 0; index < existingPreferences.length(); index++) {
                    expandedPreferences.setUserID(index, existingPreferences.getUserID(index));
                    expandedPreferences.setItemID(index, existingPreferences.getItemID(index));
                    expandedPreferences.setValue(index, existingPreferences.getValue(index));
                }
                expandedPreferences.setUserID(existingPreferences.length(), numericalUserId);
                expandedPreferences.setItemID(existingPreferences.length(), numericalSongId);
                expandedPreferences.setValue(existingPreferences.length(), ratingValue);
                userPreferencesMap.put(numericalUserId, expandedPreferences);
            }
        }

        return new GenericDataModel(userPreferencesMap);
    }
}
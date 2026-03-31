package com.devteria.identityservice.service;

import com.devteria.identityservice.entity.Favorite;
import com.devteria.identityservice.entity.UserInteraction;
import com.devteria.identityservice.repository.FavoriteRepository;
import com.devteria.identityservice.repository.UserInteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InteractionAggregationService {

    private static final Logger log = LoggerFactory.getLogger(InteractionAggregationService.class);

    private final FavoriteRepository favoriteRepository;
    private final UserInteractionRepository userInteractionRepository;

    public InteractionAggregationService(
            FavoriteRepository favoriteRepository,
            UserInteractionRepository userInteractionRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userInteractionRepository = userInteractionRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void calculateAndStoreInteractions() {
        log.info("Khởi động tiến trình đồng bộ hóa ma trận tương tác...");
        userInteractionRepository.deleteAll();
        List<Favorite> allFavorites = favoriteRepository.findAll();

        if (allFavorites.isEmpty()) {
            log.warn("Không gian dữ liệu rỗng. Dừng tiến trình tổng hợp.");
            return;
        }
        for (Favorite favorite : allFavorites) {
            UserInteraction interaction = new UserInteraction();
            interaction.setUser(favorite.getUser());
            interaction.setSong(favorite.getSong());
            interaction.setRatingScore(5.0f);

            userInteractionRepository.save(interaction);
        }
        log.info("Hoàn tất tái cấu trúc ma trận với {} điểm dữ liệu", allFavorites.size());
    }
}
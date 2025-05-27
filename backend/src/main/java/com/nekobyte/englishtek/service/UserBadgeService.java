package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.model.Badge;
import com.nekobyte.englishtek.model.User;
import com.nekobyte.englishtek.model.UserBadge;
import com.nekobyte.englishtek.repository.BadgeRepository;
import com.nekobyte.englishtek.repository.UserBadgeRepository;
import com.nekobyte.englishtek.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserBadgeService {
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Transactional
    public void addBadgeToUser(User user, Badge badge) {
        // Refresh the badge from the database to ensure it's managed
        Badge managedBadge = badgeRepository.findById(badge.getId())
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));
        
        // Check if user already has this badge
        if (userBadgeRepository.findByUserAndBadge(user, managedBadge).isPresent()) {
            return; // User already has this badge
        }

        // Create new UserBadge
        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(managedBadge);
        userBadgeRepository.save(userBadge);
    }

    public int countUserBadges(User user) {
        return userBadgeRepository.countByUserId(user.getId());
    }
}

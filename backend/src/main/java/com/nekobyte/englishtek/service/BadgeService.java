package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.dto.badge.BadgeResponse;
import com.nekobyte.englishtek.dto.badge.UserBadgeResponse;
import com.nekobyte.englishtek.model.Badge;
import com.nekobyte.englishtek.model.User;
import com.nekobyte.englishtek.model.UserBadge;
import com.nekobyte.englishtek.repository.BadgeRepository;
import com.nekobyte.englishtek.repository.UserBadgeRepository;
import com.nekobyte.englishtek.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {
    private final BadgeRepository badgeRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;

    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BadgeResponse getBadge(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));
        return mapToResponse(badge);
    }

    @Transactional
    public BadgeResponse createBadge(String name, String description, MultipartFile icon) {
        Badge badge = new Badge();
        badge.setName(name);
        badge.setDescription(description);

        if (icon != null && !icon.isEmpty()) {
            String fileName = fileStorageService.storeFile(icon);
            badge.setIconUrl(fileName);
        }

        badge = badgeRepository.save(badge);
        return mapToResponse(badge);
    }

    @Transactional
    public BadgeResponse updateBadge(Long id, String name, String description, MultipartFile icon) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));

        if (name != null) {
            badge.setName(name);
        }
        if (description != null) {
            badge.setDescription(description);
        }
        if (icon != null && !icon.isEmpty()) {
            // Delete old icon if it exists
            if (badge.getIconUrl() != null) {
                fileStorageService.deleteFile(badge.getIconUrl());
            }
            String fileName = fileStorageService.storeFile(icon);
            badge.setIconUrl(fileName);
        }

        badge = badgeRepository.save(badge);
        return mapToResponse(badge);
    }

    @Transactional
    public void deleteBadge(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));
        
        // Delete icon file if it exists
        if (badge.getIconUrl() != null) {
            fileStorageService.deleteFile(badge.getIconUrl());
        }
        
        badgeRepository.delete(badge);
    }

    public List<UserBadgeResponse> getMyBadges() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("Current user not found"));
            
        return userBadgeRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToUserBadgeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void awardBadge(User user, Badge badge) {
        // Check if user already has this badge
        if (userBadgeRepository.findByUserAndBadge(user, badge).isPresent()) {
            return; // User already has this badge
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setDateObtained(LocalDateTime.now());
        userBadgeRepository.save(userBadge);
    }

    private BadgeResponse mapToResponse(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl() != null
                    ? "/api/v1/files/" + badge.getIconUrl()
                    : null)
                .build();
    }

    private UserBadgeResponse mapToUserBadgeResponse(UserBadge userBadge) {
        return UserBadgeResponse.builder()
                .id(userBadge.getBadge().getId())
                .name(userBadge.getBadge().getName())
                .description(userBadge.getBadge().getDescription())
                .iconUrl(userBadge.getBadge().getIconUrl() != null
                    ? "/api/v1/files/" + userBadge.getBadge().getIconUrl()
                    : null)
                .dateObtained(userBadge.getDateObtained())
                .build();
    }
}

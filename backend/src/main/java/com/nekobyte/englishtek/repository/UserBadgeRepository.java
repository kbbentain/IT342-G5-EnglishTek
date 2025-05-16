package com.nekobyte.englishtek.repository;

import com.nekobyte.englishtek.model.Badge;
import com.nekobyte.englishtek.model.User;
import com.nekobyte.englishtek.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    Optional<UserBadge> findByUserAndBadge(User user, Badge badge);
    List<UserBadge> findByUserId(Long userId);
    int countByUserId(Long userId);
    void deleteByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserBadge ub WHERE ub.badge.id = :badgeId")
    void deleteByBadgeId(@Param("badgeId") Long badgeId);
}

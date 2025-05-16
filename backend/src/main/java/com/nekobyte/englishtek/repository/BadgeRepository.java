package com.nekobyte.englishtek.repository;

import com.nekobyte.englishtek.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByIdIn(List<Long> ids);
}

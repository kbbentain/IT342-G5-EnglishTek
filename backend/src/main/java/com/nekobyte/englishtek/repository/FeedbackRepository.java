package com.nekobyte.englishtek.repository;

import com.nekobyte.englishtek.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByChapterId(Long chapterId);
    Optional<Feedback> findByUserIdAndChapterId(Long userId, Long chapterId);
    List<Feedback> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Feedback f WHERE f.chapter.id = :chapterId")
    void deleteByChapterId(@Param("chapterId") Long chapterId);
}

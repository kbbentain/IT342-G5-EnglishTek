package com.nekobyte.englishtek.repository;

import com.nekobyte.englishtek.model.LessonAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LessonAttemptRepository extends JpaRepository<LessonAttempt, Long> {
    List<LessonAttempt> findByUserId(Long userId);
    List<LessonAttempt> findByLessonId(Long lessonId);
    Optional<LessonAttempt> findByUserIdAndLessonId(Long userId, Long lessonId);
    int countByUserIdAndCompletedAtIsNotNull(Long userId);
    void deleteByUserId(Long userId);
    List<LessonAttempt> findByUserIdAndCompletedAtIsNotNull(Long userId);
    List<LessonAttempt> findByUserIdAndLesson_Chapter_Id(Long userId, Long chapterId);

    @Modifying
    @Query("DELETE FROM LessonAttempt la WHERE la.lesson.id = :lessonId")
    void deleteByLessonId(@Param("lessonId") Long lessonId);
}

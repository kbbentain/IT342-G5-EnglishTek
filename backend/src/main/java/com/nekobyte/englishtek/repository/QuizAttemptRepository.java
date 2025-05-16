package com.nekobyte.englishtek.repository;

import com.nekobyte.englishtek.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
    List<QuizAttempt> findByUserId(Long userId);
    List<QuizAttempt> findByQuizId(Long quizId);
    int countByUserIdAndCompletedAtIsNotNull(Long userId);
    void deleteByUserId(Long userId);
    List<QuizAttempt> findByUserIdAndCompletedAtIsNotNull(Long userId);
    List<QuizAttempt> findByUserIdAndQuiz_Chapter_Id(Long userId, Long chapterId);
    @Modifying
    @Query("DELETE FROM QuizAttempt qa WHERE qa.quiz.id = :quizId")
    void deleteByQuizId(@Param("quizId") Long quizId);
}

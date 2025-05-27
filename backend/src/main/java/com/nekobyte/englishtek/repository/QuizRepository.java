package com.nekobyte.englishtek.repository;

import com.nekobyte.englishtek.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByChapterId(Long chapterId);
    
    @Modifying
    @Query(value = "DELETE FROM quiz_questions WHERE quiz_id = :quizId", nativeQuery = true)
    void deleteQuestionsByQuizId(@Param("quizId") Long quizId);
}

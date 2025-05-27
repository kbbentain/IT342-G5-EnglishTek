package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.model.Chapter;
import com.nekobyte.englishtek.model.Lesson;
import com.nekobyte.englishtek.model.Quiz;
import com.nekobyte.englishtek.repository.ChapterRepository;
import com.nekobyte.englishtek.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataMigrationService {
    private final ChapterRepository chapterRepository;
    private final QuizRepository quizRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateData() {
        System.out.println("Starting data migration...");

        // Get all chapters
        List<Chapter> chapters = chapterRepository.findAll();
        
        for (Chapter chapter : chapters) {
            boolean needsUpdate = false;

            // Sort lessons and quizzes by creation date if order is null
            List<Lesson> lessons = chapter.getLessons();
            List<Quiz> quizzes = chapter.getQuizzes();

            // Handle lesson orders
            if (lessons != null && !lessons.isEmpty() && lessons.stream().anyMatch(l -> l.getOrder() == null)) {
                System.out.println("Migrating lesson orders for chapter " + chapter.getId());
                List<Lesson> sortedLessons = new ArrayList<>(lessons);
                sortedLessons.sort(Comparator.comparing(Lesson::getCreatedAt));
                for (int i = 0; i < sortedLessons.size(); i++) {
                    sortedLessons.get(i).setOrder(i + 1);
                }
                needsUpdate = true;
            }

            // Handle quiz orders
            if (quizzes != null && !quizzes.isEmpty() && quizzes.stream().anyMatch(q -> q.getOrder() == null)) {
                System.out.println("Migrating quiz orders for chapter " + chapter.getId());
                List<Quiz> sortedQuizzes = new ArrayList<>(quizzes);
                sortedQuizzes.sort(Comparator.comparing(Quiz::getCreatedAt));
                for (int i = 0; i < sortedQuizzes.size(); i++) {
                    sortedQuizzes.get(i).setOrder(i + 1);
                }
                needsUpdate = true;
            }

            // Handle quiz isRandom
            if (quizzes != null && !quizzes.isEmpty() && quizzes.stream().anyMatch(q -> q.getIsRandom() == null)) {
                System.out.println("Setting default isRandom values for quizzes in chapter " + chapter.getId());
                quizzes.forEach(quiz -> {
                    if (quiz.getIsRandom() == null) {
                        quiz.setIsRandom(false);
                    }
                });
                needsUpdate = true;
            }

            if (needsUpdate) {
                chapterRepository.save(chapter);
                System.out.println("Updated chapter " + chapter.getId());
            }
        }

        System.out.println("Data migration completed successfully.");
    }
}

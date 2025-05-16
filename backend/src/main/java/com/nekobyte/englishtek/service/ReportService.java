package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.model.*;
import com.nekobyte.englishtek.repository.*;
import com.nekobyte.englishtek.util.HtmlPdfConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final UserService userService;
    private final ChapterRepository chapterRepository;
    private final LessonAttemptRepository lessonAttemptRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    @Autowired
    private HtmlPdfConverter htmlPdfConverter;

    public byte[] generateUserReport(Long userId) throws IOException {
        User user = userId != null 
            ? userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"))
            : userService.getCurrentUser();
            
        return generateReport(user);
    }

    private byte[] generateReport(User user) throws IOException {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' hh:mm a");
        String currentDateTime = LocalDateTime.now().format(formatter);

        // Add HTML doctype and head
        report.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
              .append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
              .append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n")
              .append("<meta charset=\"UTF-8\" />\n")
              .append("<title>User Progress Report - ").append(user.getUsername()).append("</title>\n");

        // Add CSS styles
        report.append("<style type=\"text/css\">\n")
              .append("body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }\n")
              .append("h1 { color: #2c3e50; text-align: center; margin-bottom: 30px; }\n")
              .append("h2 { color: #3498db; margin-top: 30px; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n")
              .append("h3 { color: #2980b9; margin-top: 20px; }\n")
              .append(".info-box { background: #f8f9fa; border-radius: 5px; padding: 20px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n")
              .append(".stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin: 20px 0; }\n")
              .append(".stat-card { background: #fff; padding: 15px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); text-align: center; }\n")
              .append(".stat-number { font-size: 24px; font-weight: bold; color: #3498db; }\n")
              .append(".stat-label { color: #7f8c8d; font-size: 14px; }\n")
              .append(".progress-table { width: 100%; border-collapse: collapse; margin: 20px 0; table-layout: fixed; }\n")
              .append(".progress-table th, .progress-table td { padding: 12px; border-bottom: 1px solid #ddd; }\n")
              .append(".progress-table th { background: #3498db; color: white; text-align: left; }\n")
              .append(".progress-table th:nth-child(1) { width: 80px; }\n") // Fixed width for Item column
              .append(".progress-table th:nth-child(3) { width: 120px; }\n") // Fixed width for Status column
              .append(".progress-table th:nth-child(4) { width: 120px; }\n") // Fixed width for Progress column
              .append(".progress-table td { vertical-align: middle; }\n")
              .append(".item-cell { white-space: nowrap; }\n") // Prevent "Lesson" and "Quiz" from wrapping
              .append(".title-cell { word-wrap: break-word; }\n")
              .append(".status { padding: 5px 10px; border-radius: 3px; font-size: 12px; font-weight: bold; white-space: nowrap; display: inline-block; }\n")
              .append(".status-completed { background: #2ecc71; color: white; }\n")
              .append(".status-incomplete { background: #e67e22; color: white; }\n")
              .append(".status-in-progress { background: #f1c40f; color: white; }\n")
              .append(".status-not-started { background: #e74c3c; color: white; }\n")
              .append(".rating { font-size: 16px; letter-spacing: 2px; }\n")
              .append(".rating-filled, .rating-empty { font-family: Arial, sans-serif; }\n")
              .append(".rating-filled { color: #f1c40f; font-weight: bold; }\n")
              .append(".rating-empty { color: #ddd; }\n")
              .append("</style>\n")
              .append("</head>\n<body>\n");

        // Header
        report.append("<h1>Generated Reports and Information<br />by EnglishTek</h1>\n\n");

        // User Information
        report.append("<div class=\"info-box\">\n")
              .append("<h2>User Information</h2>\n")
              .append("<p><strong>ID:</strong> ").append(user.getId()).append("</p>\n")
              .append("<p><strong>Name:</strong> ").append(user.getName()).append("</p>\n")
              .append("<p><strong>Username:</strong> ").append(user.getUsername()).append("</p>\n")
              .append("<p><strong>Bio:</strong> ").append(user.getBio() != null ? user.getBio() : "Not provided").append("</p>\n")
              .append("<p><strong>Registered on:</strong> ").append(user.getCreatedAt().format(formatter)).append("</p>\n")
              .append("</div>\n\n");

        // Overview Statistics
        List<Chapter> allChapters = chapterRepository.findAll();
        List<LessonAttempt> completedLessons = lessonAttemptRepository.findByUserIdAndCompletedAtIsNotNull(user.getId());
        List<QuizAttempt> completedQuizzes = quizAttemptRepository.findByUserIdAndCompletedAtIsNotNull(user.getId());
        
        Set<Long> completedChapterIds = new HashSet<>();
        for (Chapter chapter : allChapters) {
            if (isChapterCompleted(chapter, user.getId())) {
                completedChapterIds.add(chapter.getId());
            }
        }

        double averageScore = completedQuizzes.stream()
            .mapToDouble(quiz -> (double) quiz.getScore() / quiz.getQuiz().getNumberOfItems() * 100)
            .average()
            .orElse(0.0);

        report.append("<h2>Overview</h2>\n")
              .append("<div class=\"stats-grid\">\n")
              .append("  <div class=\"stat-card\">\n")
              .append("    <div class=\"stat-number\">").append(completedChapterIds.size()).append("</div>\n")
              .append("    <div class=\"stat-label\">Completed Chapters</div>\n")
              .append("  </div>\n")
              .append("  <div class=\"stat-card\">\n")
              .append("    <div class=\"stat-number\">").append(completedLessons.size()).append("</div>\n")
              .append("    <div class=\"stat-label\">Completed Lessons</div>\n")
              .append("  </div>\n")
              .append("  <div class=\"stat-card\">\n")
              .append("    <div class=\"stat-number\">").append(completedQuizzes.size()).append("</div>\n")
              .append("    <div class=\"stat-label\">Completed Quizzes</div>\n")
              .append("  </div>\n")
              .append("  <div class=\"stat-card\">\n")
              .append("    <div class=\"stat-number\">").append(String.format("%.1f%%", averageScore)).append("</div>\n")
              .append("    <div class=\"stat-label\">Average Quiz Score</div>\n")
              .append("  </div>\n")
              .append("</div>\n\n");

        // Completed Chapters
        report.append("<h2>Completed Chapters</h2>\n");
        appendChapterProgress(report, allChapters.stream()
            .filter(chapter -> completedChapterIds.contains(chapter.getId()))
            .collect(Collectors.toList()), user.getId(), true);

        // Incomplete Chapters
        report.append("<h2>Incomplete Chapters</h2>\n");
        appendChapterProgress(report, allChapters.stream()
            .filter(chapter -> !completedChapterIds.contains(chapter.getId()))
            .collect(Collectors.toList()), user.getId(), false);

        // Feedback Reports
        report.append("<h2>Feedback Reports</h2>\n");
        List<Feedback> feedbacks = feedbackRepository.findByUserId(user.getId());
        if (!feedbacks.isEmpty()) {
            for (Feedback feedback : feedbacks) {
                report.append("<div class=\"feedback-card\">\n")
                      .append("  <h3>Chapter ").append(feedback.getChapter().getTitle()).append("</h3>\n")
                      .append("  <p><strong>Rating:</strong> <span style=\"color: #f1c40f; font-weight: bold; letter-spacing: 2px;\">")
                      .append(generateRatingDisplay(feedback.getRating()))
                      .append("</span></p>\n")
                      .append("  <p><strong>Word Picked:</strong> ").append(feedback.getFeedbackKeyword()).append("</p>\n")
                      .append("  <p><strong>Feedback:</strong> ").append(feedback.getFeedbackText()).append("</p>\n")
                      .append("</div>\n");
            }
        } else {
            report.append("<div class=\"info-box\">")
                  .append("<p><em>No feedback provided yet</em></p>")
                  .append("</div>\n");
        }

        // Footer
        report.append("<div style=\"margin-top: 40px; text-align: center; color: #7f8c8d; font-size: 12px;\">\n")
              .append("<hr style=\"margin: 20px 0;\"/>\n")
              .append("<p>Generated by ").append(userService.getCurrentUser().getUsername())
              .append(" on ").append(currentDateTime).append("</p>\n")
              .append("</div>");

        // Close HTML tags
        report.append("\n</body>\n</html>");

        return htmlPdfConverter.convertHtmlToPdf(report.toString());
    }

    private void appendChapterProgress(StringBuilder report, List<Chapter> chapters, Long userId, boolean completed) {
        if (chapters.isEmpty()) {
            report.append("<div class=\"info-box\">")
                  .append("<p><em>No ").append(completed ? "completed" : "incomplete").append(" chapters</em></p>")
                  .append("</div>\n");
            return;
        }

        for (Chapter chapter : chapters) {
            report.append("<div class=\"info-box\">\n")
                  .append("<h3>").append(chapter.getTitle()).append("</h3>\n")
                  .append("<table class=\"progress-table\">\n")
                  .append("<tr><th>Item</th><th>Title</th><th>Status</th><th>Progress</th></tr>\n");

            // Get attempts for this chapter
            List<LessonAttempt> lessonAttempts = lessonAttemptRepository.findByUserIdAndLesson_Chapter_Id(userId, chapter.getId());
            List<QuizAttempt> quizAttempts = quizAttemptRepository.findByUserIdAndQuiz_Chapter_Id(userId, chapter.getId());

            // Sort lessons and quizzes by their order or ID
            chapter.getLessons().stream()
                .sorted(Comparator.comparing(Lesson::getId))
                .forEach(lesson -> {
                    Optional<LessonAttempt> attempt = lessonAttempts.stream()
                        .filter(la -> la.getLesson().getId().equals(lesson.getId()))
                        .findFirst();
                    
                    String statusClass = attempt.map(la -> la.getCompletedAt() != null ? 
                        "status-completed" : "status-in-progress").orElse("status-not-started");
                    String statusText = attempt.map(la -> la.getCompletedAt() != null ? 
                        "COMPLETED" : "IN PROGRESS").orElse("NOT STARTED");
                    
                    report.append("<tr>\n")
                          .append("  <td class=\"item-cell\">Lesson</td>\n")
                          .append("  <td class=\"title-cell\">").append(lesson.getTitle()).append("</td>\n")
                          .append("  <td><span class=\"status ").append(statusClass).append("\">")
                          .append(statusText).append("</span></td>\n")
                          .append("  <td>-</td>\n")
                          .append("</tr>\n");
                });

            chapter.getQuizzes().stream()
                .sorted(Comparator.comparing(Quiz::getId))
                .forEach(quiz -> {
                    Optional<QuizAttempt> attempt = quizAttempts.stream()
                        .filter(qa -> qa.getQuiz().getId().equals(quiz.getId()))
                        .findFirst();
                    
                    if (attempt.isPresent()) {
                        QuizAttempt qa = attempt.get();
                        double percentage = (double) qa.getScore() / qa.getQuiz().getNumberOfItems() * 100;
                        String statusClass;
                        String statusText;
                        
                        if (qa.getCompletedAt() != null) {
                            if (percentage >= 80.0) {
                                statusClass = "status-completed";
                                statusText = "COMPLETED";
                            } else {
                                statusClass = "status-incomplete";
                                statusText = "INCOMPLETE";
                            }
                        } else {
                            statusClass = "status-in-progress";
                            statusText = "IN PROGRESS";
                        }
                        
                        report.append("<tr>\n")
                              .append("  <td class=\"item-cell\">Quiz</td>\n")
                              .append("  <td class=\"title-cell\">").append(quiz.getTitle()).append("</td>\n")
                              .append("  <td><span class=\"status ").append(statusClass).append("\">")
                              .append(statusText).append("</span></td>\n")
                              .append("  <td>").append(qa.getScore()).append("/")
                              .append(qa.getQuiz().getNumberOfItems())
                              .append(" (").append(String.format("%.1f%%", percentage)).append(")</td>\n")
                              .append("</tr>\n");
                    } else {
                        report.append("<tr>\n")
                              .append("  <td class=\"item-cell\">Quiz</td>\n")
                              .append("  <td class=\"title-cell\">").append(quiz.getTitle()).append("</td>\n")
                              .append("  <td><span class=\"status status-not-started\">NOT STARTED</span></td>\n")
                              .append("  <td>Not attempted</td>\n")
                              .append("</tr>\n");
                    }
                });

            report.append("</table>\n</div>\n\n");
        }
    }

    private boolean isChapterCompleted(Chapter chapter, Long userId) {
        // Check if all lessons are completed
        boolean lessonsCompleted = chapter.getLessons().stream()
            .allMatch(lesson -> lessonAttemptRepository.findByUserIdAndLessonId(userId, lesson.getId())
                .map(attempt -> attempt.getCompletedAt() != null)
                .orElse(false));

        // Check if all quizzes are completed
        boolean quizzesCompleted = chapter.getQuizzes().stream()
            .allMatch(quiz -> quizAttemptRepository.findByUserIdAndQuizId(userId, quiz.getId())
                .map(attempt -> attempt.getCompletedAt() != null)
                .orElse(false));

        return lessonsCompleted && quizzesCompleted;
    }

    private String generateRatingDisplay(int rating) {
        StringBuilder ratingDisplay = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                ratingDisplay.append("&#9673;"); // Solid circle
            } else {
                ratingDisplay.append("&#9472;"); // Em dash
            }
            ratingDisplay.append(" "); // Add space between symbols
        }
        ratingDisplay.append(" (").append(rating).append(" out of 5)");
        return ratingDisplay.toString();
    }
}

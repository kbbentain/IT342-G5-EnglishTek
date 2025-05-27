package com.nekobyte.englishtek.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nekobyte.englishtek.dto.badge.BadgeResponse;
import com.nekobyte.englishtek.dto.quiz.QuizRequest;
import com.nekobyte.englishtek.dto.quiz.QuizResponse;
import com.nekobyte.englishtek.dto.quiz.QuizSubmissionRequest;
import com.nekobyte.englishtek.dto.quiz.QuizSubmissionResponse;
import com.nekobyte.englishtek.model.*;
import com.nekobyte.englishtek.repository.BadgeRepository;
import com.nekobyte.englishtek.repository.ChapterRepository;
import com.nekobyte.englishtek.repository.QuizAttemptRepository;
import com.nekobyte.englishtek.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final ChapterRepository chapterRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final BadgeRepository badgeRepository;
    private final UserService userService;
    private final BadgeService badgeService;
    private final ObjectMapper objectMapper;

    public List<QuizResponse> getAllQuizzes() {
        User currentUser = userService.getCurrentUser();
        return quizRepository.findAll().stream()
                .map(quiz -> mapToResponse(quiz, currentUser))
                .collect(Collectors.toList());
    }

    public List<QuizResponse> getQuizzesByChapter(Long chapterId) {
        User currentUser = userService.getCurrentUser();
        return quizRepository.findByChapterId(chapterId).stream()
                .map(quiz -> mapToResponse(quiz, currentUser))
                .collect(Collectors.toList());
    }

    public QuizResponse getQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        User currentUser = userService.getCurrentUser();
        return mapToResponse(quiz, currentUser);
    }

    @Transactional
    public QuizResponse createQuiz(QuizRequest request) {
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setDifficulty(request.getDifficulty());
        quiz.setMaxScore(request.getMaxScore());
        quiz.setChapter(chapter);
        quiz.setIsRandom(request.getIsRandom() != null ? request.getIsRandom() : false);
        quiz.setNumberOfItems(request.getQuestions().size());

        if (request.getBadgeId() != null) {
            Badge badge = badgeRepository.findById(request.getBadgeId())
                    .orElseThrow(() -> new IllegalArgumentException("Badge not found"));
            quiz.setBadge(badge);
        }

        // Create questions
        List<QuizQuestion> questions = new ArrayList<>();
        for (QuizRequest.QuestionRequest questionRequest : request.getQuestions()) {
            QuizQuestion question = createQuestion(quiz, questionRequest);
            questions.add(question);
        }

        quiz.setQuestions(questions);
        quiz = quizRepository.save(quiz);

        return mapToResponse(quiz, userService.getCurrentUser());
    }

    @Transactional
    public QuizResponse updateQuiz(Long id, QuizRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setDifficulty(request.getDifficulty());
        quiz.setMaxScore(request.getMaxScore());
        quiz.setChapter(chapter);
        quiz.setIsRandom(request.getIsRandom() != null ? request.getIsRandom() : quiz.getIsRandom());
        quiz.setNumberOfItems(request.getQuestions().size());

        if (request.getBadgeId() != null) {
            Badge badge = badgeRepository.findById(request.getBadgeId())
                    .orElseThrow(() -> new IllegalArgumentException("Badge not found"));
            quiz.setBadge(badge);
        } else {
            quiz.setBadge(null);
        }

        // Properly handle the bidirectional relationship
        List<QuizQuestion> oldQuestions = new ArrayList<>(quiz.getQuestions());
        quiz.getQuestions().clear();
        
        // Create new questions and maintain bidirectional relationship
        for (QuizRequest.QuestionRequest questionRequest : request.getQuestions()) {
            QuizQuestion question = createQuestion(quiz, questionRequest);
            quiz.getQuestions().add(question);
        }

        // Save the quiz
        quiz = quizRepository.save(quiz);
        
        // Now it's safe to remove old questions from the persistence context
        oldQuestions.clear();
        
        return mapToResponse(quiz, userService.getCurrentUser());
    }

    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        
        // Delete the badge if it exists
        if (quiz.getBadge() != null) {
            badgeService.deleteBadge(quiz.getBadge().getId());
        }
        
        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizResponse startQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        User currentUser = userService.getCurrentUser();

        // Find existing attempt
        QuizAttempt existingAttempt = quizAttemptRepository
                .findByUserIdAndQuizId(currentUser.getId(), id)
                .orElse(null);

        // If there's an existing attempt and it's completed
        if (existingAttempt != null && existingAttempt.getCompletedAt() != null) {
            // Check if eligible for retake (score < maxScore)
            if (existingAttempt.getScore() < quiz.getMaxScore()) {
                // Delete old attempt
                quizAttemptRepository.delete(existingAttempt);
                existingAttempt = null;
            } else {
                throw new IllegalStateException("Quiz already completed with maximum score");
            }
        }

        // Create new attempt if none exists
        if (existingAttempt == null) {
            existingAttempt = new QuizAttempt();
            existingAttempt.setQuiz(quiz);
            existingAttempt.setUser(currentUser);
            existingAttempt.setStartedAt(LocalDateTime.now());
            quizAttemptRepository.save(existingAttempt);
        }

        // Map quiz to response
        QuizResponse response = mapToResponse(quiz, currentUser);

        // If isRandom is true, shuffle the questions
        if (quiz.getIsRandom()) {
            Collections.shuffle(response.getQuestions());
            // Update page numbers after shuffling
            for (int i = 0; i < response.getQuestions().size(); i++) {
                response.getQuestions().get(i).setPage(i + 1);
            }
        }

        return response;
    }

    @Transactional
    public QuizSubmissionResponse submitQuiz(Long id, QuizSubmissionRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        User currentUser = userService.getCurrentUser();

        // Admin can submit multiple times
        if (!currentUser.getRole().name().equals("ADMIN")) {
            QuizAttempt existingAttempt = quizAttemptRepository
                    .findByUserIdAndQuizId(currentUser.getId(), id)
                    .orElseThrow(() -> new IllegalStateException("Cannot submit a quiz that hasn't been started. Please start the quiz first."));

            if (existingAttempt.getStartedAt() == null) {
                throw new IllegalStateException("Cannot submit a quiz that hasn't been started. Please start the quiz first.");
            }

            if (existingAttempt.getCompletedAt() != null) {
                throw new IllegalStateException("This quiz has already been completed.");
            }
        }

        QuizAttempt attempt = quizAttemptRepository
                .findByUserIdAndQuizId(currentUser.getId(), id)
                .orElseGet(() -> {
                    QuizAttempt newAttempt = new QuizAttempt();
                    newAttempt.setUser(currentUser);
                    newAttempt.setQuiz(quiz);
                    newAttempt.setStartedAt(LocalDateTime.now());
                    return newAttempt;
                });

        attempt.setScore(request.getScore());
        attempt.setCompletedAt(LocalDateTime.now());

        // Award badge if score is at least 80% of max score and quiz has a badge
        boolean badgeAwarded = false;
        if (request.getScore() >= (quiz.getMaxScore() * 0.8) && quiz.getBadge() != null) {
            userService.addBadgeToUser(currentUser, quiz.getBadge());
            badgeAwarded = true;
        }

        attempt = quizAttemptRepository.save(attempt);

        return QuizSubmissionResponse.builder()
                .score(request.getScore())
                .maxScore(quiz.getMaxScore())
                .isEligibleForRetake(request.getScore() < (quiz.getMaxScore() * 0.8))
                .isEligibleForBadge(!badgeAwarded && quiz.getBadge() != null)
                .badge(quiz.getBadge() != null ? BadgeResponse.builder()
                        .id(quiz.getBadge().getId())
                        .name(quiz.getBadge().getName())
                        .description(quiz.getBadge().getDescription())
                        .iconUrl(quiz.getBadge().getIconUrl() != null
                            ? "/api/v1/files/" + quiz.getBadge().getIconUrl()
                            : null)
                        .build() : null)
                .badgeAwarded(badgeAwarded)
                .build();
    }

    private QuizResponse mapToResponse(Quiz quiz, User currentUser) {
        boolean completed = isQuizCompletedByUser(quiz, currentUser);

        return QuizResponse.builder()
                .id(quiz.getId())
                .chapterId(quiz.getChapter().getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .difficulty(quiz.getDifficulty())
                .maxScore(quiz.getMaxScore())
                .numberOfItems(quiz.getNumberOfItems())
                .badgeId(quiz.getBadge() != null ? quiz.getBadge().getId() : null)
                .isRandom(quiz.getIsRandom())
                .questions(quiz.getQuestions().stream()
                        .map(this::mapQuestionToResponse)
                        .collect(Collectors.toList()))
                .completed(completed)
                .build();
    }

    private boolean isQuizCompletedByUser(Quiz quiz, User user) {
        return quizAttemptRepository.findByUserIdAndQuizId(user.getId(), quiz.getId())
                .map(attempt -> {
                    if (attempt.getCompletedAt() == null) return false;
                    // Quiz is only considered completed if score is at least 80% of max score
                    return attempt.getScore() >= (quiz.getMaxScore() * 0.8);
                })
                .orElse(false);
    }

    private QuizResponse.QuestionResponse mapQuestionToResponse(QuizQuestion question) {
        return QuizResponse.QuestionResponse.builder()
                .id(question.getId())
                .page(question.getPage())
                .type(question.getType())
                .title(question.getTitle())
                .choices(question.getChoices())
                .correctAnswer(parseCorrectAnswer(question.getCorrectAnswer()))
                .build();
    }

    private Object parseCorrectAnswer(String correctAnswerJson) {
        try {
            // Try to parse as List first for multiple choice questions
            try {
                return objectMapper.readValue(correctAnswerJson, List.class);
            } catch (JsonProcessingException e) {
                // If not a list, try to parse as String for identification questions
                return objectMapper.readValue(correctAnswerJson, String.class);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse correct answer: " + e.getMessage(), e);
        }
    }

    private QuizQuestion createQuestion(Quiz quiz, QuizRequest.QuestionRequest questionRequest) {
        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setPage(questionRequest.getPage());
        question.setType(questionRequest.getType());
        question.setTitle(questionRequest.getTitle());
        question.setChoices(questionRequest.getChoices());
        try {
            // For multiple_choice, correctAnswer should be a List<String>
            // For identification, correctAnswer should be a String
            if ("multiple_choice".equals(questionRequest.getType())) {
                if (!(questionRequest.getCorrectAnswer() instanceof List)) {
                    throw new IllegalArgumentException("Multiple choice questions must have a list of correct answers");
                }
            } else if ("identification".equals(questionRequest.getType())) {
                if (!(questionRequest.getCorrectAnswer() instanceof String)) {
                    throw new IllegalArgumentException("Identification questions must have a single correct answer");
                }
            }
            question.setCorrectAnswer(objectMapper.writeValueAsString(questionRequest.getCorrectAnswer()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid correct answer format: " + e.getMessage());
        }
        return question;
    }
}

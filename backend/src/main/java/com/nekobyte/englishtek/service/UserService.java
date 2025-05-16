package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.dto.auth.AuthResponse;
import com.nekobyte.englishtek.dto.user.UpdateUserRequest;
import com.nekobyte.englishtek.dto.user.UpdateUserByAdminFormRequest;
import com.nekobyte.englishtek.dto.user.UpdateUserByAdminRequest;
import com.nekobyte.englishtek.dto.user.UserResponse;
import com.nekobyte.englishtek.model.Badge;
import com.nekobyte.englishtek.model.Chapter;
import com.nekobyte.englishtek.model.Lesson;
import com.nekobyte.englishtek.model.Quiz;
import com.nekobyte.englishtek.model.User;
import com.nekobyte.englishtek.repository.BadgeRepository;
import com.nekobyte.englishtek.repository.ChapterRepository;
import com.nekobyte.englishtek.repository.LessonAttemptRepository;
import com.nekobyte.englishtek.repository.QuizAttemptRepository;
import com.nekobyte.englishtek.repository.UserBadgeRepository;
import com.nekobyte.englishtek.repository.UserRepository;
import com.nekobyte.englishtek.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final LessonAttemptRepository lessonAttemptRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserBadgeService userBadgeService;
    private final ChapterRepository chapterRepository;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    public AuthResponse getCurrentUserDetails() {
        User user = getCurrentUser();
        
        // Count completed tasks (lessons + quizzes)
        int completedLessons = lessonAttemptRepository.countByUserIdAndCompletedAtIsNotNull(user.getId());
        int completedQuizzes = quizAttemptRepository.countByUserIdAndCompletedAtIsNotNull(user.getId());
        int totalCompletedTasks = completedLessons + completedQuizzes;
        
        // Count badges
        int totalBadges = userBadgeService.countUserBadges(user);

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().toString())
                .bio(user.getBio())
                .avatarUrl(user.getAvatar() != null
                    ? "/api/v1/files/" + user.getAvatar()
                    : null)
                .totalCompletedTasks(totalCompletedTasks)
                .totalBadges(totalBadges)
                .build();
    }

    @Transactional
    @Validated(UpdateUserRequest.PasswordUpdate.class)
    public AuthResponse updateCurrentUser(UpdateUserRequest request, MultipartFile avatar) {
        User user = getCurrentUser();

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        // Update password if provided
        if (request.isPasswordUpdateRequested()) {
            if (request.getExistingPassword() == null || request.getExistingPassword().isEmpty()) {
                throw new IllegalArgumentException("Existing password is required to update password");
            }
            
            if (!passwordEncoder.matches(request.getExistingPassword(), user.getPassword())) {
                throw new BadCredentialsException("Incorrect existing password");
            }
            
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // Update avatar if provided and not empty
        if (avatar != null && !avatar.isEmpty()) {
            String fileName = fileStorageService.storeFile(avatar);
            user.setAvatar(fileName);
        }

        user = userRepository.save(user);

        // Get updated stats for response
        int completedLessons = lessonAttemptRepository.countByUserIdAndCompletedAtIsNotNull(user.getId());
        int completedQuizzes = quizAttemptRepository.countByUserIdAndCompletedAtIsNotNull(user.getId());
        int totalCompletedTasks = completedLessons + completedQuizzes;
        int totalBadges = userBadgeService.countUserBadges(user);

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().toString())
                .bio(user.getBio())
                .avatarUrl(user.getAvatar() != null
                    ? "/api/v1/files/" + user.getAvatar()
                    : null)
                .totalCompletedTasks(totalCompletedTasks)
                .totalBadges(totalBadges)
                .build();
    }

    @Transactional
    public void deleteCurrentUser() {
        User user = getCurrentUser();
        userRepository.delete(user);
    }

    @Transactional
    public void addBadgeToUser(User user, Badge badge) {
        if (userBadgeRepository.findByUserAndBadge(user, badge).isEmpty()) {
            userBadgeService.addBadgeToUser(user, badge);
        }
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    // Get user stats
                    int totalBadges = userBadgeRepository.findByUserId(user.getId()).size();
                    int completedLessons = (int) lessonAttemptRepository.findByUserId(user.getId()).stream()
                            .filter(attempt -> attempt.getCompletedAt() != null)
                            .count();
                    int completedQuizzes = (int) quizAttemptRepository.findByUserId(user.getId()).stream()
                            .filter(attempt -> {
                                if (attempt.getCompletedAt() == null) return false;
                                return attempt.getScore() >= (attempt.getQuiz().getMaxScore() * 0.8);
                            })
                            .count();

                    // Count completed chapters - a chapter is completed when all its tasks are completed
                    List<Chapter> chapters = chapterRepository.findAll();
                    int completedChapters = 0;
                    
                    for (Chapter chapter : chapters) {
                        int completedTasks = 0;
                        int totalTasks = 0;
                        
                        // Count completed and total lessons
                        if (chapter.getLessons() != null) {
                            totalTasks += chapter.getLessons().size();
                            completedTasks += chapter.getLessons().stream()
                                .filter(lesson -> lessonAttemptRepository
                                    .findByUserIdAndLessonId(user.getId(), lesson.getId())
                                    .map(attempt -> attempt.getCompletedAt() != null)
                                    .orElse(false))
                                .count();
                        }
                        
                        // Count completed and total quizzes
                        if (chapter.getQuizzes() != null) {
                            totalTasks += chapter.getQuizzes().size();
                            completedTasks += chapter.getQuizzes().stream()
                                .filter(quiz -> quizAttemptRepository
                                    .findByUserIdAndQuizId(user.getId(), quiz.getId())
                                    .map(attempt -> {
                                        if (attempt.getCompletedAt() == null) return false;
                                        return attempt.getScore() >= (quiz.getMaxScore() * 0.8);
                                    })
                                    .orElse(false))
                                .count();
                        }
                        
                        // Chapter is completed only if all tasks are completed
                        if (totalTasks > 0 && completedTasks == totalTasks) {
                            completedChapters++;
                        }
                    }

                    return UserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .name(user.getName())
                            .role(user.getRole())
                            .bio(user.getBio())
                            .avatarUrl(user.getAvatar() != null 
                                ? "/api/v1/files/" + user.getAvatar()
                                : null)
                            .createdAt(user.getCreatedAt())
                            .lastLoginAt(user.getLastLoginAt())
                            .totalBadges(totalBadges)
                            .completedChapters(completedChapters)
                            .completedLessons(completedLessons)
                            .completedQuizzes(completedQuizzes)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private UserResponse mapUserToResponse(User user) {
        // Get user stats
        int totalBadges = userBadgeRepository.findByUserId(user.getId()).size();
        int completedLessons = (int) lessonAttemptRepository.findByUserId(user.getId()).stream()
                .filter(attempt -> attempt.getCompletedAt() != null)
                .count();
        int completedQuizzes = (int) quizAttemptRepository.findByUserId(user.getId()).stream()
                .filter(attempt -> {
                    if (attempt.getCompletedAt() == null) return false;
                    return attempt.getScore() >= (attempt.getQuiz().getMaxScore() * 0.8);
                })
                .count();

        // Count unique completed chapters
        Set<Long> completedChapterIds = new HashSet<>();
        
        // Add chapters from completed lessons
        lessonAttemptRepository.findByUserId(user.getId()).stream()
                .filter(attempt -> attempt.getCompletedAt() != null)
                .map(attempt -> attempt.getLesson().getChapter().getId())
                .forEach(completedChapterIds::add);
        
        // Add chapters from completed quizzes (>= 80%)
        quizAttemptRepository.findByUserId(user.getId()).stream()
                .filter(attempt -> {
                    if (attempt.getCompletedAt() == null) return false;
                    return attempt.getScore() >= (attempt.getQuiz().getMaxScore() * 0.8);
                })
                .map(attempt -> attempt.getQuiz().getChapter().getId())
                .forEach(completedChapterIds::add);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .bio(user.getBio())
                .avatarUrl(user.getAvatar() != null 
                    ? "/api/v1/files/" + user.getAvatar()
                    : null)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .totalBadges(totalBadges)
                .completedChapters(completedChapterIds.size())
                .completedLessons(completedLessons)
                .completedQuizzes(completedQuizzes)
                .build();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        return mapUserToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserByAdminFormRequest request, MultipartFile avatar) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            userRepository.findByUsername(request.getUsername())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("Username already exists");
                    });
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            userRepository.findByEmail(request.getEmail())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("Email already exists");
                    });
            user.setEmail(request.getEmail());
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName());
        }

        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            try {
                user.setRole(User.Role.valueOf(request.getRole()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
            }
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (avatar != null && !avatar.isEmpty()) {
            String avatarPath = fileStorageService.storeFile(avatar);
            user.setAvatar(avatarPath);
        }

        user = userRepository.save(user);
        return mapUserToResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        // Delete all user's data
        lessonAttemptRepository.deleteByUserId(id);
        quizAttemptRepository.deleteByUserId(id);
        userBadgeRepository.deleteByUserId(id);
        
        userRepository.delete(user);
    }

    @Transactional
    public AuthResponse updateUserById(Long id, UpdateUserRequest request, MultipartFile avatar) {
        // Validate that the admin is updating a valid user
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Update user details
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName());
        }
        
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        // Update password if provided
        if (request.isPasswordUpdateRequested()) {
            if (request.getExistingPassword() == null || request.getExistingPassword().isEmpty()) {
                throw new IllegalArgumentException("Existing password is required to update password");
            }
            
            if (!passwordEncoder.matches(request.getExistingPassword(), user.getPassword())) {
                throw new BadCredentialsException("Incorrect existing password");
            }
            
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // Update avatar if provided and not empty
        if (avatar != null && !avatar.isEmpty()) {
            String fileName = fileStorageService.storeFile(avatar);
            user.setAvatar(fileName);
        }

        user = userRepository.save(user);

        // Get updated stats for response
        int completedLessons = lessonAttemptRepository.countByUserIdAndCompletedAtIsNotNull(user.getId());
        int completedQuizzes = quizAttemptRepository.countByUserIdAndCompletedAtIsNotNull(user.getId());
        int totalCompletedTasks = completedLessons + completedQuizzes;
        int totalBadges = userBadgeService.countUserBadges(user);

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().toString())
                .bio(user.getBio())
                .avatarUrl(user.getAvatar() != null
                    ? "/api/v1/files/" + user.getAvatar()
                    : null)
                .totalCompletedTasks(totalCompletedTasks)
                .totalBadges(totalBadges)
                .build();
    }

    @Transactional
    public void changeUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

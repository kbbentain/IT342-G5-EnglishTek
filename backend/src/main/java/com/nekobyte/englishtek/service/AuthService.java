package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.dto.auth.AuthResponse;
import com.nekobyte.englishtek.dto.auth.LoginRequest;
import com.nekobyte.englishtek.dto.auth.RegisterRequest;
import com.nekobyte.englishtek.model.User;
import com.nekobyte.englishtek.repository.UserRepository;
import com.nekobyte.englishtek.security.JwtService;
import com.nekobyte.englishtek.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final FileStorageService fileStorageService;

    @Value("${server.port}")
    private String serverPort;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(User.Role.USER);

        // Handle avatar upload
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String fileName = fileStorageService.storeFile(request.getAvatar());
            user.setAvatar(fileName);
        }

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return createAuthResponse(user, token);
    }

    @Transactional
    public AuthResponse registerAdmin(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(User.Role.ADMIN);

        // Handle avatar upload
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String fileName = fileStorageService.storeFile(request.getAvatar());
            user.setAvatar(fileName);
        }

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return createAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        String token = jwtService.generateToken(user);
        return createAuthResponse(user, token);
    }

    private AuthResponse createAuthResponse(User user, String token) {
        return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole().toString())
            .avatarUrl(user.getAvatar() != null 
                ? "/api/v1/files/" + user.getAvatar()
                : null)
            .bio(user.getBio())
            .build();
    }
}

package sysint.G5.EnglishTek.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sysint.G5.EnglishTek.dto.auth.AuthResponse;
import sysint.G5.EnglishTek.model.User;
import sysint.G5.EnglishTek.config.JwtService;
import sysint.G5.EnglishTek.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2 Authentication", description = "OAuth2 authentication endpoints")
public class OAuth2Controller {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Operation(summary = "OAuth2 Failure", description = "Endpoint called after failed OAuth2 authentication")
    @GetMapping("/failure")
    public Map<String, String> handleOAuth2Failure() {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Authentication failed");
        return response;
    }

    @Operation(summary = "Get Current User", description = "Get the currently authenticated user's information")
    @GetMapping("/user")
    public Map<String, Object> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            
            Map<String, Object> response = new HashMap<>();
            response.put("name", oAuth2User.getAttribute("name"));
            response.put("email", oAuth2User.getAttribute("email"));
            response.put("picture", oAuth2User.getAttribute("picture"));
            
            return response;
        }
        
        throw new RuntimeException("Not authenticated with OAuth2");
    }
    
    @Operation(summary = "Get JWT Token", description = "Get JWT token for the authenticated user")
    @GetMapping("/token")
    public AuthResponse getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            String jwtToken = jwtService.generateToken(user);
            
            return AuthResponse.builder()
                    .token(jwtToken)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
        }
        
        throw new RuntimeException("Not authenticated with JWT");
    }
}

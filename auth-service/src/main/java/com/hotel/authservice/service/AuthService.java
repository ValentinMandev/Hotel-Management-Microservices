package com.hotel.authservice.service;

import com.hotel.authservice.dto.AuthResponseDto;
import com.hotel.authservice.dto.RefreshTokenRequestDto;
import com.hotel.authservice.dto.UserLoginDto;
import com.hotel.authservice.dto.UserRegistrationDto;
import com.hotel.authservice.model.Role;
import com.hotel.authservice.model.User;
import com.hotel.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.JwtException;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;


    public AuthResponseDto register(UserRegistrationDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.USER);

        user = userRepository.save(user);

        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        return createAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponseDto login(UserLoginDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        return createAuthResponse(user, accessToken, refreshToken);
    }


    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        try {
            Long userId = jwtTokenService.extractUserId(request.getRefreshToken());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!user.getIsActive()) {
                throw new IllegalArgumentException("Account is deactivated");
            }

            String newAccessToken = jwtTokenService.generateAccessToken(user);

            return createAuthResponse(user, newAccessToken, request.getRefreshToken());

        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }


    private AuthResponseDto createAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponseDto.UserInfo userInfo = new AuthResponseDto.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenService.getAccessTokenExpiration(),
                userInfo
        );
    }
}

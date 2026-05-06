package com.cyphershare.service;

import com.cyphershare.dto.AuthRequest;
import com.cyphershare.dto.AuthResponse;
import com.cyphershare.model.SubscriptionTier;
import com.cyphershare.model.User;
import com.cyphershare.repository.UserRepository;
import com.cyphershare.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse registerUser(AuthRequest request, SubscriptionTier tier) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTier(tier);

        userRepository.save(user);

        String jwt = jwtUtils.generateJwtToken(user.getEmail());
        return new AuthResponse(jwt, user.getEmail(), user.getTier().name());
    }

    public AuthResponse authenticateUser(AuthRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            String jwt = jwtUtils.generateJwtToken(user.getEmail());
            return new AuthResponse(jwt, user.getEmail(), user.getTier().name());
        }
        throw new IllegalArgumentException("Error: Invalid email or password");
    }
}

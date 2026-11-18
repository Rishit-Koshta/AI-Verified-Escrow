package com.escrow.backend.project.auth.security;

import com.escrow.backend.project.auth.AppUser;
import com.escrow.backend.project.auth.AuthRepository;
import com.escrow.backend.project.auth.UserRole;
import com.escrow.backend.project.auth.dto.*;
import com.escrow.backend.project.auth.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthRepository repository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);

        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email is already registered");
        }

        UserRole role;
        try {
            role = UserRole.valueOf(
                    request.role().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role must be CLIENT or FREELANCER");
        }

        if (role != UserRole.CLIENT && role != UserRole.FREELANCER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Public registration cannot create an arbitrator");
        }

        AppUser user = new AppUser(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                role);

        repository.saveAndFlush(user);

        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email, request.password()));

        AppUser user = repository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "Invalid email or password"));

        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }
}


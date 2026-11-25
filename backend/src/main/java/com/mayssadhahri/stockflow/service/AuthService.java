package com.mayssadhahri.stockflow.service;

import com.mayssadhahri.stockflow.dto.AuthDtos.AuthResponse;
import com.mayssadhahri.stockflow.dto.AuthDtos.LoginRequest;
import com.mayssadhahri.stockflow.dto.AuthDtos.RegisterRequest;
import com.mayssadhahri.stockflow.entity.Role;
import com.mayssadhahri.stockflow.entity.User;
import com.mayssadhahri.stockflow.exception.ConflictException;
import com.mayssadhahri.stockflow.repository.UserRepository;
import com.mayssadhahri.stockflow.security.JwtService;
import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Un compte existe déjà avec cet email");
        }
        User user = userRepository.save(
                new User(request.nom().trim(), email, passwordEncoder.encode(request.password()), Role.EMPLOYE));
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                // même message dans les deux cas : on ne révèle pas si l'email existe
                .orElseThrow(() -> new BadCredentialsException("Identifiants invalides"));
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), user.getNom(), user.getEmail(), user.getRole().name());
    }
}

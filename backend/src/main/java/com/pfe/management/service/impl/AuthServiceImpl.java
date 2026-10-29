package com.pfe.management.service.impl;

import com.pfe.management.dto.request.LoginRequest;
import com.pfe.management.dto.request.RegisterRequest;
import com.pfe.management.dto.response.AuthResponse;
import com.pfe.management.dto.response.UserResponse;
import com.pfe.management.entity.Role;
import com.pfe.management.entity.User;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.repository.UserRepository;
import com.pfe.management.security.JwtService;
import com.pfe.management.security.UserPrincipal;
import com.pfe.management.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentative d'inscription pour l'email : {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Un utilisateur avec cet email existe déjà : " + request.getEmail());
        }

        // Sécurité stricte : l'inscription publique attribue EXCLUSIVEMENT le rôle STUDENT
        // Les rôles privilégiés (SUPERVISOR, ADMIN) nécessitent une attribution administrative
        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);

        User savedUser = userRepository.save(user);
        log.info("Utilisateur enregistré avec succès avec l'ID : {}", savedUser.getId());

        String token = jwtService.generateTokenForUser(savedUser.getEmail(), savedUser.getId(), savedUser.getRole().name());

        return new AuthResponse(token, toUserResponse(savedUser));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l'email : {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(authentication);

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        return new AuthResponse(token, toUserResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}


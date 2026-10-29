package com.pfe.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.management.dto.request.LoginRequest;
import com.pfe.management.dto.request.RegisterRequest;
import com.pfe.management.entity.Role;
import com.pfe.management.entity.User;
import com.pfe.management.repository.UserRepository;
import com.pfe.management.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Inscription réussie d'un étudiant avec rôle STUDENT par défaut")
    void shouldRegisterStudentSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Yassine",
                "El Ansari",
                "yassine@example.com",
                "Password123!"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.email", is("yassine@example.com")))
                .andExpect(jsonPath("$.user.role", is("STUDENT")))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    @DisplayName("Refus de l'inscription avec email déjà existant (HTTP 409)")
    void shouldRejectDuplicateEmailOnRegister() throws Exception {
        User existing = new User("Safia", "Test", "duplicate@example.com", passwordEncoder.encode("Secret123!"), Role.STUDENT);
        userRepository.save(existing);

        RegisterRequest request = new RegisterRequest(
                "Autre",
                "Personne",
                "duplicate@example.com",
                "Password123!"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("Connexion réussie avec génération de JWT valide (HTTP 200)")
    void shouldLoginSuccessfully() throws Exception {
        User user = new User("Mohamed", "Simo", "simo@example.com", passwordEncoder.encode("SecretPass123!"), Role.SUPERVISOR);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("simo@example.com", "SecretPass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("simo@example.com")))
                .andExpect(jsonPath("$.user.role", is("SUPERVISOR")));
    }

    @Test
    @DisplayName("Refus de connexion avec mot de passe incorrect (HTTP 401)")
    void shouldRejectLoginWithWrongPassword() throws Exception {
        User user = new User("Salma", "Dev", "salma@example.com", passwordEncoder.encode("CorrectPassword123!"), Role.STUDENT);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("salma@example.com", "WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Accès à /auth/me avec token JWT valide (HTTP 200)")
    void shouldGetMeWithValidJwt() throws Exception {
        User user = new User("Admin", "Root", "admin@example.com", passwordEncoder.encode("AdminPass123!"), Role.ADMIN);
        User saved = userRepository.save(user);

        String token = jwtService.generateTokenForUser(saved.getEmail(), saved.getId(), saved.getRole().name());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("admin@example.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("Refus d'accès à /auth/me sans token JWT (HTTP 401)")
    void shouldRejectMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}


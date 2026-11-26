package com.pfe.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.management.dto.request.UserActiveUpdateRequest;
import com.pfe.management.dto.request.UserCreateRequest;
import com.pfe.management.dto.request.UserRoleUpdateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.UserResponse;
import com.pfe.management.entity.Role;
import com.pfe.management.exception.BusinessRuleException;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.security.CustomAccessDeniedHandler;
import com.pfe.management.security.CustomAuthenticationEntryPoint;
import com.pfe.management.security.JwtAuthenticationFilter;
import com.pfe.management.security.JwtService;
import com.pfe.management.security.PasswordEncoderConfig;
import com.pfe.management.security.SecurityConfig;
import com.pfe.management.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, JwtAuthenticationFilter.class, PasswordEncoderConfig.class})
@MockBean(JwtService.class)
@MockBean(com.pfe.management.security.CustomUserDetailsService.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse createSampleUserResponse(Long id, String email, String role, boolean active) {
        return new UserResponse(id, "Alice", "Student", email, role, active, LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/v1/users sans authentification retourne 401")
    void getAllUsersWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /api/v1/users avec rôle STUDENT retourne 403 Forbidden")
    void getAllUsersAsStudentReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    @DisplayName("GET /api/v1/users avec rôle SUPERVISOR retourne 200 OK et liste paginée")
    void getAllUsersAsSupervisorReturns200() throws Exception {
        UserResponse sample = createSampleUserResponse(5L, "alice@pfe.com", "STUDENT", true);
        PageResponse<UserResponse> pageResponse = new PageResponse<>(List.of(sample), 0, 10, 1, 1, true, true);

        when(userService.getAllUsers(any(), any(), any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/users")
                        .param("role", "STUDENT")
                        .param("q", "ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5L))
                .andExpect(jsonPath("$.content[0].email").value("alice@pfe.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /api/v1/users avec rôle STUDENT retourne 403 Forbidden")
    void createUserAsStudentReturns403() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "Alice", "Student", "alice@pfe.com", "Password123", Role.STUDENT
        );

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    @DisplayName("POST /api/v1/users avec rôle SUPERVISOR retourne 403 Forbidden")
    void createUserAsSupervisorReturns403() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "Alice", "Student", "alice@pfe.com", "Password123", Role.STUDENT
        );

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/users avec données valides retourne 201 Created")
    void createUserAsAdminReturns201() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "Alice", "Student", "alice@pfe.com", "Password123", Role.STUDENT
        );
        UserResponse response = createSampleUserResponse(5L, "alice@pfe.com", "STUDENT", true);

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.email").value("alice@pfe.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/users avec email déjà utilisé retourne 409 Conflict")
    void createUserDuplicateEmailReturns409() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "Alice", "Student", "alice@pfe.com", "Password123", Role.STUDENT
        );

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Un utilisateur avec cet email existe déjà"));

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/users avec mot de passe faible retourne 400 Bad Request")
    void createUserWeakPasswordReturns400() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "Alice", "Student", "alice@pfe.com", "weakpass", Role.STUDENT
        );

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/users/{id}/role avec données valides retourne 200 OK")
    void updateUserRoleAsAdminReturns200() throws Exception {
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.SUPERVISOR);
        UserResponse response = createSampleUserResponse(5L, "alice@pfe.com", "SUPERVISOR", true);

        when(userService.updateUserRole(eq(5L), any(UserRoleUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/5/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    @DisplayName("PATCH /api/v1/users/{id}/active avec rôle SUPERVISOR retourne 403 Forbidden")
    void updateUserActiveAsSupervisorReturns403() throws Exception {
        UserActiveUpdateRequest request = new UserActiveUpdateRequest(false);

        mockMvc.perform(patch("/api/v1/users/5/active")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/users/{id}/role utilisateur inexistant retourne 404")
    void updateUserRoleNotFoundReturns404() throws Exception {
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.SUPERVISOR);

        when(userService.updateUserRole(eq(999L), any(UserRoleUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Utilisateur introuvable"));

        mockMvc.perform(patch("/api/v1/users/999/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/users/{id}/active auto-désactivation retourne 400 Bad Request")
    void selfDeactivateAsAdminReturns400() throws Exception {
        UserActiveUpdateRequest request = new UserActiveUpdateRequest(false);

        when(userService.updateUserActive(eq(1L), any(UserActiveUpdateRequest.class)))
                .thenThrow(new BusinessRuleException("Un administrateur ne peut pas désactiver son propre compte"));

        mockMvc.perform(patch("/api/v1/users/1/active")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

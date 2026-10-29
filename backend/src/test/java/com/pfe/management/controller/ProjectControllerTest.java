package com.pfe.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.management.dto.request.ProjectCreateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.ProjectResponse;
import com.pfe.management.entity.ProjectStatus;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.security.CustomAccessDeniedHandler;
import com.pfe.management.security.CustomAuthenticationEntryPoint;
import com.pfe.management.security.SecurityConfig;
import com.pfe.management.service.ProjectService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, com.pfe.management.security.JwtAuthenticationFilter.class, com.pfe.management.security.PasswordEncoderConfig.class})
@org.springframework.boot.test.mock.mockito.MockBean(com.pfe.management.security.JwtService.class)
@org.springframework.boot.test.mock.mockito.MockBean(com.pfe.management.security.CustomUserDetailsService.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @Test
    @DisplayName("GET /api/v1/projects est accessible publiquement et retourne la liste paginée")
    void getAllProjectsShouldBePublic() throws Exception {
        ProjectResponse sample = new ProjectResponse(
                1L, "Projet 1", "Description", ProjectStatus.PROPOSED,
                LocalDate.now(), LocalDate.now().plusMonths(6),
                0, 0, 0, LocalDateTime.now(), LocalDateTime.now()
        );
        PageResponse<ProjectResponse> pageResponse = new PageResponse<>(
                List.of(sample), 0, 10, 1, 1, true, true
        );

        when(projectService.getAllProjects(any(), any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Projet 1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} avec ID existant retourne 200")
    void getProjectByIdFound() throws Exception {
        ProjectResponse sample = new ProjectResponse(
                1L, "Projet 1", "Description", ProjectStatus.PROPOSED,
                LocalDate.now(), LocalDate.now().plusMonths(6),
                0, 0, 0, LocalDateTime.now(), LocalDateTime.now()
        );

        when(projectService.getProjectById(1L)).thenReturn(sample);

        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Projet 1"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} inexistant retourne 404 standardisé")
    void getProjectByIdNotFoundReturns404() throws Exception {
        when(projectService.getProjectById(999L))
                .thenThrow(new ResourceNotFoundException("Projet introuvable"));

        mockMvc.perform(get("/api/v1/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Projet introuvable"));
    }

    @Test
    @DisplayName("POST /api/v1/projects sans authentification retourne 401 Unauthorized")
    void createProjectWithoutAuthShouldReturn401() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Projet Sécurisé", "Description", ProjectStatus.PROPOSED,
                LocalDate.now(), LocalDate.now().plusMonths(6)
        );

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentification requise pour accéder à cette ressource"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/projects avec authentification et données valides retourne 201 Created")
    void createProjectWithAuthSuccess() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Projet Sécurisé", "Description", ProjectStatus.PROPOSED,
                LocalDate.now(), LocalDate.now().plusMonths(6)
        );

        ProjectResponse response = new ProjectResponse(
                10L, "Projet Sécurisé", "Description", ProjectStatus.PROPOSED,
                LocalDate.now(), LocalDate.now().plusMonths(6),
                0, 0, 0, LocalDateTime.now(), LocalDateTime.now()
        );

        when(projectService.createProject(any(ProjectCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Projet Sécurisé"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/projects avec titre déjà existant retourne 409 Conflict")
    void createProjectConflictReturns409() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Projet Existant", "Description", ProjectStatus.PROPOSED,
                LocalDate.now(), LocalDate.now().plusMonths(6)
        );

        when(projectService.createProject(any(ProjectCreateRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Un projet portant le titre 'Projet Existant' existe déjà"));

        mockMvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Un projet portant le titre 'Projet Existant' existe déjà"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/projects avec validation invalide retourne 400 Bad Request")
    void createProjectValidationFailsReturns400() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "", "Description", ProjectStatus.PROPOSED,
                LocalDate.now(), LocalDate.now().plusMonths(6)
        );

        mockMvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.title").exists());
    }
}

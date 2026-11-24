package com.pfe.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.management.dto.request.ProjectMemberRequest;
import com.pfe.management.dto.response.ProjectMemberResponse;
import com.pfe.management.entity.Role;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.security.CustomAccessDeniedHandler;
import com.pfe.management.security.CustomAuthenticationEntryPoint;
import com.pfe.management.security.JwtAuthenticationFilter;
import com.pfe.management.security.JwtService;
import com.pfe.management.security.PasswordEncoderConfig;
import com.pfe.management.security.SecurityConfig;
import com.pfe.management.service.ProjectMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
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

@WebMvcTest(ProjectMemberController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, JwtAuthenticationFilter.class, PasswordEncoderConfig.class})
@MockBean(JwtService.class)
@MockBean(com.pfe.management.security.CustomUserDetailsService.class)
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectMemberService projectMemberService;

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/members sans authentification retourne 401")
    void addMemberWithoutAuthReturns401() throws Exception {
        ProjectMemberRequest request = new ProjectMemberRequest(5L);

        mockMvc.perform(post("/api/v1/projects/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /api/v1/projects/{projectId}/members avec rôle STUDENT retourne 403")
    void addMemberAsStudentReturns403() throws Exception {
        ProjectMemberRequest request = new ProjectMemberRequest(5L);

        mockMvc.perform(post("/api/v1/projects/1/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/projects/{projectId}/members avec validation invalide retourne 400")
    void addMemberWithInvalidPayloadReturns400() throws Exception {
        ProjectMemberRequest request = new ProjectMemberRequest(null);

        mockMvc.perform(post("/api/v1/projects/1/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.userId").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/projects/{projectId}/members projet ou utilisateur introuvable retourne 404")
    void addMemberNotFoundReturns404() throws Exception {
        ProjectMemberRequest request = new ProjectMemberRequest(999L);
        when(projectMemberService.addMember(eq(1L), any(ProjectMemberRequest.class)))
                .thenThrow(new ResourceNotFoundException("Utilisateur introuvable"));

        mockMvc.perform(post("/api/v1/projects/1/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/projects/{projectId}/members déjà membre retourne 409 Conflict")
    void addMemberAlreadyMemberReturns409() throws Exception {
        ProjectMemberRequest request = new ProjectMemberRequest(5L);
        when(projectMemberService.addMember(eq(1L), any(ProjectMemberRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("L'utilisateur est déjà membre du projet"));

        mockMvc.perform(post("/api/v1/projects/1/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/projects/{projectId}/members avec données valides retourne 201 Created")
    void addMemberSuccessAsAdminReturns201() throws Exception {
        ProjectMemberRequest request = new ProjectMemberRequest(5L);
        ProjectMemberResponse response = new ProjectMemberResponse(
                5L, "Jean", "Dupont", "jean@pfe.com", Role.STUDENT, LocalDateTime.now()
        );

        when(projectMemberService.addMember(eq(1L), any(ProjectMemberRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/projects/1/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(5L))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.email").value("jean@pfe.com"));
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{projectId}/members/{userId} sans authentification retourne 401")
    void removeMemberWithoutAuthReturns401() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/1/members/5"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("DELETE /api/v1/projects/{projectId}/members/{userId} avec rôle STUDENT retourne 403")
    void removeMemberAsStudentReturns403() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/1/members/5").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/projects/{projectId}/members/{userId} membre inexistant retourne 404")
    void removeMemberNotFoundReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Membre introuvable"))
                .when(projectMemberService).removeMember(1L, 999L);

        mockMvc.perform(delete("/api/v1/projects/1/members/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    @DisplayName("DELETE /api/v1/projects/{projectId}/members/{userId} avec SUPERVISOR retourne 204 No Content")
    void removeMemberSuccessReturns204() throws Exception {
        doNothing().when(projectMemberService).removeMember(1L, 5L);

        mockMvc.perform(delete("/api/v1/projects/1/members/5").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/members sans authentification retourne 401")
    void getProjectMembersWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/projects/1/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /api/v1/projects/{projectId}/members pour non membre retourne 403")
    void getProjectMembersNotMemberReturns403() throws Exception {
        when(projectMemberService.getProjectMembers(1L))
                .thenThrow(new AccessDeniedException("Accès refusé"));

        mockMvc.perform(get("/api/v1/projects/1/members"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/projects/{projectId}/members pour projet inexistant retourne 404")
    void getProjectMembersNotFoundReturns404() throws Exception {
        when(projectMemberService.getProjectMembers(999L))
                .thenThrow(new ResourceNotFoundException("Projet introuvable"));

        mockMvc.perform(get("/api/v1/projects/999/members"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/projects/{projectId}/members avec authentification retourne 200 OK")
    void getProjectMembersSuccessReturns200() throws Exception {
        ProjectMemberResponse member = new ProjectMemberResponse(
                5L, "Jean", "Dupont", "jean@pfe.com", Role.STUDENT, LocalDateTime.now()
        );
        when(projectMemberService.getProjectMembers(1L)).thenReturn(List.of(member));

        mockMvc.perform(get("/api/v1/projects/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(5L))
                .andExpect(jsonPath("$[0].firstName").value("Jean"))
                .andExpect(jsonPath("$[0].role").value("STUDENT"));
    }
}

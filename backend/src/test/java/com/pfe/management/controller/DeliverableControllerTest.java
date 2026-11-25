package com.pfe.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.management.dto.request.DeliverableReviewRequest;
import com.pfe.management.dto.response.DeliverableResponse;
import com.pfe.management.dto.response.DeliverableFileResource;
import com.pfe.management.dto.response.DeliverableUserResponse;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.entity.DeliverableStatus;
import com.pfe.management.entity.DeliverableType;
import com.pfe.management.exception.BusinessRuleException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.security.CustomAccessDeniedHandler;
import com.pfe.management.security.CustomAuthenticationEntryPoint;
import com.pfe.management.security.JwtAuthenticationFilter;
import com.pfe.management.security.JwtService;
import com.pfe.management.security.PasswordEncoderConfig;
import com.pfe.management.security.SecurityConfig;
import com.pfe.management.service.DeliverableService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeliverableController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, JwtAuthenticationFilter.class, PasswordEncoderConfig.class})
@MockBean(JwtService.class)
@MockBean(com.pfe.management.security.CustomUserDetailsService.class)
class DeliverableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliverableService deliverableService;

    private DeliverableResponse sample(Long id, DeliverableStatus status) {
        return new DeliverableResponse(
                id, "RAPPORT_FINAL - Version 1", null, DeliverableType.RAPPORT_FINAL, 1,
                status, "rapport.pdf", "application/pdf", 1024L, null,
                LocalDateTime.now(), 1L, "Plateforme PFE",
                new DeliverableUserResponse(3L, "Alice Student"), null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private MockMultipartFile pdf() {
        return new MockMultipartFile("file", "rapport.pdf", "application/pdf", "contenu".getBytes());
    }

    // ---------- POST ----------

    @Test
    @DisplayName("POST /deliverables sans authentification retourne 401")
    void uploadWithoutAuthReturns401() throws Exception {
        mockMvc.perform(multipart("/api/v1/deliverables").file(pdf())
                        .param("projectId", "1").param("type", "RAPPORT_FINAL"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /deliverables valide retourne 201")
    void uploadSuccessReturns201() throws Exception {
        when(deliverableService.createDeliverable(eq(1L), eq(DeliverableType.RAPPORT_FINAL), any()))
                .thenReturn(sample(10L, DeliverableStatus.SUBMITTED));

        mockMvc.perform(multipart("/api/v1/deliverables").file(pdf())
                        .param("projectId", "1").param("type", "RAPPORT_FINAL"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /deliverables par un non-membre retourne 403")
    void uploadNotMemberReturns403() throws Exception {
        when(deliverableService.createDeliverable(any(), any(), any()))
                .thenThrow(new AccessDeniedException("non membre"));

        mockMvc.perform(multipart("/api/v1/deliverables").file(pdf())
                        .param("projectId", "1").param("type", "RAPPORT_FINAL"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /deliverables fichier trop gros retourne 413")
    void uploadTooLargeReturns413() throws Exception {
        when(deliverableService.createDeliverable(any(), any(), any()))
                .thenThrow(new MaxUploadSizeExceededException(20L * 1024 * 1024));

        mockMvc.perform(multipart("/api/v1/deliverables").file(pdf())
                        .param("projectId", "1").param("type", "RAPPORT_FINAL"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /deliverables extension interdite retourne 400")
    void uploadForbiddenExtensionReturns400() throws Exception {
        when(deliverableService.createDeliverable(any(), any(), any()))
                .thenThrow(new BusinessRuleException("Extension de fichier non autorisée (exe)"));

        MockMultipartFile exe = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "x".getBytes());

        mockMvc.perform(multipart("/api/v1/deliverables").file(exe)
                        .param("projectId", "1").param("type", "RAPPORT_FINAL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /deliverables sans projectId retourne 400")
    void uploadMissingProjectIdReturns400() throws Exception {
        mockMvc.perform(multipart("/api/v1/deliverables").file(pdf()).param("type", "RAPPORT_FINAL"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(deliverableService);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /deliverables avec type inconnu retourne 400")
    void uploadUnknownTypeReturns400() throws Exception {
        mockMvc.perform(multipart("/api/v1/deliverables").file(pdf())
                        .param("projectId", "1").param("type", "INCONNU"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(deliverableService);
    }

    // ---------- GET list ----------

    @Test
    @DisplayName("GET /deliverables sans authentification retourne 401")
    void listWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/deliverables")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /deliverables?projectId=1 retourne 200 et la page")
    void listByProjectReturns200() throws Exception {
        PageResponse<DeliverableResponse> page =
                new PageResponse<>(List.of(sample(10L, DeliverableStatus.SUBMITTED)), 0, 10, 1, 1, true, true);
        when(deliverableService.getAllDeliverables(eq(1L), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/deliverables").param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /deliverables?projectId= d'un projet non membre retourne 403")
    void listForbiddenProjectReturns403() throws Exception {
        when(deliverableService.getAllDeliverables(eq(99L), any(), any(Pageable.class)))
                .thenThrow(new AccessDeniedException("non membre"));

        mockMvc.perform(get("/api/v1/deliverables").param("projectId", "99"))
                .andExpect(status().isForbidden());
    }

    // ---------- GET download ----------

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /deliverables/{id}/download retourne 200 avec pièce jointe")
    void downloadSuccessReturns200() throws Exception {
        byte[] bytes = "contenu".getBytes();
        when(deliverableService.downloadDeliverable(10L)).thenReturn(
                new DeliverableFileResource(new ByteArrayResource(bytes), "rapport.pdf", "application/pdf", bytes.length));

        mockMvc.perform(get("/api/v1/deliverables/10/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().bytes(bytes));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /deliverables/{id}/download inexistant retourne 404")
    void downloadNotFoundReturns404() throws Exception {
        when(deliverableService.downloadDeliverable(99L)).thenThrow(new ResourceNotFoundException("introuvable"));

        mockMvc.perform(get("/api/v1/deliverables/99/download"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /deliverables/{id}/download par un non-membre retourne 403")
    void downloadForbiddenReturns403() throws Exception {
        when(deliverableService.downloadDeliverable(10L)).thenThrow(new AccessDeniedException("non membre"));

        mockMvc.perform(get("/api/v1/deliverables/10/download"))
                .andExpect(status().isForbidden());
    }

    // ---------- PATCH review ----------

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("PATCH /review par un STUDENT retourne 403 sans appeler le service")
    void reviewAsStudentReturns403() throws Exception {
        mockMvc.perform(patch("/api/v1/deliverables/10/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeliverableReviewRequest(DeliverableStatus.APPROVED, "ok"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(deliverableService);
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    @DisplayName("PATCH /review APPROVED par un SUPERVISOR retourne 200")
    void reviewApprovedReturns200() throws Exception {
        when(deliverableService.reviewDeliverable(eq(10L), any(DeliverableReviewRequest.class)))
                .thenReturn(sample(10L, DeliverableStatus.APPROVED));

        mockMvc.perform(patch("/api/v1/deliverables/10/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeliverableReviewRequest(DeliverableStatus.APPROVED, "Bien"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    @DisplayName("PATCH /review REJECTED sans commentaire retourne 400")
    void reviewRejectedWithoutCommentReturns400() throws Exception {
        when(deliverableService.reviewDeliverable(eq(10L), any(DeliverableReviewRequest.class)))
                .thenThrow(new BusinessRuleException("Un commentaire est obligatoire en cas de refus du livrable"));

        mockMvc.perform(patch("/api/v1/deliverables/10/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeliverableReviewRequest(DeliverableStatus.REJECTED, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    @DisplayName("PATCH /review sans statut retourne 400")
    void reviewMissingStatusReturns400() throws Exception {
        mockMvc.perform(patch("/api/v1/deliverables/10/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"x\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(deliverableService);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /review sur livrable inexistant retourne 404")
    void reviewNotFoundReturns404() throws Exception {
        when(deliverableService.reviewDeliverable(eq(99L), any(DeliverableReviewRequest.class)))
                .thenThrow(new ResourceNotFoundException("introuvable"));

        mockMvc.perform(patch("/api/v1/deliverables/99/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeliverableReviewRequest(DeliverableStatus.APPROVED, "ok"))))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE ----------

    @Test
    @DisplayName("DELETE /deliverables/{id} sans authentification retourne 401")
    void deleteWithoutAuthReturns401() throws Exception {
        mockMvc.perform(delete("/api/v1/deliverables/10")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("DELETE /deliverables/{id} retourne 204")
    void deleteReturns204() throws Exception {
        doNothing().when(deliverableService).deleteDeliverable(10L);

        mockMvc.perform(delete("/api/v1/deliverables/10")).andExpect(status().isNoContent());
        verify(deliverableService).deleteDeliverable(10L);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("DELETE /deliverables/{id} d'un autre étudiant retourne 403")
    void deleteForbiddenReturns403() throws Exception {
        doThrow(new AccessDeniedException("non auteur")).when(deliverableService).deleteDeliverable(10L);

        mockMvc.perform(delete("/api/v1/deliverables/10")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /deliverables/{id} inexistant retourne 404")
    void deleteNotFoundReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("introuvable")).when(deliverableService).deleteDeliverable(99L);

        mockMvc.perform(delete("/api/v1/deliverables/99")).andExpect(status().isNotFound());
    }
}
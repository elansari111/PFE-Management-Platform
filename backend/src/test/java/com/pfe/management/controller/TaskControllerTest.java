package com.pfe.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.management.dto.request.TaskCreateRequest;
import com.pfe.management.dto.request.TaskStatusRequest;
import com.pfe.management.dto.request.TaskUpdateRequest;
import com.pfe.management.dto.response.AssignedUserResponse;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.TaskResponse;
import com.pfe.management.entity.TaskPriority;
import com.pfe.management.entity.TaskStatus;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.security.CustomAccessDeniedHandler;
import com.pfe.management.security.CustomAuthenticationEntryPoint;
import com.pfe.management.security.JwtAuthenticationFilter;
import com.pfe.management.security.JwtService;
import com.pfe.management.security.PasswordEncoderConfig;
import com.pfe.management.security.SecurityConfig;
import com.pfe.management.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class, JwtAuthenticationFilter.class, PasswordEncoderConfig.class})
@MockBean(JwtService.class)
@MockBean(com.pfe.management.security.CustomUserDetailsService.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private TaskResponse createSampleTaskResponse(Long id, String title, TaskStatus status) {
        return new TaskResponse(
                id, title, "Description", status, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(10), 1L, "Plateforme PFE",
                new AssignedUserResponse(3L, "Alice Student"),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/tasks sans authentification retourne 401")
    void getAllTasksWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /api/v1/tasks avec authentification retourne 200 OK et liste paginée")
    void getAllTasksWithAuthReturns200() throws Exception {
        TaskResponse sample = createSampleTaskResponse(1L, "Tâche 1", TaskStatus.TODO);
        PageResponse<TaskResponse> pageResponse = new PageResponse<>(
                List.of(sample), 0, 10, 1, 1, true, true
        );

        when(taskService.getAllTasks(any(), any(), any(), any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Tâche 1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} sans authentification retourne 401")
    void getTaskByIdWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/tasks/{id} tâche inexistante retourne 404")
    void getTaskByIdNotFoundReturns404() throws Exception {
        when(taskService.getTaskById(999L)).thenThrow(new ResourceNotFoundException("Tâche introuvable"));

        mockMvc.perform(get("/api/v1/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /api/v1/tasks/{id} non membre retourne 403 Forbidden")
    void getTaskByIdNotMemberReturns403() throws Exception {
        when(taskService.getTaskById(1L)).thenThrow(new AccessDeniedException("Accès refusé"));

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/tasks/{id} avec tâche existante retourne 200 OK")
    void getTaskByIdFoundReturns200() throws Exception {
        TaskResponse sample = createSampleTaskResponse(1L, "Tâche 1", TaskStatus.TODO);
        when(taskService.getTaskById(1L)).thenReturn(sample);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Tâche 1"))
                .andExpect(jsonPath("$.projectTitle").value("Plateforme PFE"))
                .andExpect(jsonPath("$.assignedTo.fullName").value("Alice Student"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks sans authentification retourne 401")
    void createTaskWithoutAuthReturns401() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
                "Nouvelle Tâche", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5), 1L, null
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /api/v1/tasks avec rôle STUDENT retourne 403 Forbidden")
    void createTaskAsStudentReturns403() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
                "Nouvelle Tâche", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5), 1L, null
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/tasks avec validation invalide retourne 400 Bad Request")
    void createTaskInvalidValidationReturns400() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
                "", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5), null, null // titre vide et projectId null
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.projectId").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/tasks avec projet inexistant retourne 404")
    void createTaskProjectNotFoundReturns404() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
                "Nouvelle Tâche", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5), 999L, null
        );

        when(taskService.createTask(any(TaskCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Projet introuvable"));

        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/tasks avec données valides retourne 201 Created")
    void createTaskSuccessReturns201() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
                "Nouvelle Tâche", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5), 1L, 3L
        );
        TaskResponse response = createSampleTaskResponse(10L, "Nouvelle Tâche", TaskStatus.TODO);

        when(taskService.createTask(any(TaskCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Nouvelle Tâche"));
    }

    @Test
    @DisplayName("PUT /api/v1/tasks/{id} sans authentification retourne 401")
    void updateTaskWithoutAuthReturns401() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Titre Modifié", "Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                LocalDate.now().plusDays(5), null
        );

        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("PUT /api/v1/tasks/{id} avec rôle STUDENT retourne 403 Forbidden")
    void updateTaskAsStudentReturns403() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Titre Modifié", "Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                LocalDate.now().plusDays(5), null
        );

        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PUT /api/v1/tasks/{id} avec titre vide retourne 400 Bad Request")
    void updateTaskValidationFailsReturns400() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "", "Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                LocalDate.now().plusDays(5), null
        );

        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PUT /api/v1/tasks/{id} tâche inexistante retourne 404")
    void updateTaskNotFoundReturns404() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Titre Valide", "Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                LocalDate.now().plusDays(5), null
        );

        when(taskService.updateTask(eq(999L), any(TaskUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Tâche introuvable"));

        mockMvc.perform(put("/api/v1/tasks/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PUT /api/v1/tasks/{id} avec données valides retourne 200 OK")
    void updateTaskSuccessReturns200() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Titre Modifié", "Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                LocalDate.now().plusDays(5), null
        );
        TaskResponse response = createSampleTaskResponse(1L, "Titre Modifié", TaskStatus.IN_PROGRESS);

        when(taskService.updateTask(eq(1L), any(TaskUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Titre Modifié"));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/status sans authentification retourne 401")
    void updateTaskStatusWithoutAuthReturns401() throws Exception {
        TaskStatusRequest request = new TaskStatusRequest(TaskStatus.DONE);

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("PATCH /api/v1/tasks/{id}/status avec statut null retourne 400 Bad Request")
    void updateTaskStatusValidationFailsReturns400() throws Exception {
        TaskStatusRequest request = new TaskStatusRequest(null);

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.status").exists());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("PATCH /api/v1/tasks/{id}/status tâche inexistante retourne 404")
    void updateTaskStatusNotFoundReturns404() throws Exception {
        TaskStatusRequest request = new TaskStatusRequest(TaskStatus.DONE);
        when(taskService.updateTaskStatus(eq(999L), any(TaskStatusRequest.class)))
                .thenThrow(new ResourceNotFoundException("Tâche introuvable"));

        mockMvc.perform(patch("/api/v1/tasks/999/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("PATCH /api/v1/tasks/{id}/status tâche non assignée à l'étudiant retourne 403 Forbidden")
    void updateTaskStatusForbiddenReturns403() throws Exception {
        TaskStatusRequest request = new TaskStatusRequest(TaskStatus.DONE);
        when(taskService.updateTaskStatus(eq(1L), any(TaskStatusRequest.class)))
                .thenThrow(new AccessDeniedException("Un étudiant ne peut modifier que le statut des tâches qui lui sont assignées"));

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("PATCH /api/v1/tasks/{id}/status avec succès retourne 200 OK")
    void updateTaskStatusSuccessReturns200() throws Exception {
        TaskStatusRequest request = new TaskStatusRequest(TaskStatus.DONE);
        TaskResponse response = createSampleTaskResponse(1L, "Tâche 1", TaskStatus.DONE);

        when(taskService.updateTaskStatus(eq(1L), any(TaskStatusRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{id} sans authentification retourne 401")
    void deleteTaskWithoutAuthReturns401() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("DELETE /api/v1/tasks/{id} avec rôle STUDENT retourne 403 Forbidden")
    void deleteTaskAsStudentReturns403() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/tasks/{id} tâche inexistante retourne 404")
    void deleteTaskNotFoundReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Tâche introuvable")).when(taskService).deleteTask(999L);

        mockMvc.perform(delete("/api/v1/tasks/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/tasks/{id} avec succès retourne 204 No Content")
    void deleteTaskSuccessReturns204() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}

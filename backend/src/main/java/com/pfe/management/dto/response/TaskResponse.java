package com.pfe.management.dto.response;

import com.pfe.management.entity.TaskPriority;
import com.pfe.management.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Informations détaillées sur une tâche")
public class TaskResponse {

    @Schema(description = "Identifiant unique de la tâche", example = "1")
    private Long id;

    @Schema(description = "Titre de la tâche", example = "Implémenter l'authentification JWT")
    private String title;

    @Schema(description = "Description de la tâche", example = "Créer les filtres et configurer Spring Security")
    private String description;

    @Schema(description = "Statut actuel de la tâche", example = "TODO")
    private TaskStatus status;

    @Schema(description = "Priorité de la tâche", example = "MEDIUM")
    private TaskPriority priority;

    @Schema(description = "Date d'échéance", example = "2026-12-31")
    private LocalDate dueDate;

    @Schema(description = "Identifiant du projet associé", example = "1")
    private Long projectId;

    @Schema(description = "Titre du projet associé", example = "Plateforme PFE")
    private String projectTitle;

    @Schema(description = "Utilisateur assigné à la tâche")
    private AssignedUserResponse assignedTo;

    @Schema(description = "Date de création", example = "2026-10-27T18:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière mise à jour", example = "2026-10-27T18:00:00")
    private LocalDateTime updatedAt;

    public TaskResponse() {
    }

    public TaskResponse(Long id, String title, String description, TaskStatus status, TaskPriority priority,
                        LocalDate dueDate, Long projectId, String projectTitle, AssignedUserResponse assignedTo,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.assignedTo = assignedTo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public AssignedUserResponse getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(AssignedUserResponse assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

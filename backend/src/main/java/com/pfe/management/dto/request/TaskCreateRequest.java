package com.pfe.management.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.pfe.management.entity.TaskPriority;
import com.pfe.management.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Données pour la création d'une tâche")
public class TaskCreateRequest {

    @NotBlank(message = "Le titre de la tâche est obligatoire")
    @Size(max = 200, message = "Le titre de la tâche ne doit pas dépasser 200 caractères")
    @Schema(description = "Titre de la tâche", example = "Implémenter l'authentification JWT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Description de la tâche", example = "Créer les filtres et configurer Spring Security")
    private String description;

    @Schema(description = "Statut initial de la tâche", example = "TODO")
    private TaskStatus status;

    @Schema(description = "Priorité de la tâche", example = "MEDIUM")
    private TaskPriority priority;

    @Schema(description = "Date d'échéance de la tâche", example = "2026-12-31")
    private LocalDate dueDate;

    @NotNull(message = "L'identifiant du projet est obligatoire")
    @Schema(description = "Identifiant du projet auquel appartient la tâche", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long projectId;

    @JsonAlias({"assignedTo", "assignedToId"})
    @Schema(description = "Identifiant de l'utilisateur assigné (doit être membre du projet)", example = "3")
    private Long assignedToId;

    public TaskCreateRequest() {
    }

    public TaskCreateRequest(String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate, Long projectId, Long assignedToId) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.projectId = projectId;
        this.assignedToId = assignedToId;
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

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public Long getAssignedTo() {
        return assignedToId;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedToId = assignedTo;
    }
}

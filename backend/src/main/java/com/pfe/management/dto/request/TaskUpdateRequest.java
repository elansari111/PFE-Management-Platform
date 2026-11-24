package com.pfe.management.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.pfe.management.entity.TaskPriority;
import com.pfe.management.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Données pour la mise à jour complète d'une tâche")
public class TaskUpdateRequest {

    @NotBlank(message = "Le titre de la tâche est obligatoire")
    @Size(max = 200, message = "Le titre de la tâche ne doit pas dépasser 200 caractères")
    @Schema(description = "Titre de la tâche", example = "Implémenter l'authentification JWT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Description de la tâche", example = "Mise à jour de la documentation et des tests")
    private String description;

    @Schema(description = "Statut de la tâche", example = "IN_PROGRESS")
    private TaskStatus status;

    @Schema(description = "Priorité de la tâche", example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "Date d'échéance", example = "2026-12-31")
    private LocalDate dueDate;

    @JsonAlias({"assignedTo", "assignedToId"})
    @Schema(description = "Identifiant de l'utilisateur assigné (doit être membre du projet)", example = "3")
    private Long assignedToId;

    public TaskUpdateRequest() {
    }

    public TaskUpdateRequest(String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate, Long assignedToId) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
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

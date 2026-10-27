package com.pfe.management.dto.response;

import com.pfe.management.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Représentation publique d'un projet PFE")
public class ProjectResponse {

    @Schema(description = "Identifiant unique du projet", example = "1")
    private Long id;

    @Schema(description = "Titre du projet", example = "Plateforme de gestion PFE")
    private String title;

    @Schema(description = "Description détaillée", example = "Application de suivi et de gestion des soutenances")
    private String description;

    @Schema(description = "Statut actuel du projet", example = "PROPOSED")
    private ProjectStatus status;

    @Schema(description = "Date de début prévue", example = "2026-11-01")
    private LocalDate startDate;

    @Schema(description = "Date de fin prévue", example = "2027-06-30")
    private LocalDate endDate;

    @Schema(description = "Nombre de membres assignés", example = "2")
    private int membersCount;

    @Schema(description = "Nombre de tâches", example = "5")
    private int tasksCount;

    @Schema(description = "Nombre de livrables", example = "1")
    private int deliverablesCount;

    @Schema(description = "Date de création", example = "2026-10-27T18:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière mise à jour", example = "2026-10-27T18:00:00")
    private LocalDateTime updatedAt;

    public ProjectResponse() {
    }

    public ProjectResponse(Long id, String title, String description, ProjectStatus status, LocalDate startDate, LocalDate endDate, int membersCount, int tasksCount, int deliverablesCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.membersCount = membersCount;
        this.tasksCount = tasksCount;
        this.deliverablesCount = deliverablesCount;
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

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    public int getTasksCount() {
        return tasksCount;
    }

    public void setTasksCount(int tasksCount) {
        this.tasksCount = tasksCount;
    }

    public int getDeliverablesCount() {
        return deliverablesCount;
    }

    public void setDeliverablesCount(int deliverablesCount) {
        this.deliverablesCount = deliverablesCount;
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

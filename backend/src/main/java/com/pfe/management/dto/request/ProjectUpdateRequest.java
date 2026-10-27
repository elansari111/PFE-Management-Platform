package com.pfe.management.dto.request;

import com.pfe.management.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Données pour la modification d'un projet PFE")
public class ProjectUpdateRequest {

    @NotBlank(message = "Le titre du projet ne peut pas être vide")
    @Size(min = 3, max = 200, message = "Le titre doit comporter entre 3 et 200 caractères")
    @Schema(description = "Titre mis à jour", example = "Plateforme de gestion PFE - V2")
    private String title;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    @Schema(description = "Description mise à jour", example = "Description enrichie du projet")
    private String description;

    @Schema(description = "Nouveau statut", example = "IN_PROGRESS")
    private ProjectStatus status;

    @Schema(description = "Date de début mise à jour", example = "2026-11-01")
    private LocalDate startDate;

    @Schema(description = "Date de fin mise à jour", example = "2027-06-30")
    private LocalDate endDate;

    public ProjectUpdateRequest() {
    }

    public ProjectUpdateRequest(String title, String description, ProjectStatus status, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
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
}

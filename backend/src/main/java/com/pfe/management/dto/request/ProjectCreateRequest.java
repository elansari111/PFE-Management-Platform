package com.pfe.management.dto.request;

import com.pfe.management.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Données pour la création d'un projet PFE")
public class ProjectCreateRequest {

    @NotBlank(message = "Le titre du projet est obligatoire")
    @Size(min = 3, max = 200, message = "Le titre doit comporter entre 3 et 200 caractères")
    @Schema(description = "Titre du projet", example = "Plateforme de gestion PFE")
    private String title;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    @Schema(description = "Description détaillée du sujet de PFE", example = "Application collaborative de gestion des sujets, encadrements et soutenances")
    private String description;

    @Schema(description = "Statut initial (par défaut PROPOSED)", example = "PROPOSED")
    private ProjectStatus status;

    @Schema(description = "Date de début prévue", example = "2026-11-01")
    private LocalDate startDate;

    @Schema(description = "Date de fin prévue", example = "2027-06-30")
    private LocalDate endDate;

    public ProjectCreateRequest() {
    }

    public ProjectCreateRequest(String title, String description, ProjectStatus status, LocalDate startDate, LocalDate endDate) {
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

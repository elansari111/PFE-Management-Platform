package com.pfe.management.dto.response;

import com.pfe.management.entity.DeliverableStatus;
import com.pfe.management.entity.DeliverableType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informations détaillées sur un livrable")
public class DeliverableResponse {

    @Schema(description = "Identifiant unique du livrable", example = "1")
    private Long id;

    @Schema(description = "Titre du livrable", example = "Rapport intermédiaire - Version 1")
    private String title;

    @Schema(description = "Description optionnelle", example = "Première version du rapport d'avancement")
    private String description;

    @Schema(description = "Type de livrable", example = "RAPPORT_INTERMEDIAIRE")
    private DeliverableType type;

    @Schema(description = "Numéro de version séquentiel", example = "1")
    private Integer version;

    @Schema(description = "Statut du livrable", example = "SUBMITTED")
    private DeliverableStatus status;

    @Schema(description = "Nom original du fichier", example = "rapport_pfe.pdf")
    private String originalFilename;

    @Schema(description = "Type MIME du fichier", example = "application/pdf")
    private String contentType;

    @Schema(description = "Taille du fichier en octets", example = "1048576")
    private Long fileSize;

    @Schema(description = "Commentaire de validation ou de rejet", example = "Validé par l'encadrant.")
    private String comment;

    @Schema(description = "Date et heure de dépôt", example = "2026-11-24T18:00:00")
    private LocalDateTime submissionDate;

    @Schema(description = "Identifiant du projet associé", example = "1")
    private Long projectId;

    @Schema(description = "Titre du projet associé", example = "Plateforme PFE")
    private String projectTitle;

    @Schema(description = "Étudiant ayant déposé le livrable")
    private DeliverableUserResponse createdBy;

    @Schema(description = "Utilisateur ayant révisé le livrable")
    private DeliverableUserResponse reviewedBy;

    @Schema(description = "Date et heure de la révision", example = "2026-11-25T10:00:00")
    private LocalDateTime reviewedAt;

    @Schema(description = "Date de création", example = "2026-11-24T18:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification", example = "2026-11-24T18:00:00")
    private LocalDateTime updatedAt;

    public DeliverableResponse() {
    }

    public DeliverableResponse(Long id, String title, String description, DeliverableType type, Integer version,
                               DeliverableStatus status, String originalFilename, String contentType,
                               Long fileSize, String comment, LocalDateTime submissionDate,
                               Long projectId, String projectTitle, DeliverableUserResponse createdBy,
                               DeliverableUserResponse reviewedBy, LocalDateTime reviewedAt,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.version = version;
        this.status = status;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.comment = comment;
        this.submissionDate = submissionDate;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.createdBy = createdBy;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
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

    public DeliverableType getType() {
        return type;
    }

    public void setType(DeliverableType type) {
        this.type = type;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public DeliverableStatus getStatus() {
        return status;
    }

    public void setStatus(DeliverableStatus status) {
        this.status = status;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
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

    public DeliverableUserResponse getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(DeliverableUserResponse createdBy) {
        this.createdBy = createdBy;
    }

    public DeliverableUserResponse getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(DeliverableUserResponse reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
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

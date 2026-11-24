package com.pfe.management.dto.response;

import com.pfe.management.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informations sur un membre d'un projet")
public class ProjectMemberResponse {

    @Schema(description = "Identifiant de l'utilisateur", example = "5")
    private Long userId;

    @Schema(description = "Prénom de l'utilisateur", example = "Jean")
    private String firstName;

    @Schema(description = "Nom de l'utilisateur", example = "Dupont")
    private String lastName;

    @Schema(description = "Adresse email de l'utilisateur", example = "jean.dupont@pfe.com")
    private String email;

    @Schema(description = "Rôle de l'utilisateur", example = "STUDENT")
    private Role role;

    @Schema(description = "Date d'adhésion au projet", example = "2026-10-27T18:00:00")
    private LocalDateTime joinedAt;

    public ProjectMemberResponse() {
    }

    public ProjectMemberResponse(Long userId, String firstName, String lastName, String email, Role role, LocalDateTime joinedAt) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}

package com.pfe.management.dto.request;

import com.pfe.management.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Données pour la création d'un utilisateur par un administrateur")
public class UserCreateRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit comporter entre 2 et 100 caractères")
    @Schema(description = "Prénom de l'utilisateur", example = "Alice", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit comporter entre 2 et 100 caractères")
    @Schema(description = "Nom de l'utilisateur", example = "Student", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    @Size(max = 180, message = "L'email ne peut pas dépasser 180 caractères")
    @Schema(description = "Adresse email unique de l'utilisateur", example = "alice@pfe.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit comporter au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "Le mot de passe doit contenir au moins une majuscule et un chiffre")
    @Schema(description = "Mot de passe en clair (sera encodé avec BCrypt)", example = "Password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    @Schema(description = "Rôle attribué à l'utilisateur", example = "STUDENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private Role role;

    public UserCreateRequest() {
    }

    public UserCreateRequest(String firstName, String lastName, String email, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

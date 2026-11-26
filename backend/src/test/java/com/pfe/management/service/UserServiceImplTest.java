package com.pfe.management.service;

import com.pfe.management.dto.request.UserActiveUpdateRequest;
import com.pfe.management.dto.request.UserCreateRequest;
import com.pfe.management.dto.request.UserRoleUpdateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.UserResponse;
import com.pfe.management.entity.Role;
import com.pfe.management.entity.User;
import com.pfe.management.exception.BusinessRuleException;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.mapper.UserMapper;
import com.pfe.management.repository.UserRepository;
import com.pfe.management.security.CurrentUserService;
import com.pfe.management.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserService currentUserService;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private UserServiceImpl userService;

    private User adminUser;
    private User studentUser;

    @BeforeEach
    void setUp() {
        adminUser = new User("Admin", "Root", "admin@pfe.com", "encoded", Role.ADMIN);
        adminUser.setId(1L);
        adminUser.setCreatedAt(LocalDateTime.now());

        studentUser = new User("Alice", "Student", "alice@pfe.com", "encoded", Role.STUDENT);
        studentUser.setId(5L);
        studentUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Liste filtrée par rôle et par recherche (prénom/nom/email)")
    void getAllUsersFiltersByRoleAndQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(studentUser), pageable, 1);

        when(userRepository.search(eq(Role.STUDENT), eq("%ali%"), any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponse> result = userService.getAllUsers(Role.STUDENT, "Ali", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("alice@pfe.com");
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).search(eq(Role.STUDENT), eq("%ali%"), any(Pageable.class));
    }

    @Test
    @DisplayName("Création d'un utilisateur avec succès : email normalisé, mot de passe encodé, compte actif")
    void createUserSuccess() {
        UserCreateRequest request = new UserCreateRequest(
                "Alice", "Student", "Alice@PFE.com", "Password123", Role.STUDENT
        );

        when(userRepository.existsByEmail("alice@pfe.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(5L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("alice@pfe.com");
        assertThat(response.getRole()).isEqualTo("STUDENT");
        assertThat(response.isActive()).isTrue();
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Création refusée si l'email est déjà utilisé (ResourceAlreadyExistsException)")
    void createUserDuplicateEmailThrowsConflict() {
        UserCreateRequest request = new UserCreateRequest(
                "Alice", "Student", "alice@pfe.com", "Password123", Role.STUDENT
        );

        when(userRepository.existsByEmail("alice@pfe.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("existe déjà");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Changement de rôle d'un autre utilisateur avec succès")
    void updateUserRoleSuccess() {
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.SUPERVISOR);

        when(userRepository.findById(5L)).thenReturn(Optional.of(studentUser));
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(userRepository.save(any(User.class))).thenReturn(studentUser);

        UserResponse response = userService.updateUserRole(5L, request);

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo("SUPERVISOR");
        assertThat(studentUser.getRole()).isEqualTo(Role.SUPERVISOR);
        verify(userRepository).save(studentUser);
    }

    @Test
    @DisplayName("Auto-retrait du rôle ADMIN refusé (BusinessRuleException)")
    void updateUserRoleSelfRemovalOfAdminThrowsBusinessRule() {
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.STUDENT);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(currentUserService.getCurrentUserId()).thenReturn(1L);

        assertThatThrownBy(() -> userService.updateUserRole(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("retirer son propre rôle ADMIN");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Changement de rôle d'un utilisateur inexistant lève ResourceNotFoundException")
    void updateUserRoleNotFoundThrows404() {
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.SUPERVISOR);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

    @Test
    @DisplayName("Auto-désactivation de l'administrateur refusée (BusinessRuleException)")
    void updateUserActiveSelfDeactivationThrowsBusinessRule() {
        UserActiveUpdateRequest request = new UserActiveUpdateRequest(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(currentUserService.getCurrentUserId()).thenReturn(1L);

        assertThatThrownBy(() -> userService.updateUserActive(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("désactiver son propre compte");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Désactivation d'un autre utilisateur avec succès")
    void updateUserActiveDeactivateSuccess() {
        UserActiveUpdateRequest request = new UserActiveUpdateRequest(false);

        when(userRepository.findById(5L)).thenReturn(Optional.of(studentUser));
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(userRepository.save(any(User.class))).thenReturn(studentUser);

        UserResponse response = userService.updateUserActive(5L, request);

        assertThat(response).isNotNull();
        assertThat(response.isActive()).isFalse();
        assertThat(studentUser.isActive()).isFalse();
        verify(userRepository).save(studentUser);
    }
}

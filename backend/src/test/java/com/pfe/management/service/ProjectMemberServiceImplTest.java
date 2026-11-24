package com.pfe.management.service;

import com.pfe.management.dto.request.ProjectMemberRequest;
import com.pfe.management.dto.response.ProjectMemberResponse;
import com.pfe.management.entity.Project;
import com.pfe.management.entity.ProjectMember;
import com.pfe.management.entity.ProjectStatus;
import com.pfe.management.entity.Role;
import com.pfe.management.entity.User;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.repository.ProjectMemberRepository;
import com.pfe.management.repository.ProjectRepository;
import com.pfe.management.repository.UserRepository;
import com.pfe.management.security.CurrentUserService;
import com.pfe.management.service.impl.ProjectMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ProjectMemberServiceImpl projectMemberService;

    private Project sampleProject;
    private User supervisorUser;
    private User studentUser;
    private ProjectMember sampleMember;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Plateforme PFE", "Description", ProjectStatus.IN_PROGRESS, LocalDate.now(), LocalDate.now().plusMonths(6));
        sampleProject.setId(1L);

        supervisorUser = new User("Sarah", "Supervisor", "supervisor@pfe.com", "pass", Role.SUPERVISOR);
        supervisorUser.setId(2L);

        studentUser = new User("Jean", "Dupont", "jean@pfe.com", "pass", Role.STUDENT);
        studentUser.setId(5L);

        sampleMember = new ProjectMember(sampleProject, studentUser);
        sampleMember.setId(100L);
    }

    @Test
    @DisplayName("Ajout d'un membre avec succès par un administrateur")
    void addMemberSuccessAsAdmin() {
        ProjectMemberRequest request = new ProjectMemberRequest(5L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isSupervisor()).thenReturn(false);
        when(userRepository.findById(5L)).thenReturn(Optional.of(studentUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 5L)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(sampleMember);

        ProjectMemberResponse response = projectMemberService.addMember(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(5L);
        assertThat(response.getFirstName()).isEqualTo("Jean");
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    @DisplayName("Ajout d'un membre avec succès par un encadrant membre du projet")
    void addMemberSuccessAsSupervisorInProject() {
        ProjectMemberRequest request = new ProjectMemberRequest(5L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(true);
        when(userRepository.findById(5L)).thenReturn(Optional.of(studentUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 5L)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(sampleMember);

        ProjectMemberResponse response = projectMemberService.addMember(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(5L);
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    @DisplayName("Échec d'ajout de membre si l'encadrant n'est pas membre du projet (AccessDeniedException)")
    void addMemberAsSupervisorNotInProjectThrowsAccessDeniedException() {
        ProjectMemberRequest request = new ProjectMemberRequest(5L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> projectMemberService.addMember(1L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Un encadrant ne peut gérer que les membres des projets dont il est membre");

        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    @DisplayName("Échec d'ajout si le projet est introuvable (ResourceNotFoundException)")
    void addMemberProjectNotFoundThrowsResourceNotFoundException() {
        ProjectMemberRequest request = new ProjectMemberRequest(5L);

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectMemberService.addMember(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Projet introuvable");
    }

    @Test
    @DisplayName("Échec d'ajout si l'utilisateur est introuvable (ResourceNotFoundException)")
    void addMemberUserNotFoundThrowsResourceNotFoundException() {
        ProjectMemberRequest request = new ProjectMemberRequest(999L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isSupervisor()).thenReturn(false);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectMemberService.addMember(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

    @Test
    @DisplayName("Échec d'ajout si l'utilisateur est déjà membre du projet (ResourceAlreadyExistsException)")
    void addMemberAlreadyMemberThrowsResourceAlreadyExistsException() {
        ProjectMemberRequest request = new ProjectMemberRequest(5L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isSupervisor()).thenReturn(false);
        when(userRepository.findById(5L)).thenReturn(Optional.of(studentUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> projectMemberService.addMember(1L, request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("déjà membre");
    }

    @Test
    @DisplayName("Retrait d'un membre avec succès par un administrateur")
    void removeMemberSuccessAsAdmin() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(currentUserService.isSupervisor()).thenReturn(false);
        when(projectMemberRepository.findByProjectIdAndUserId(1L, 5L)).thenReturn(Optional.of(sampleMember));

        projectMemberService.removeMember(1L, 5L);

        verify(projectMemberRepository).delete(sampleMember);
    }

    @Test
    @DisplayName("Retrait d'un membre avec succès par un encadrant membre du projet")
    void removeMemberSuccessAsSupervisorInProject() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(true);
        when(projectMemberRepository.findByProjectIdAndUserId(1L, 5L)).thenReturn(Optional.of(sampleMember));

        projectMemberService.removeMember(1L, 5L);

        verify(projectMemberRepository).delete(sampleMember);
    }

    @Test
    @DisplayName("Échec de retrait si l'encadrant n'est pas membre du projet (AccessDeniedException)")
    void removeMemberAsSupervisorNotInProjectThrowsAccessDeniedException() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> projectMemberService.removeMember(1L, 5L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Un encadrant ne peut gérer que les membres des projets dont il est membre");

        verify(projectMemberRepository, never()).delete(any(ProjectMember.class));
    }

    @Test
    @DisplayName("Échec de retrait si le membre n'existe pas dans le projet (ResourceNotFoundException)")
    void removeMemberNotFoundThrowsResourceNotFoundException() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(currentUserService.isSupervisor()).thenReturn(false);
        when(projectMemberRepository.findByProjectIdAndUserId(1L, 999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectMemberService.removeMember(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Membre introuvable");
    }

    @Test
    @DisplayName("Lister les membres d'un projet pour un administrateur")
    void getProjectMembersSuccessAsAdmin() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(true);
        when(projectMemberRepository.findByProjectId(1L)).thenReturn(List.of(sampleMember));

        List<ProjectMemberResponse> members = projectMemberService.getProjectMembers(1L);

        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUserId()).isEqualTo(5L);
        assertThat(members.get(0).getEmail()).isEqualTo("jean@pfe.com");
    }

    @Test
    @DisplayName("Lister les membres d'un projet pour un membre du projet")
    void getProjectMembersSuccessAsMember() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(5L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 5L)).thenReturn(true);
        when(projectMemberRepository.findByProjectId(1L)).thenReturn(List.of(sampleMember));

        List<ProjectMemberResponse> members = projectMemberService.getProjectMembers(1L);

        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUserId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Échec de consultation si l'utilisateur n'est ni ADMIN ni membre du projet (AccessDeniedException)")
    void getProjectMembersNotMemberThrowsAccessDeniedException() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(99L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> projectMemberService.getProjectMembers(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Accès refusé");
    }
}

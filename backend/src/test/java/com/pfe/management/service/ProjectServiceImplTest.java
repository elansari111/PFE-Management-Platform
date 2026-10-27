package com.pfe.management.service;

import com.pfe.management.dto.request.ProjectCreateRequest;
import com.pfe.management.dto.response.ProjectResponse;
import com.pfe.management.entity.Project;
import com.pfe.management.entity.ProjectStatus;
import com.pfe.management.exception.BusinessRuleException;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.mapper.ProjectMapper;
import com.pfe.management.repository.ProjectRepository;
import com.pfe.management.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    private ProjectMapper projectMapper;
    private ProjectServiceImpl projectService;
    private Project sampleProject;

    @BeforeEach
    void setUp() {
        projectMapper = new ProjectMapper();
        projectService = new ProjectServiceImpl(projectRepository, projectMapper);
        sampleProject = new Project("Plateforme PFE", "Description du projet", ProjectStatus.PROPOSED, LocalDate.now(), LocalDate.now().plusMonths(6));
        sampleProject.setId(1L);
    }

    @Test
    @DisplayName("Création d'un projet avec succès")
    void createProjectSuccess() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Plateforme PFE",
                "Description du projet",
                ProjectStatus.PROPOSED,
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        when(projectRepository.existsByTitleIgnoreCase("Plateforme PFE")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Plateforme PFE");
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("Rejet de la création si un projet avec le même titre existe déjà")
    void createProjectConflictThrowsException() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Plateforme PFE",
                "Description",
                ProjectStatus.PROPOSED,
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        when(projectRepository.existsByTitleIgnoreCase("Plateforme PFE")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("existe déjà");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Rejet si date de fin antérieure à la date de début")
    void createProjectWithInvalidDatesThrowsBusinessRuleException() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Projet Erroné",
                "Description",
                ProjectStatus.PROPOSED,
                LocalDate.of(2027, 6, 1),
                LocalDate.of(2026, 1, 1)
        );

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("antérieure à la date de début");
    }

    @Test
    @DisplayName("Consultation d'un projet par son ID")
    void getProjectByIdFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        ProjectResponse response = projectService.getProjectById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Plateforme PFE");
    }

    @Test
    @DisplayName("Consultation d'un projet inexistant lève ResourceNotFoundException")
    void getProjectByIdNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Suppression d'un projet existant")
    void deleteProjectSuccess() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        projectService.deleteProject(1L);

        verify(projectRepository, times(1)).delete(sampleProject);
    }
}

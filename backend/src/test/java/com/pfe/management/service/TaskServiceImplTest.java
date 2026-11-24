package com.pfe.management.service;

import com.pfe.management.dto.request.TaskCreateRequest;
import com.pfe.management.dto.request.TaskStatusRequest;
import com.pfe.management.dto.request.TaskUpdateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.TaskResponse;
import com.pfe.management.entity.Project;
import com.pfe.management.entity.ProjectMember;
import com.pfe.management.entity.ProjectStatus;
import com.pfe.management.entity.Role;
import com.pfe.management.entity.Task;
import com.pfe.management.entity.TaskPriority;
import com.pfe.management.entity.TaskStatus;
import com.pfe.management.entity.User;
import com.pfe.management.exception.BusinessRuleException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.mapper.TaskMapper;
import com.pfe.management.repository.ProjectMemberRepository;
import com.pfe.management.repository.ProjectRepository;
import com.pfe.management.repository.TaskRepository;
import com.pfe.management.repository.UserRepository;
import com.pfe.management.security.CurrentUserService;
import com.pfe.management.service.impl.TaskServiceImpl;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Spy
    private TaskMapper taskMapper = new TaskMapper();

    @InjectMocks
    private TaskServiceImpl taskService;

    private Project sampleProject;
    private User supervisorUser;
    private User studentUser1;
    private User studentUser2;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Projet PFE", "Description", ProjectStatus.IN_PROGRESS, LocalDate.now(), LocalDate.now().plusMonths(6));
        sampleProject.setId(1L);

        supervisorUser = new User("Sarah", "Supervisor", "supervisor@pfe.com", "pass", Role.SUPERVISOR);
        supervisorUser.setId(2L);

        studentUser1 = new User("Alice", "Student", "alice@pfe.com", "pass", Role.STUDENT);
        studentUser1.setId(3L);

        studentUser2 = new User("Bob", "Student", "bob@pfe.com", "pass", Role.STUDENT);
        studentUser2.setId(4L);

        sampleTask = new Task("Tâche Test", "Description", TaskStatus.TODO, TaskPriority.MEDIUM, LocalDate.now().plusDays(10), sampleProject, studentUser1);
        sampleTask.setId(10L);
    }

    @Test
    @DisplayName("Création d'une tâche avec succès par un administrateur")
    void createTaskSuccessAsAdmin() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Tâche Test", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(10), 1L, 3L
        );

        when(currentUserService.isStudent()).thenReturn(false);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(userRepository.findById(3L)).thenReturn(Optional.of(studentUser1));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 3L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("Tâche Test");
        assertThat(response.getAssignedTo()).isNotNull();
        assertThat(response.getAssignedTo().getId()).isEqualTo(3L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Création d'une tâche avec succès par un SUPERVISOR membre du projet")
    void createTaskSuccessAsSupervisor() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Tâche Test", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(10), 1L, 3L
        );

        when(currentUserService.isStudent()).thenReturn(false);
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(studentUser1));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 3L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Échec de création si l'utilisateur assigné n'est pas membre du projet (BusinessRuleException)")
    void createTaskWithAssigneeNotMemberThrowsBusinessRuleException() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Tâche Test", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(10), 1L, 3L
        );

        when(currentUserService.isStudent()).thenReturn(false);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(userRepository.findById(3L)).thenReturn(Optional.of(studentUser1));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 3L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("L'utilisateur assigné doit être membre du projet");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Échec de création si la date d'échéance est dans le passé (BusinessRuleException)")
    void createTaskWithPastDueDateThrowsBusinessRuleException() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Tâche Test", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().minusDays(1), 1L, 3L
        );

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La date d'échéance ne peut pas être dans le passé");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Échec de création si un STUDENT tente de créer une tâche (AccessDeniedException)")
    void createTaskAsStudentThrowsAccessDeniedException() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Tâche Test", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5), 1L, null
        );

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isStudent()).thenReturn(true);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Les étudiants ne sont pas autorisés à créer des tâches");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Échec de création si SUPERVISOR n'est pas membre du projet (AccessDeniedException)")
    void createTaskAsSupervisorNotInProjectThrowsAccessDeniedException() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Tâche Test", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5), 1L, null
        );

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isStudent()).thenReturn(false);
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Un encadrant ne peut créer de tâches que sur les projets dont il est membre");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Échec de création si le projet n'existe pas (ResourceNotFoundException)")
    void createTaskProjectNotFoundThrowsResourceNotFoundException() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Tâche Test", "Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5), 999L, null
        );

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Projet introuvable");
    }

    @Test
    @DisplayName("Consultation d'une tâche existante par son ID")
    void getTaskByIdSuccess() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(false);

        TaskResponse response = taskService.getTaskById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("Tâche Test");
    }

    @Test
    @DisplayName("Consultation d'une tâche introuvable lève ResourceNotFoundException")
    void getTaskByIdNotFoundThrowsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tâche introuvable");
    }

    @Test
    @DisplayName("Consultation refusée si un STUDENT n'est pas membre du projet (AccessDeniedException)")
    void getTaskByIdAsStudentNotInProjectThrowsAccessDeniedException() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(4L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 4L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.getTaskById(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Accès refusé");
    }

    @Test
    @DisplayName("Lister les tâches pour un administrateur sans restriction sur les projets")
    void getAllTasksAsAdminCallsFindAllWithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(sampleTask), pageable, 1);

        when(currentUserService.isStudent()).thenReturn(false);
        when(taskRepository.findAllWithFilters(null, null, null, pageable)).thenReturn(page);

        PageResponse<TaskResponse> result = taskService.getAllTasks(null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lister les tâches pour un STUDENT : filtrage automatique par ses projets")
    void getAllTasksAsStudentFiltersByMemberProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        ProjectMember pm = new ProjectMember(sampleProject, studentUser1);

        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(3L);
        when(projectMemberRepository.findByUserId(3L)).thenReturn(List.of(pm));

        Page<Task> page = new PageImpl<>(List.of(sampleTask), pageable, 1);
        when(taskRepository.findByProjectIdInWithFilters(List.of(1L), null, null, null, pageable)).thenReturn(page);

        PageResponse<TaskResponse> result = taskService.getAllTasks(null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findByProjectIdInWithFilters(List.of(1L), null, null, null, pageable);
    }

    @Test
    @DisplayName("Lister les tâches pour un STUDENT sans projet retourne une page vide")
    void getAllTasksAsStudentWithNoProjectsReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(3L);
        when(projectMemberRepository.findByUserId(3L)).thenReturn(Collections.emptyList());

        PageResponse<TaskResponse> result = taskService.getAllTasks(null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(taskRepository, never()).findAllWithFilters(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Modification d'une tâche avec succès par un administrateur")
    void updateTaskSuccess() {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Titre Modifié", "Description modifiée", TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH, LocalDate.now().plusDays(15), 3L
        );

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(studentUser1));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 3L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.updateTask(10L, request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Titre Modifié");
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @DisplayName("Échec de modification si un STUDENT tente de modifier une tâche (AccessDeniedException)")
    void updateTaskAsStudentThrowsAccessDeniedException() {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Titre Modifié", "Description", TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH, LocalDate.now().plusDays(15), null
        );

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(true);

        assertThatThrownBy(() -> taskService.updateTask(10L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Les étudiants ne sont pas autorisés à modifier des tâches");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Échec de modification si SUPERVISOR n'est pas membre du projet (AccessDeniedException)")
    void updateTaskAsSupervisorNotInProjectThrowsAccessDeniedException() {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Titre Modifié", "Description", TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH, LocalDate.now().plusDays(15), null
        );

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(false);
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.updateTask(10L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Un encadrant ne peut modifier de tâches que sur les projets dont il est membre");
    }

    @Test
    @DisplayName("STUDENT change avec succès le statut de la tâche qui lui est assignée")
    void updateTaskStatusAsStudentAssignedSuccess() {
        TaskStatusRequest request = new TaskStatusRequest(TaskStatus.DONE);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(3L); // sampleTask est assigné à studentUser1 (id 3L)
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 3L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.updateTaskStatus(10L, request);

        assertThat(response).isNotNull();
        assertThat(sampleTask.getStatus()).isEqualTo(TaskStatus.DONE);
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @DisplayName("Échec si un STUDENT tente de changer le statut d'une tâche assignée à un autre (AccessDeniedException)")
    void updateTaskStatusAsStudentNotAssignedThrowsAccessDeniedException() {
        TaskStatusRequest request = new TaskStatusRequest(TaskStatus.DONE);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask)); // sampleTask est assigné à id 3L
        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(4L); // Connecté en tant qu'étudiant 4L (Bob)
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 4L)).thenReturn(true); // Membre du projet mais pas assigné

        assertThatThrownBy(() -> taskService.updateTaskStatus(10L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Un étudiant ne peut modifier que le statut des tâches qui lui sont assignées");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Échec si un STUDENT hors projet tente de modifier le statut (AccessDeniedException)")
    void updateTaskStatusAsStudentNotInProjectThrowsAccessDeniedException() {
        TaskStatusRequest request = new TaskStatusRequest(TaskStatus.DONE);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(4L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 4L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.updateTaskStatus(10L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("vous n'êtes pas membre de ce projet");
    }

    @Test
    @DisplayName("Échec si un SUPERVISOR hors projet tente de modifier le statut (AccessDeniedException)")
    void updateTaskStatusAsSupervisorNotInProjectThrowsAccessDeniedException() {
        TaskStatusRequest request = new TaskStatusRequest(TaskStatus.DONE);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(false);
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.updateTaskStatus(10L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Un encadrant ne peut modifier le statut de tâches que sur les projets dont il est membre");
    }

    @Test
    @DisplayName("Suppression d'une tâche avec succès par un administrateur")
    void deleteTaskSuccess() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(false);

        taskService.deleteTask(10L);

        verify(taskRepository).delete(sampleTask);
    }

    @Test
    @DisplayName("Échec si un STUDENT tente de supprimer une tâche (AccessDeniedException)")
    void deleteTaskAsStudentThrowsAccessDeniedException() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(true);

        assertThatThrownBy(() -> taskService.deleteTask(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Les étudiants ne sont pas autorisés à supprimer des tâches");

        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("Échec si un SUPERVISOR hors projet tente de supprimer une tâche (AccessDeniedException)")
    void deleteTaskAsSupervisorNotInProjectThrowsAccessDeniedException() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(currentUserService.isStudent()).thenReturn(false);
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Un encadrant ne peut supprimer de tâches que sur les projets dont il est membre");

        verify(taskRepository, never()).delete(any(Task.class));
    }
}

package com.pfe.management.service.impl;

import com.pfe.management.dto.request.TaskCreateRequest;
import com.pfe.management.dto.request.TaskStatusRequest;
import com.pfe.management.dto.request.TaskUpdateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.TaskResponse;
import com.pfe.management.entity.Project;
import com.pfe.management.entity.ProjectMember;
import com.pfe.management.entity.Task;
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
import com.pfe.management.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           UserRepository userRepository,
                           ProjectMemberRepository projectMemberRepository,
                           TaskMapper taskMapper,
                           CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskMapper = taskMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public TaskResponse createTask(TaskCreateRequest request) {
        log.info("Creating new task with title: '{}' for project ID: {}", request.getTitle(), request.getProjectId());

        if (request.getDueDate() != null && request.getDueDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("La date d'échéance ne peut pas être dans le passé");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec l'identifiant: " + request.getProjectId()));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserService.isStudent()) {
            throw new AccessDeniedException("Les étudiants ne sont pas autorisés à créer des tâches");
        }

        if (currentUserService.isSupervisor() && !currentUserService.isAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), currentUserId)) {
                throw new AccessDeniedException("Un encadrant ne peut créer de tâches que sur les projets dont il est membre");
            }
        }

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur assigné introuvable avec l'identifiant: " + request.getAssignedToId()));

            if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), request.getAssignedToId())) {
                throw new BusinessRuleException("L'utilisateur assigné doit être membre du projet");
            }
        }

        Task task = taskMapper.toEntity(request, project, assignedTo);
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());

        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task with ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche introuvable avec l'identifiant: " + id));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserService.isStudent()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), currentUserId)) {
                throw new AccessDeniedException("Accès refusé : vous devez être membre du projet pour consulter cette tâche");
            }
        } else if (currentUserService.isSupervisor() && !currentUserService.isAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), currentUserId)) {
                throw new AccessDeniedException("Accès refusé : un encadrant ne peut consulter que les tâches des projets dont il est membre");
            }
        }

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> getAllTasks(Long projectId, TaskStatus status, Long assigneeId, Pageable pageable) {
        log.debug("Fetching tasks list with filters - projectId: {}, status: {}, assigneeId: {}", projectId, status, assigneeId);

        Long currentUserId = currentUserService.getCurrentUserId();
        Page<Task> page;

        if (currentUserService.isStudent()) {
            List<ProjectMember> memberships = projectMemberRepository.findByUserId(currentUserId);
            List<Long> memberProjectIds = memberships.stream()
                    .map(pm -> pm.getProject().getId())
                    .toList();

            if (memberProjectIds.isEmpty()) {
                return new PageResponse<>(Collections.emptyList(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0, true, true);
            }

            page = taskRepository.findByProjectIdInWithFilters(memberProjectIds, projectId, status, assigneeId, pageable);
        } else {
            page = taskRepository.findAllWithFilters(projectId, status, assigneeId, pageable);
        }

        List<TaskResponse> content = page.getContent()
                .stream()
                .map(taskMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst()
        );
    }

    @Override
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        log.info("Updating task with ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche introuvable avec l'identifiant: " + id));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserService.isStudent()) {
            throw new AccessDeniedException("Les étudiants ne sont pas autorisés à modifier des tâches");
        }

        if (currentUserService.isSupervisor() && !currentUserService.isAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), currentUserId)) {
                throw new AccessDeniedException("Un encadrant ne peut modifier de tâches que sur les projets dont il est membre");
            }
        }

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur assigné introuvable avec l'identifiant: " + request.getAssignedToId()));

            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), request.getAssignedToId())) {
                throw new BusinessRuleException("L'utilisateur assigné doit être membre du projet");
            }
        }

        taskMapper.updateEntityFromDto(request, task, assignedTo);
        Task updatedTask = taskRepository.save(task);
        log.info("Task with ID: {} updated successfully", updatedTask.getId());

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public TaskResponse updateTaskStatus(Long id, TaskStatusRequest request) {
        log.info("Updating status of task ID: {} to {}", id, request.getStatus());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche introuvable avec l'identifiant: " + id));

        Long currentUserId = currentUserService.getCurrentUserId();

        if (currentUserService.isStudent()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), currentUserId)) {
                throw new AccessDeniedException("Accès refusé : vous n'êtes pas membre de ce projet");
            }
            if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUserId)) {
                throw new AccessDeniedException("Un étudiant ne peut modifier que le statut des tâches qui lui sont assignées");
            }
        } else if (currentUserService.isSupervisor() && !currentUserService.isAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), currentUserId)) {
                throw new AccessDeniedException("Un encadrant ne peut modifier le statut de tâches que sur les projets dont il est membre");
            }
        }

        task.setStatus(request.getStatus());
        Task updatedTask = taskRepository.save(task);
        log.info("Task ID: {} status updated to {}", id, updatedTask.getStatus());

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche introuvable avec l'identifiant: " + id));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserService.isStudent()) {
            throw new AccessDeniedException("Les étudiants ne sont pas autorisés à supprimer des tâches");
        }

        if (currentUserService.isSupervisor() && !currentUserService.isAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), currentUserId)) {
                throw new AccessDeniedException("Un encadrant ne peut supprimer de tâches que sur les projets dont il est membre");
            }
        }

        taskRepository.delete(task);
        log.info("Task with ID: {} deleted successfully", id);
    }
}

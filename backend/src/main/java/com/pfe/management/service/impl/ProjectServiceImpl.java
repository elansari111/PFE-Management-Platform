package com.pfe.management.service.impl;

import com.pfe.management.dto.request.ProjectCreateRequest;
import com.pfe.management.dto.request.ProjectUpdateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.ProjectResponse;
import com.pfe.management.entity.Project;
import com.pfe.management.entity.ProjectStatus;
import com.pfe.management.exception.BusinessRuleException;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.mapper.ProjectMapper;
import com.pfe.management.repository.ProjectRepository;
import com.pfe.management.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    public ProjectResponse createProject(ProjectCreateRequest request) {
        log.info("Creating new project with title: '{}'", request.getTitle());

        validateDates(request.getStartDate(), request.getEndDate());

        if (projectRepository.existsByTitleIgnoreCase(request.getTitle().trim())) {
            throw new ResourceAlreadyExistsException("Un projet portant le titre '" + request.getTitle() + "' existe déjà");
        }

        Project project = projectMapper.toEntity(request);
        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getId());

        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        log.debug("Fetching project with ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec l'identifiant: " + id));
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getAllProjects(ProjectStatus status, Pageable pageable) {
        log.debug("Fetching projects list with status: {}, page: {}", status, pageable.getPageNumber());
        Page<Project> page;
        if (status != null) {
            page = projectRepository.findByStatus(status, pageable);
        } else {
            page = projectRepository.findAll(pageable);
        }

        List<ProjectResponse> content = page.getContent()
                .stream()
                .map(projectMapper::toResponse)
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
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
        log.info("Updating project with ID: {}", id);

        validateDates(request.getStartDate(), request.getEndDate());

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec l'identifiant: " + id));

        // Check unique title if modified
        if (!project.getTitle().equalsIgnoreCase(request.getTitle().trim())
                && projectRepository.existsByTitleIgnoreCase(request.getTitle().trim())) {
            throw new ResourceAlreadyExistsException("Un projet portant le titre '" + request.getTitle() + "' existe déjà");
        }

        projectMapper.updateEntityFromDto(request, project);
        Project updatedProject = projectRepository.save(project);
        log.info("Project with ID: {} updated successfully", updatedProject.getId());

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    public void deleteProject(Long id) {
        log.info("Deleting project with ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec l'identifiant: " + id));
        projectRepository.delete(project);
        log.info("Project with ID: {} deleted successfully", id);
    }

    private void validateDates(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessRuleException("La date de fin ne peut pas être antérieure à la date de début");
        }
    }
}

package com.pfe.management.service;

import com.pfe.management.dto.request.ProjectCreateRequest;
import com.pfe.management.dto.request.ProjectUpdateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.ProjectResponse;
import com.pfe.management.entity.ProjectStatus;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse createProject(ProjectCreateRequest request);
    ProjectResponse getProjectById(Long id);
    PageResponse<ProjectResponse> getAllProjects(ProjectStatus status, Pageable pageable);
    ProjectResponse updateProject(Long id, ProjectUpdateRequest request);
    void deleteProject(Long id);
}

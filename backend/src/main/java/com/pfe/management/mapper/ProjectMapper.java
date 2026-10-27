package com.pfe.management.mapper;

import com.pfe.management.dto.request.ProjectCreateRequest;
import com.pfe.management.dto.request.ProjectUpdateRequest;
import com.pfe.management.dto.response.ProjectResponse;
import com.pfe.management.entity.Project;
import com.pfe.management.entity.ProjectStatus;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public Project toEntity(ProjectCreateRequest request) {
        if (request == null) {
            return null;
        }
        Project project = new Project();
        project.setTitle(request.getTitle().trim());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.PROPOSED);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        return project;
    }

    public void updateEntityFromDto(ProjectUpdateRequest request, Project project) {
        if (request == null || project == null) {
            return;
        }
        project.setTitle(request.getTitle().trim());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
    }

    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }
        int membersCount = project.getMembers() != null ? project.getMembers().size() : 0;
        int tasksCount = project.getTasks() != null ? project.getTasks().size() : 0;
        int deliverablesCount = project.getDeliverables() != null ? project.getDeliverables().size() : 0;

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                membersCount,
                tasksCount,
                deliverablesCount,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}

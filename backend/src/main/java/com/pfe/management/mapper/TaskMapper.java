package com.pfe.management.mapper;

import com.pfe.management.dto.request.TaskCreateRequest;
import com.pfe.management.dto.request.TaskUpdateRequest;
import com.pfe.management.dto.response.AssignedUserResponse;
import com.pfe.management.dto.response.TaskResponse;
import com.pfe.management.entity.Project;
import com.pfe.management.entity.Task;
import com.pfe.management.entity.TaskPriority;
import com.pfe.management.entity.TaskStatus;
import com.pfe.management.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public Task toEntity(TaskCreateRequest request, Project project, User assignedTo) {
        if (request == null) {
            return null;
        }

        Task task = new Task();
        task.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setAssignedTo(assignedTo);
        return task;
    }

    public void updateEntityFromDto(TaskUpdateRequest request, Task task, User assignedTo) {
        if (request == null || task == null) {
            return;
        }

        task.setTitle(request.getTitle() != null ? request.getTitle().trim() : task.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setDueDate(request.getDueDate());
        task.setAssignedTo(assignedTo);
    }

    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }

        Long projectId = task.getProject() != null ? task.getProject().getId() : null;
        String projectTitle = task.getProject() != null ? task.getProject().getTitle() : null;

        AssignedUserResponse assignedUser = null;
        if (task.getAssignedTo() != null) {
            User user = task.getAssignedTo();
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            String fullName = (firstName + " " + lastName).trim();
            assignedUser = new AssignedUserResponse(user.getId(), fullName);
        }

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                projectId,
                projectTitle,
                assignedUser,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}

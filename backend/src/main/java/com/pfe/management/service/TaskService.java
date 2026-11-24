package com.pfe.management.service;

import com.pfe.management.dto.request.TaskCreateRequest;
import com.pfe.management.dto.request.TaskStatusRequest;
import com.pfe.management.dto.request.TaskUpdateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.TaskResponse;
import com.pfe.management.entity.TaskStatus;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponse createTask(TaskCreateRequest request);

    TaskResponse getTaskById(Long id);

    PageResponse<TaskResponse> getAllTasks(Long projectId, TaskStatus status, Long assigneeId, Pageable pageable);

    TaskResponse updateTask(Long id, TaskUpdateRequest request);

    TaskResponse updateTaskStatus(Long id, TaskStatusRequest request);

    void deleteTask(Long id);
}

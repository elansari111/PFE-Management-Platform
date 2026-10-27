package com.pfe.management.repository;

import com.pfe.management.entity.Task;
import com.pfe.management.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
    List<Task> findByAssignedToId(Long userId);
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
}

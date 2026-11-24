package com.pfe.management.repository;

import com.pfe.management.entity.Task;
import com.pfe.management.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    List<Task> findByAssignedToId(Long userId);

    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE " +
           "(:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:assigneeId IS NULL OR t.assignedTo.id = :assigneeId)")
    Page<Task> findAllWithFilters(@Param("projectId") Long projectId,
                                  @Param("status") TaskStatus status,
                                  @Param("assigneeId") Long assigneeId,
                                  Pageable pageable);

    @Query("SELECT t FROM Task t WHERE " +
           "t.project.id IN :projectIds AND " +
           "(:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:assigneeId IS NULL OR t.assignedTo.id = :assigneeId)")
    Page<Task> findByProjectIdInWithFilters(@Param("projectIds") Collection<Long> projectIds,
                                           @Param("projectId") Long projectId,
                                           @Param("status") TaskStatus status,
                                           @Param("assigneeId") Long assigneeId,
                                           Pageable pageable);
}

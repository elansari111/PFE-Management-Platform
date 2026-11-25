package com.pfe.management.repository;

import com.pfe.management.entity.Deliverable;
import com.pfe.management.entity.DeliverableStatus;
import com.pfe.management.entity.DeliverableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliverableRepository extends JpaRepository<Deliverable, Long> {

    List<Deliverable> findByProjectId(Long projectId);

    Optional<Deliverable> findTopByProjectIdAndTypeOrderByVersionDesc(Long projectId, DeliverableType type);

    @Query("SELECT d FROM Deliverable d WHERE " +
           "(:projectId IS NULL OR d.project.id = :projectId) AND " +
           "(:status IS NULL OR d.status = :status)")
    Page<Deliverable> findAllWithFilters(@Param("projectId") Long projectId,
                                         @Param("status") DeliverableStatus status,
                                         Pageable pageable);

    @Query("SELECT d FROM Deliverable d WHERE " +
           "d.project.id IN :projectIds AND " +
           "(:projectId IS NULL OR d.project.id = :projectId) AND " +
           "(:status IS NULL OR d.status = :status)")
    Page<Deliverable> findByProjectIdInWithFilters(@Param("projectIds") Collection<Long> projectIds,
                                                   @Param("projectId") Long projectId,
                                                   @Param("status") DeliverableStatus status,
                                                   Pageable pageable);
}

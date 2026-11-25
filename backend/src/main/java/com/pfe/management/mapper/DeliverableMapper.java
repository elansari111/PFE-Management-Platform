package com.pfe.management.mapper;

import com.pfe.management.dto.response.DeliverableResponse;
import com.pfe.management.dto.response.DeliverableUserResponse;
import com.pfe.management.entity.Deliverable;
import com.pfe.management.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DeliverableMapper {

    public DeliverableResponse toResponse(Deliverable deliverable) {
        if (deliverable == null) {
            return null;
        }

        Long projectId = deliverable.getProject() != null ? deliverable.getProject().getId() : null;
        String projectTitle = deliverable.getProject() != null ? deliverable.getProject().getTitle() : null;

        DeliverableUserResponse createdByUser = mapUser(deliverable.getCreatedBy());
        DeliverableUserResponse reviewedByUser = mapUser(deliverable.getReviewedBy());

        return new DeliverableResponse(
                deliverable.getId(),
                deliverable.getTitle(),
                deliverable.getDescription(),
                deliverable.getType(),
                deliverable.getVersion(),
                deliverable.getStatus(),
                deliverable.getOriginalFilename(),
                deliverable.getContentType(),
                deliverable.getFileSize(),
                deliverable.getComment(),
                deliverable.getSubmissionDate(),
                projectId,
                projectTitle,
                createdByUser,
                reviewedByUser,
                deliverable.getReviewedAt(),
                deliverable.getCreatedAt(),
                deliverable.getUpdatedAt()
        );
    }

    private DeliverableUserResponse mapUser(User user) {
        if (user == null) {
            return null;
        }
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        return new DeliverableUserResponse(user.getId(), fullName);
    }
}

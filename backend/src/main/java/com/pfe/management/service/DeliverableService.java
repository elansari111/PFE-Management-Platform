package com.pfe.management.service;

import com.pfe.management.dto.request.DeliverableReviewRequest;
import com.pfe.management.dto.response.DeliverableFileResource;
import com.pfe.management.dto.response.DeliverableResponse;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.entity.DeliverableStatus;
import com.pfe.management.entity.DeliverableType;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface DeliverableService {

    DeliverableResponse createDeliverable(Long projectId, DeliverableType type, MultipartFile file);

    DeliverableResponse getDeliverableById(Long id);

    PageResponse<DeliverableResponse> getAllDeliverables(Long projectId, DeliverableStatus status, Pageable pageable);

    DeliverableFileResource downloadDeliverable(Long id);

    DeliverableResponse reviewDeliverable(Long id, DeliverableReviewRequest request);

    void deleteDeliverable(Long id);
}

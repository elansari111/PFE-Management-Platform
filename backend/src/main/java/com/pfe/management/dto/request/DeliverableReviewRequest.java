package com.pfe.management.dto.request;

import com.pfe.management.entity.DeliverableStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de validation ou de refus d'un livrable")
public class DeliverableReviewRequest {

    @NotNull(message = "Le statut est obligatoire")
    @Schema(description = "Nouveau statut du livrable (APPROVED ou REJECTED)", example = "APPROVED", requiredMode = Schema.RequiredMode.REQUIRED)
    private DeliverableStatus status;

    @Schema(description = "Commentaire ou motif de révision (obligatoire si rejeté)", example = "Document complet et bien structuré.")
    private String comment;

    public DeliverableReviewRequest() {
    }

    public DeliverableReviewRequest(DeliverableStatus status, String comment) {
        this.status = status;
        this.comment = comment;
    }

    public DeliverableStatus getStatus() {
        return status;
    }

    public void setStatus(DeliverableStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

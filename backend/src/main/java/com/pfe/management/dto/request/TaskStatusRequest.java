package com.pfe.management.dto.request;

import com.pfe.management.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Données pour le changement de statut d'une tâche")
public class TaskStatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    @Schema(description = "Nouveau statut de la tâche", example = "DONE", requiredMode = Schema.RequiredMode.REQUIRED)
    private TaskStatus status;

    public TaskStatusRequest() {
    }

    public TaskStatusRequest(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}

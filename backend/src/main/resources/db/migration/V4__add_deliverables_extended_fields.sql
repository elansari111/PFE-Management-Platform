-- V4__add_deliverables_extended_fields.sql
-- Extension de la table deliverables pour le versionnement, l'audit et la validation des fichiers

ALTER TABLE deliverables
    ADD COLUMN type VARCHAR(50) NOT NULL DEFAULT 'RAPPORT_FINAL' AFTER description,
    ADD COLUMN version INT NOT NULL DEFAULT 1 AFTER type,
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED' AFTER version,
    ADD COLUMN original_filename VARCHAR(255) NOT NULL DEFAULT '' AFTER status,
    ADD COLUMN stored_filename VARCHAR(255) NOT NULL DEFAULT '' AFTER original_filename,
    ADD COLUMN content_type VARCHAR(100) NULL AFTER stored_filename,
    ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0 AFTER content_type,
    ADD COLUMN comment TEXT NULL AFTER file_size,
    ADD COLUMN created_by BIGINT NULL AFTER comment,
    ADD COLUMN reviewed_by BIGINT NULL AFTER created_by,
    ADD COLUMN reviewed_at TIMESTAMP NULL AFTER reviewed_by;

ALTER TABLE deliverables
    ADD CONSTRAINT fk_deliv_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_deliv_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD INDEX idx_deliv_project_type (project_id, type),
    ADD INDEX idx_deliv_status (status),
    ADD INDEX idx_deliv_created_by (created_by),
    ADD INDEX idx_deliv_reviewed_by (reviewed_by);

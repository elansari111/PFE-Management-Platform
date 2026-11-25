package com.pfe.management.service.impl;

import com.pfe.management.dto.request.DeliverableReviewRequest;
import com.pfe.management.dto.response.DeliverableFileResource;
import com.pfe.management.dto.response.DeliverableResponse;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.entity.*;
import com.pfe.management.exception.BusinessRuleException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.mapper.DeliverableMapper;
import com.pfe.management.repository.DeliverableRepository;
import com.pfe.management.repository.ProjectMemberRepository;
import com.pfe.management.repository.ProjectRepository;
import com.pfe.management.repository.UserRepository;
import com.pfe.management.security.CurrentUserService;
import com.pfe.management.service.DeliverableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class DeliverableServiceImpl implements DeliverableService {

    private static final Logger log = LoggerFactory.getLogger(DeliverableServiceImpl.class);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "pptx", "zip", "md", "txt");

    private final DeliverableRepository deliverableRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final DeliverableMapper deliverableMapper;
    private final CurrentUserService currentUserService;
    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public DeliverableServiceImpl(DeliverableRepository deliverableRepository,
                                  ProjectRepository projectRepository,
                                  UserRepository userRepository,
                                  ProjectMemberRepository projectMemberRepository,
                                  DeliverableMapper deliverableMapper,
                                  CurrentUserService currentUserService) {
        this.deliverableRepository = deliverableRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.deliverableMapper = deliverableMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public DeliverableResponse createDeliverable(Long projectId, DeliverableType type, MultipartFile file) {
        log.info("Uploading deliverable for project ID: {}, type: {}", projectId, type);

        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Le fichier fourni est vide ou manquant");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new MaxUploadSizeExceededException(MAX_FILE_SIZE_BYTES);
        }

        if (projectId == null) {
            throw new BusinessRuleException("L'identifiant du projet est obligatoire");
        }

        if (type == null) {
            throw new BusinessRuleException("Le type de livrable est obligatoire");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessRuleException("Nom de fichier original invalide");
        }

        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessRuleException("Extension de fichier non autorisée (" + extension + "). Formats acceptés : pdf, docx, pptx, zip, md, txt");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec l'identifiant: " + projectId));

        Long currentUserId = currentUserService.getCurrentUserId();

        // Règle : Un STUDENT membre du projet dépose un livrable (ou ADMIN)
        if (!currentUserService.isAdmin()) {
            if (!currentUserService.isStudent()) {
                throw new AccessDeniedException("Seuls les étudiants sont autorisés à déposer un livrable");
            }
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)) {
                throw new AccessDeniedException("Vous devez être membre de ce projet pour déposer un livrable");
            }
        }

        // Stockage sur disque sécurisé
        String storedFilename = UUID.randomUUID() + "." + extension.toLowerCase();
        Path storageDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageDirectory);
            Path targetPath = storageDirectory.resolve(storedFilename).normalize();

            // Sécurité anti Path-Traversal
            if (!targetPath.startsWith(storageDirectory)) {
                throw new BusinessRuleException("Nom de fichier invalide");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store file on disk", e);
            throw new BusinessRuleException("Erreur lors de l'enregistrement physique du fichier");
        }

        // Versionnement N+1 pour le même type et même projet
        int nextVersion = deliverableRepository.findTopByProjectIdAndTypeOrderByVersionDesc(projectId, type)
                .map(d -> d.getVersion() + 1)
                .orElse(1);

        User createdBy = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        String title = type.name() + " - Version " + nextVersion;
        String storedPath = storageDirectory.resolve(storedFilename).toString();

        Deliverable deliverable = new Deliverable(
                title,
                null,
                type,
                nextVersion,
                DeliverableStatus.SUBMITTED,
                originalFilename,
                storedFilename,
                storedPath,
                file.getContentType(),
                file.getSize(),
                project,
                createdBy
        );

        Deliverable saved = deliverableRepository.save(deliverable);
        log.info("Deliverable created successfully with ID: {}, version: {}", saved.getId(), saved.getVersion());

        return deliverableMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliverableResponse getDeliverableById(Long id) {
        log.debug("Fetching deliverable with ID: {}", id);

        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livrable introuvable avec l'identifiant: " + id));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (!currentUserService.isAdmin() && !projectMemberRepository.existsByProjectIdAndUserId(deliverable.getProject().getId(), currentUserId)) {
            throw new AccessDeniedException("Accès refusé : vous devez être membre du projet pour consulter ce livrable");
        }

        return deliverableMapper.toResponse(deliverable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeliverableResponse> getAllDeliverables(Long projectId, DeliverableStatus status, Pageable pageable) {
        log.debug("Fetching deliverables with filters - projectId: {}, status: {}", projectId, status);

        Long currentUserId = currentUserService.getCurrentUserId();
        Page<Deliverable> page;

        if (currentUserService.isAdmin()) {
            page = deliverableRepository.findAllWithFilters(projectId, status, pageable);
        } else {
            List<ProjectMember> memberships = projectMemberRepository.findByUserId(currentUserId);
            List<Long> memberProjectIds = memberships.stream()
                    .map(pm -> pm.getProject().getId())
                    .toList();

            if (memberProjectIds.isEmpty()) {
                return new PageResponse<>(Collections.emptyList(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0, true, true);
            }

            if (projectId != null && !memberProjectIds.contains(projectId)) {
                throw new AccessDeniedException("Accès refusé : vous n'êtes pas membre du projet spécifié");
            }

            page = deliverableRepository.findByProjectIdInWithFilters(memberProjectIds, projectId, status, pageable);
        }

        List<DeliverableResponse> content = page.getContent()
                .stream()
                .map(deliverableMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DeliverableFileResource downloadDeliverable(Long id) {
        log.info("Downloading deliverable with ID: {}", id);

        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livrable introuvable avec l'identifiant: " + id));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (!currentUserService.isAdmin() && !projectMemberRepository.existsByProjectIdAndUserId(deliverable.getProject().getId(), currentUserId)) {
            throw new AccessDeniedException("Accès refusé : vous devez être membre du projet pour télécharger ce livrable");
        }

        Path filePath = Paths.get(deliverable.getFilePath()).normalize();
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Fichier physique introuvable sur le serveur");
            }

            String contentType = deliverable.getContentType() != null ? deliverable.getContentType() : "application/octet-stream";
            return new DeliverableFileResource(
                    resource,
                    deliverable.getOriginalFilename(),
                    contentType,
                    deliverable.getFileSize()
            );
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Chemin de fichier invalide");
        }
    }

    @Override
    public DeliverableResponse reviewDeliverable(Long id, DeliverableReviewRequest request) {
        log.info("Reviewing deliverable with ID: {}, new status: {}", id, request.getStatus());

        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livrable introuvable avec l'identifiant: " + id));

        Long currentUserId = currentUserService.getCurrentUserId();

        if (currentUserService.isStudent()) {
            throw new AccessDeniedException("Les étudiants ne sont pas autorisés à valider ou rejeter un livrable");
        }

        if (currentUserService.isSupervisor() && !currentUserService.isAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(deliverable.getProject().getId(), currentUserId)) {
                throw new AccessDeniedException("Un encadrant ne peut réviser que les livrables des projets dont il est membre");
            }
        }

        if (request.getStatus() != DeliverableStatus.APPROVED && request.getStatus() != DeliverableStatus.REJECTED) {
            throw new BusinessRuleException("Le statut de révision doit être APPROVED ou REJECTED");
        }

        if (request.getStatus() == DeliverableStatus.REJECTED) {
            if (request.getComment() == null || request.getComment().trim().isEmpty()) {
                throw new BusinessRuleException("Un commentaire est obligatoire en cas de refus du livrable");
            }
        }

        User reviewer = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        deliverable.setStatus(request.getStatus());
        deliverable.setComment(request.getComment() != null ? request.getComment().trim() : null);
        deliverable.setReviewedBy(reviewer);
        deliverable.setReviewedAt(LocalDateTime.now());

        Deliverable saved = deliverableRepository.save(deliverable);
        log.info("Deliverable ID: {} successfully reviewed to {}", saved.getId(), saved.getStatus());

        return deliverableMapper.toResponse(saved);
    }

    @Override
    public void deleteDeliverable(Long id) {
        log.info("Deleting deliverable with ID: {}", id);

        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livrable introuvable avec l'identifiant: " + id));

        Long currentUserId = currentUserService.getCurrentUserId();

        if (!currentUserService.isAdmin()) {
            boolean isAuthor = deliverable.getCreatedBy() != null && deliverable.getCreatedBy().getId().equals(currentUserId);
            if (!isAuthor) {
                throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer ce livrable");
            }
            if (deliverable.getStatus() != DeliverableStatus.SUBMITTED) {
                throw new BusinessRuleException("Impossible de supprimer un livrable déjà validé ou refusé");
            }
        }

        // Suppression physique du fichier
        try {
            Path filePath = Paths.get(deliverable.getFilePath()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier physique: {}", deliverable.getFilePath());
        }

        deliverableRepository.delete(deliverable);
        log.info("Deliverable ID: {} deleted successfully", id);
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1);
    }
}

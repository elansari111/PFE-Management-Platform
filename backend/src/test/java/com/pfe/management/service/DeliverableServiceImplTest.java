package com.pfe.management.service;

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
import com.pfe.management.service.impl.DeliverableServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliverableServiceImplTest {

    @Mock
    private DeliverableRepository deliverableRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Spy
    private DeliverableMapper deliverableMapper = new DeliverableMapper();

    @InjectMocks
    private DeliverableServiceImpl deliverableService;

    @TempDir
    Path tempUploadDir;

    private Project sampleProject;
    private User studentUser;
    private User supervisorUser;
    private Deliverable sampleDeliverable;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(deliverableService, "uploadDir", tempUploadDir.toString());

        sampleProject = new Project("Plateforme PFE", "Description", ProjectStatus.IN_PROGRESS, LocalDate.now(), LocalDate.now().plusMonths(6));
        sampleProject.setId(1L);

        studentUser = new User("Alice", "Student", "alice@pfe.com", "pass", Role.STUDENT);
        studentUser.setId(3L);

        supervisorUser = new User("Dr.", "Supervisor", "supervisor@pfe.com", "pass", Role.SUPERVISOR);
        supervisorUser.setId(2L);

        sampleDeliverable = new Deliverable(
                "RAPPORT_INTERMEDIAIRE - Version 1",
                null,
                DeliverableType.RAPPORT_INTERMEDIAIRE,
                1,
                DeliverableStatus.SUBMITTED,
                "rapport.pdf",
                "sample-uuid.pdf",
                tempUploadDir.resolve("sample-uuid.pdf").toString(),
                "application/pdf",
                1024L,
                sampleProject,
                studentUser
        );
        sampleDeliverable.setId(10L);
    }

    @Test
    @DisplayName("Dépôt réussi d'un livrable par un étudiant membre du projet (Version 1)")
    void createDeliverableSuccess() {
        MockMultipartFile file = new MockMultipartFile("file", "rapport.pdf", "application/pdf", "Contenu PDF valide".getBytes());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(3L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 3L)).thenReturn(true);
        when(deliverableRepository.findTopByProjectIdAndTypeOrderByVersionDesc(1L, DeliverableType.RAPPORT_INTERMEDIAIRE))
                .thenReturn(Optional.empty());
        when(userRepository.findById(3L)).thenReturn(Optional.of(studentUser));
        when(deliverableRepository.save(any(Deliverable.class))).thenReturn(sampleDeliverable);

        DeliverableResponse response = deliverableService.createDeliverable(1L, DeliverableType.RAPPORT_INTERMEDIAIRE, file);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getVersion()).isEqualTo(1);
        assertThat(response.getOriginalFilename()).isEqualTo("rapport.pdf");
        verify(deliverableRepository).save(any(Deliverable.class));
    }

    @Test
    @DisplayName("Incrémentation de la version (N+1) si un livrable du même type existe déjà")
    void createDeliverableVersionIncrement() {
        MockMultipartFile file = new MockMultipartFile("file", "rapport_v2.pdf", "application/pdf", "Contenu version 2".getBytes());

        Deliverable existingV2 = new Deliverable(
                "RAPPORT_INTERMEDIAIRE - Version 2", null, DeliverableType.RAPPORT_INTERMEDIAIRE,
                2, DeliverableStatus.APPROVED, "rapport_v2.pdf", "uuid2.pdf",
                tempUploadDir.resolve("uuid2.pdf").toString(), "application/pdf", 2048L,
                sampleProject, studentUser
        );

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(3L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 3L)).thenReturn(true);
        when(deliverableRepository.findTopByProjectIdAndTypeOrderByVersionDesc(1L, DeliverableType.RAPPORT_INTERMEDIAIRE))
                .thenReturn(Optional.of(existingV2));
        when(userRepository.findById(3L)).thenReturn(Optional.of(studentUser));
        when(deliverableRepository.save(any(Deliverable.class))).thenAnswer(invocation -> {
            Deliverable d = invocation.getArgument(0);
            d.setId(11L);
            return d;
        });

        DeliverableResponse response = deliverableService.createDeliverable(1L, DeliverableType.RAPPORT_INTERMEDIAIRE, file);

        assertThat(response).isNotNull();
        assertThat(response.getVersion()).isEqualTo(3);
    }

    @Test
    @DisplayName("Échec si le fichier envoyé est vide (BusinessRuleException)")
    void createDeliverableEmptyFileThrowsBusinessRuleException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> deliverableService.createDeliverable(1L, DeliverableType.RAPPORT_FINAL, emptyFile))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("vide");

        verify(deliverableRepository, never()).save(any());
    }

    @Test
    @DisplayName("Échec si l'extension n'est pas autorisée (BusinessRuleException)")
    void createDeliverableInvalidExtensionThrowsBusinessRuleException() {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "script.exe", "application/octet-stream", "evil content".getBytes());

        assertThatThrownBy(() -> deliverableService.createDeliverable(1L, DeliverableType.RAPPORT_FINAL, invalidFile))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Extension de fichier non autorisée");

        verify(deliverableRepository, never()).save(any());
    }

    @Test
    @DisplayName("Échec si un étudiant dépose sur un projet dont il n'est pas membre (AccessDeniedException)")
    void createDeliverableStudentNotMemberThrowsAccessDeniedException() {
        MockMultipartFile file = new MockMultipartFile("file", "rapport.pdf", "application/pdf", "PDF content".getBytes());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(4L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 4L)).thenReturn(false);

        assertThatThrownBy(() -> deliverableService.createDeliverable(1L, DeliverableType.RAPPORT_FINAL, file))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Vous devez être membre de ce projet");

        verify(deliverableRepository, never()).save(any());
    }

    @Test
    @DisplayName("Échec si le projet n'existe pas (ResourceNotFoundException)")
    void createDeliverableProjectNotFoundThrowsResourceNotFoundException() {
        MockMultipartFile file = new MockMultipartFile("file", "rapport.pdf", "application/pdf", "PDF content".getBytes());

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliverableService.createDeliverable(999L, DeliverableType.RAPPORT_FINAL, file))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Projet introuvable");
    }

    @Test
    @DisplayName("Consultation d'un livrable par son identifiant avec succès")
    void getDeliverableByIdSuccess() {
        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isAdmin()).thenReturn(true);

        DeliverableResponse response = deliverableService.getDeliverableById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("RAPPORT_INTERMEDIAIRE - Version 1");
    }

    @Test
    @DisplayName("Consultation refusée si l'utilisateur n'est pas membre du projet (AccessDeniedException)")
    void getDeliverableByIdNotMemberThrowsAccessDeniedException() {
        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(4L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 4L)).thenReturn(false);

        assertThatThrownBy(() -> deliverableService.getDeliverableById(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Accès refusé");
    }

    @Test
    @DisplayName("Lister les livrables par un administrateur")
    void getAllDeliverablesAdminCallsFindAllWithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Deliverable> page = new PageImpl<>(List.of(sampleDeliverable), pageable, 1);

        when(currentUserService.isAdmin()).thenReturn(true);
        when(deliverableRepository.findAllWithFilters(null, null, pageable)).thenReturn(page);

        PageResponse<DeliverableResponse> result = deliverableService.getAllDeliverables(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Lister les livrables pour un étudiant : filtré par ses projets")
    void getAllDeliverablesStudentFiltersByMemberProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        ProjectMember pm = new ProjectMember(sampleProject, studentUser);

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(3L);
        when(projectMemberRepository.findByUserId(3L)).thenReturn(List.of(pm));

        Page<Deliverable> page = new PageImpl<>(List.of(sampleDeliverable), pageable, 1);
        when(deliverableRepository.findByProjectIdInWithFilters(List.of(1L), null, null, pageable)).thenReturn(page);

        PageResponse<DeliverableResponse> result = deliverableService.getAllDeliverables(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Téléchargement réussi d'un fichier existant sur disque")
    void downloadDeliverableSuccess() throws IOException {
        Path physicalFile = tempUploadDir.resolve("sample-uuid.pdf");
        Files.writeString(physicalFile, "Fichier physique de test");

        sampleDeliverable.setFilePath(physicalFile.toString());
        sampleDeliverable.setOriginalFilename("mon_rapport.pdf");

        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isAdmin()).thenReturn(true);

        DeliverableFileResource resource = deliverableService.downloadDeliverable(10L);

        assertThat(resource).isNotNull();
        assertThat(resource.getOriginalFilename()).isEqualTo("mon_rapport.pdf");
        assertThat(resource.getResource().exists()).isTrue();
    }

    @Test
    @DisplayName("Échec de téléchargement si non membre du projet (AccessDeniedException)")
    void downloadDeliverableNotMemberThrowsAccessDeniedException() {
        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(4L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 4L)).thenReturn(false);

        assertThatThrownBy(() -> deliverableService.downloadDeliverable(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Accès refusé");
    }

    @Test
    @DisplayName("Validation d'un livrable (APPROVED) par un encadrant membre du projet")
    void reviewDeliverableApprovedSuccess() {
        DeliverableReviewRequest request = new DeliverableReviewRequest(DeliverableStatus.APPROVED, "Excellent travail");

        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isStudent()).thenReturn(false);
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(supervisorUser));
        when(deliverableRepository.save(any(Deliverable.class))).thenReturn(sampleDeliverable);

        DeliverableResponse response = deliverableService.reviewDeliverable(10L, request);

        assertThat(response).isNotNull();
        assertThat(sampleDeliverable.getStatus()).isEqualTo(DeliverableStatus.APPROVED);
        assertThat(sampleDeliverable.getComment()).isEqualTo("Excellent travail");
        verify(deliverableRepository).save(sampleDeliverable);
    }

    @Test
    @DisplayName("Refus d'un livrable (REJECTED) avec commentaire obligatoire fourni")
    void reviewDeliverableRejectedWithCommentSuccess() {
        DeliverableReviewRequest request = new DeliverableReviewRequest(DeliverableStatus.REJECTED, "Manque la partie architecture");

        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isStudent()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(deliverableRepository.save(any(Deliverable.class))).thenReturn(sampleDeliverable);

        DeliverableResponse response = deliverableService.reviewDeliverable(10L, request);

        assertThat(response).isNotNull();
        assertThat(sampleDeliverable.getStatus()).isEqualTo(DeliverableStatus.REJECTED);
        assertThat(sampleDeliverable.getComment()).isEqualTo("Manque la partie architecture");
    }

    @Test
    @DisplayName("Échec de refus (REJECTED) sans commentaire (BusinessRuleException)")
    void reviewDeliverableRejectedWithoutCommentThrowsBusinessRuleException() {
        DeliverableReviewRequest request = new DeliverableReviewRequest(DeliverableStatus.REJECTED, "   ");

        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isStudent()).thenReturn(false);

        assertThatThrownBy(() -> deliverableService.reviewDeliverable(10L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Un commentaire est obligatoire en cas de refus");

        verify(deliverableRepository, never()).save(any());
    }

    @Test
    @DisplayName("Échec si un STUDENT tente de valider un livrable (AccessDeniedException)")
    void reviewDeliverableStudentThrowsAccessDeniedException() {
        DeliverableReviewRequest request = new DeliverableReviewRequest(DeliverableStatus.APPROVED, "auto validation");

        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isStudent()).thenReturn(true);

        assertThatThrownBy(() -> deliverableService.reviewDeliverable(10L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Les étudiants ne sont pas autorisés à valider");

        verify(deliverableRepository, never()).save(any());
    }

    @Test
    @DisplayName("Suppression d'un livrable par le déposant tant qu'il est SUBMITTED")
    void deleteDeliverableSuccessByAuthorWhenSubmitted() {
        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(3L); // Id de Alice (l'auteur)

        deliverableService.deleteDeliverable(10L);

        verify(deliverableRepository).delete(sampleDeliverable);
    }

    @Test
    @DisplayName("Échec de suppression si le livrable est déjà APPROVED (BusinessRuleException)")
    void deleteDeliverableByAuthorWhenAlreadyApprovedThrowsBusinessRuleException() {
        sampleDeliverable.setStatus(DeliverableStatus.APPROVED);

        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(3L);

        assertThatThrownBy(() -> deliverableService.deleteDeliverable(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Impossible de supprimer un livrable déjà validé ou refusé");

        verify(deliverableRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Échec de suppression si un utilisateur non-auteur tente de supprimer (AccessDeniedException)")
    void deleteDeliverableByOtherStudentThrowsAccessDeniedException() {
        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(4L); // Autre étudiant

        assertThatThrownBy(() -> deliverableService.deleteDeliverable(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Vous n'êtes pas autorisé à supprimer ce livrable");

        verify(deliverableRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Échec si le fichier dépasse 20 Mo (MaxUploadSizeExceededException)")
    void createDeliverableTooLargeThrowsMaxUploadSizeExceeded() {
        MultipartFile big = mock(MultipartFile.class);
        when(big.isEmpty()).thenReturn(false);
        when(big.getSize()).thenReturn(20L * 1024 * 1024 + 1);

        assertThatThrownBy(() -> deliverableService.createDeliverable(1L, DeliverableType.RAPPORT_FINAL, big))
                .isInstanceOf(MaxUploadSizeExceededException.class);

        verify(deliverableRepository, never()).save(any());
    }

    @Test
    @DisplayName("Échec si un SUPERVISOR tente de déposer un livrable (AccessDeniedException)")
    void createDeliverableSupervisorThrowsAccessDeniedException() {
        MockMultipartFile file = new MockMultipartFile("file", "rapport.pdf", "application/pdf", "x".getBytes());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.getCurrentUserId()).thenReturn(2L);

        assertThatThrownBy(() -> deliverableService.createDeliverable(1L, DeliverableType.RAPPORT_FINAL, file))
                .isInstanceOf(AccessDeniedException.class);

        verify(deliverableRepository, never()).save(any());
    }

    @Test
    @DisplayName("Anti path-traversal : le nom d'origine n'est jamais utilisé comme chemin")
    void createDeliverableNeverUsesOriginalFilenameAsPath() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "../../evil.pdf", "application/pdf", "contenu".getBytes());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.isStudent()).thenReturn(true);
        when(currentUserService.getCurrentUserId()).thenReturn(3L);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 3L)).thenReturn(true);
        when(deliverableRepository.findTopByProjectIdAndTypeOrderByVersionDesc(1L, DeliverableType.RAPPORT_FINAL))
                .thenReturn(Optional.empty());
        when(userRepository.findById(3L)).thenReturn(Optional.of(studentUser));
        when(deliverableRepository.save(any(Deliverable.class))).thenAnswer(inv -> inv.getArgument(0));

        deliverableService.createDeliverable(1L, DeliverableType.RAPPORT_FINAL, file);

        org.mockito.ArgumentCaptor<Deliverable> captor = org.mockito.ArgumentCaptor.forClass(Deliverable.class);
        verify(deliverableRepository).save(captor.capture());
        assertThat(captor.getValue().getStoredFilename()).doesNotContain("evil").doesNotContain("..").endsWith(".pdf");
        assertThat(Path.of(captor.getValue().getFilePath()).getParent()).isEqualTo(tempUploadDir.toAbsolutePath().normalize());

        try (var files = Files.list(tempUploadDir)) {
            assertThat(files.toList()).hasSize(1);
        }
    }

    @Test
    @DisplayName("Téléchargement : livrable inexistant (ResourceNotFoundException)")
    void downloadDeliverableNotFoundThrowsResourceNotFoundException() {
        when(deliverableRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliverableService.downloadDeliverable(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Téléchargement : fichier physique absent du disque (ResourceNotFoundException)")
    void downloadDeliverableMissingPhysicalFileThrowsResourceNotFoundException() {
        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.isAdmin()).thenReturn(true);

        assertThatThrownBy(() -> deliverableService.downloadDeliverable(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("physique");
    }

    @Test
    @DisplayName("Révision : livrable inexistant (ResourceNotFoundException)")
    void reviewDeliverableNotFoundThrowsResourceNotFoundException() {
        when(deliverableRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliverableService.reviewDeliverable(99L,
                new DeliverableReviewRequest(DeliverableStatus.APPROVED, "ok")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Révision : un SUPERVISOR non membre du projet est refusé (AccessDeniedException)")
    void reviewDeliverableSupervisorNotMemberThrowsAccessDeniedException() {
        when(deliverableRepository.findById(10L)).thenReturn(Optional.of(sampleDeliverable));
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(currentUserService.isSupervisor()).thenReturn(true);
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> deliverableService.reviewDeliverable(10L,
                new DeliverableReviewRequest(DeliverableStatus.APPROVED, "ok")))
                .isInstanceOf(AccessDeniedException.class);

        verify(deliverableRepository, never()).save(any());
    }

    @Test
    @DisplayName("Suppression : livrable inexistant (ResourceNotFoundException)")
    void deleteDeliverableNotFoundThrowsResourceNotFoundException() {
        when(deliverableRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliverableService.deleteDeliverable(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(deliverableRepository, never()).delete(any());
    }
}
package com.pfe.management.service.impl;

import com.pfe.management.dto.request.ProjectMemberRequest;
import com.pfe.management.dto.response.ProjectMemberResponse;
import com.pfe.management.entity.Project;
import com.pfe.management.entity.ProjectMember;
import com.pfe.management.entity.User;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.repository.ProjectMemberRepository;
import com.pfe.management.repository.ProjectRepository;
import com.pfe.management.repository.UserRepository;
import com.pfe.management.security.CurrentUserService;
import com.pfe.management.service.ProjectMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private static final Logger log = LoggerFactory.getLogger(ProjectMemberServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public ProjectMemberServiceImpl(ProjectRepository projectRepository,
                                    UserRepository userRepository,
                                    ProjectMemberRepository projectMemberRepository,
                                    CurrentUserService currentUserService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public ProjectMemberResponse addMember(Long projectId, ProjectMemberRequest request) {
        log.info("Adding user ID: {} to project ID: {}", request.getUserId(), projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec l'identifiant: " + projectId));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserService.isSupervisor() && !currentUserService.isAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)) {
                throw new AccessDeniedException("Un encadrant ne peut gérer que les membres des projets dont il est membre");
            }
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'identifiant: " + request.getUserId()));

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new ResourceAlreadyExistsException("L'utilisateur avec l'identifiant " + request.getUserId() + " est déjà membre du projet");
        }

        ProjectMember member = new ProjectMember(project, user);
        ProjectMember savedMember = projectMemberRepository.save(member);
        log.info("User ID: {} successfully added to project ID: {}", request.getUserId(), projectId);

        return toResponse(savedMember);
    }

    @Override
    public void removeMember(Long projectId, Long userId) {
        log.info("Removing user ID: {} from project ID: {}", userId, projectId);

        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Projet introuvable avec l'identifiant: " + projectId);
        }

        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserService.isSupervisor() && !currentUserService.isAdmin()) {
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)) {
                throw new AccessDeniedException("Un encadrant ne peut gérer que les membres des projets dont il est membre");
            }
        }

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre introuvable dans ce projet avec l'identifiant utilisateur: " + userId));

        projectMemberRepository.delete(member);
        log.info("User ID: {} removed from project ID: {}", userId, projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        log.debug("Fetching members for project ID: {}", projectId);

        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Projet introuvable avec l'identifiant: " + projectId);
        }

        Long currentUserId = currentUserService.getCurrentUserId();
        boolean isAuthorized = currentUserService.isAdmin() ||
                projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId);

        if (!isAuthorized) {
            throw new AccessDeniedException("Accès refusé : vous devez être membre du projet ou administrateur");
        }

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return members.stream()
                .map(this::toResponse)
                .toList();
    }

    private ProjectMemberResponse toResponse(ProjectMember member) {
        User user = member.getUser();
        return new ProjectMemberResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                member.getJoinedAt()
        );
    }
}

package com.pfe.management.service.impl;

import com.pfe.management.dto.request.UserActiveUpdateRequest;
import com.pfe.management.dto.request.UserCreateRequest;
import com.pfe.management.dto.request.UserRoleUpdateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.UserResponse;
import com.pfe.management.entity.Role;
import com.pfe.management.entity.User;
import com.pfe.management.exception.BusinessRuleException;
import com.pfe.management.exception.ResourceAlreadyExistsException;
import com.pfe.management.exception.ResourceNotFoundException;
import com.pfe.management.mapper.UserMapper;
import com.pfe.management.repository.UserRepository;
import com.pfe.management.security.CurrentUserService;
import com.pfe.management.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Role role, String q, Pageable pageable) {
        log.debug("Fetching users list with filters - role: {}, q: {}", role, q);

        String searchPattern = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        Page<User> page = userRepository.search(role, searchPattern, pageable);

        List<UserResponse> content = page.getContent()
                .stream()
                .map(userMapper::toResponse)
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
    public UserResponse createUser(UserCreateRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Creating new user with email: '{}' and role: {}", email, request.getRole());

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Un utilisateur avec cet email existe déjà : " + email);
        }

        User user = userMapper.toEntity(request, passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse updateUserRole(Long id, UserRoleUpdateRequest request) {
        log.info("Updating role of user ID: {} to {}", id, request.getRole());

        User user = findUserOrThrow(id);
        Role newRole = request.getRole();
        Long currentUserId = currentUserService.getCurrentUserId();

        if (user.getId().equals(currentUserId) && user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            throw new BusinessRuleException("Un administrateur ne peut pas retirer son propre rôle ADMIN");
        }

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        log.info("User ID: {} role updated successfully", updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse updateUserActive(Long id, UserActiveUpdateRequest request) {
        log.info("Updating active status of user ID: {} to {}", id, request.getActive());

        User user = findUserOrThrow(id);
        Long currentUserId = currentUserService.getCurrentUserId();

        if (Boolean.FALSE.equals(request.getActive()) && user.getId().equals(currentUserId)) {
            throw new BusinessRuleException("Un administrateur ne peut pas désactiver son propre compte");
        }

        user.setActive(Boolean.TRUE.equals(request.getActive()));
        User updatedUser = userRepository.save(user);
        log.info("User ID: {} active status updated successfully", updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'identifiant: " + id));
    }
}

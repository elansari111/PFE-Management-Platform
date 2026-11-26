package com.pfe.management.service;

import com.pfe.management.dto.request.UserActiveUpdateRequest;
import com.pfe.management.dto.request.UserCreateRequest;
import com.pfe.management.dto.request.UserRoleUpdateRequest;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.UserResponse;
import com.pfe.management.entity.Role;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PageResponse<UserResponse> getAllUsers(Role role, String q, Pageable pageable);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUserRole(Long id, UserRoleUpdateRequest request);

    UserResponse updateUserActive(Long id, UserActiveUpdateRequest request);
}

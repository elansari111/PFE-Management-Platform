package com.pfe.management.mapper;

import com.pfe.management.dto.request.UserCreateRequest;
import com.pfe.management.dto.response.UserResponse;
import com.pfe.management.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequest request, String encodedPassword) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(encodedPassword);
        user.setRole(request.getRole());
        user.setActive(true);
        return user;
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.isActive(),
                user.getCreatedAt()
        );
    }
}

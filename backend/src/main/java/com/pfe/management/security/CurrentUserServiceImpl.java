package com.pfe.management.security;

import com.pfe.management.entity.Role;
import com.pfe.management.entity.User;
import com.pfe.management.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(User::getId)
                    .orElse(null);
        }

        if (principal instanceof String username) {
            return userRepository.findByEmail(username)
                    .map(User::getId)
                    .orElse(null);
        }

        return null;
    }

    @Override
    public Role getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        for (GrantedAuthority authority : auth.getAuthorities()) {
            String authName = authority.getAuthority();
            if (authName.startsWith("ROLE_")) {
                try {
                    return Role.valueOf(authName.substring(5));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return null;
    }

    @Override
    public boolean isAdmin() {
        return Role.ADMIN.equals(getCurrentUserRole());
    }

    @Override
    public boolean isSupervisor() {
        return Role.SUPERVISOR.equals(getCurrentUserRole());
    }

    @Override
    public boolean isStudent() {
        return Role.STUDENT.equals(getCurrentUserRole());
    }

    @Override
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}

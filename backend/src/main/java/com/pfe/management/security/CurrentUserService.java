package com.pfe.management.security;

import com.pfe.management.entity.Role;

public interface CurrentUserService {

    Long getCurrentUserId();

    Role getCurrentUserRole();

    boolean isAdmin();

    boolean isSupervisor();

    boolean isStudent();

    boolean isAuthenticated();
}

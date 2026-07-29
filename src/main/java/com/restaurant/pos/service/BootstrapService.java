package com.restaurant.pos.service;

import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BootstrapService {

    private static final Logger log = LoggerFactory.getLogger(BootstrapService.class);

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final AuthService authService;

    public BootstrapService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public void ensureDefaultAdminExists() {
        if (userRepository.findByUsername(DEFAULT_ADMIN_USERNAME).isPresent()) {
            return;
        }

        char[] password = DEFAULT_ADMIN_PASSWORD.toCharArray();
        User admin = User.builder()
                .username(DEFAULT_ADMIN_USERNAME)
                .displayName("Administrator")
                .passwordHash(authService.hashPassword(password))
                .role(Role.ADMINISTRATOR)
                .build();
        userRepository.insert(admin);
        log.warn("Created default admin account (username: {}). Change this password immediately.",
                DEFAULT_ADMIN_USERNAME);
    }
}

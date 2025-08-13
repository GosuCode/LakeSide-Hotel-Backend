package com.dailycodework.lakesidehotel.config;

import com.dailycodework.lakesidehotel.model.Role;
import com.dailycodework.lakesidehotel.model.User;
import com.dailycodework.lakesidehotel.repository.RoleRepository;
import com.dailycodework.lakesidehotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initializing default roles and admin user...");

        // Create ROLE_USER if it doesn't exist
        Role userRole = getOrCreateRole("ROLE_USER");

        // Create ROLE_ADMIN if it doesn't exist
        Role adminRole = getOrCreateRole("ROLE_ADMIN");

        // Create default admin user if it doesn't exist
        if (!userRepository.existsByEmail("admin@gmail.com")) {
            User adminUser = new User();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Admin");
            adminUser.setEmail("admin@gmail.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));

            // Set the admin role
            adminUser.setRoles(new HashSet<>());
            adminUser.getRoles().add(adminRole);

            userRepository.save(adminUser);
            log.info("Created default admin user: admin@gmail.com");
        } else {
            log.info("Default admin user already exists");
        }

        log.info("Data initialization completed");
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role(roleName);
                    Role savedRole = roleRepository.save(role);
                    log.info("Created {} role", roleName);
                    return savedRole;
                });
    }
}

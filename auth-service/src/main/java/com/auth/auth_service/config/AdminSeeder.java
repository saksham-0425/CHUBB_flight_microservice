package com.auth.auth_service.config;

import com.auth.auth_service.model.AppUser;
import com.auth.auth_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder encoder) {

        return args -> {

            String adminEmail = "admin@aeroflux.com";

            if (repo.findByEmail(adminEmail).isPresent()) {
                return;
            }

            AppUser admin = new AppUser();
            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(encoder.encode("admin123"));
            admin.setRoles(Set.of("ROLE_ADMIN"));

            repo.save(admin);

            System.out.println("✅ ADMIN user seeded");
        };
    }
}

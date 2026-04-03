package fr.manu.sprinvoice.config;

import fr.manu.sprinvoice.models.Role;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.repositories.RoleRepository;
import fr.manu.sprinvoice.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(RoleRepository roleRepository,
                                           UserRepository userRepository,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_ADMIN");
                return roleRepository.save(r);
            });

            roleRepository.findByName("ROLE_CLIENT").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_CLIENT");
                return roleRepository.save(r);
            });

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("Admin1234!"));
                admin.setRole(adminRole);
                userRepository.save(admin);
                System.out.println(">>> Utilisateur admin créé : admin / Admin1234!");
            }
        };
    }
}

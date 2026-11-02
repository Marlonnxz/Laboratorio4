package co.edu.uptc.auth.config;

import co.edu.uptc.auth.entity.Role;
import co.edu.uptc.auth.entity.User;
import co.edu.uptc.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users in auth_db...");

            User admin = new User(
                    "admin@uptc.edu.co",
                    passwordEncoder.encode("Admin123*"),
                    "Administrador Sistema",
                    Role.ADMIN,
                    null
            );

            User student1 = new User(
                    "estudiante1@uptc.edu.co",
                    passwordEncoder.encode("Student123*"),
                    "Juan Pérez",
                    Role.ESTUDIANTE,
                    1L
            );

            User student2 = new User(
                    "estudiante2@uptc.edu.co",
                    passwordEncoder.encode("Student123*"),
                    "María Gómez",
                    Role.ESTUDIANTE,
                    2L
            );

            userRepository.save(admin);
            userRepository.save(student1);
            userRepository.save(student2);

            log.info("Default users created: admin@uptc.edu.co, estudiante1@uptc.edu.co, estudiante2@uptc.edu.co");
        }
    }
}


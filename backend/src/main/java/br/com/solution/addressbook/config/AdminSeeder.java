package br.com.solution.addressbook.config;

import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRepository;
import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.shared.Cpf;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final String name;
    private final String cpf;
    private final String password;

    public AdminSeeder(UserRepository repository, PasswordEncoder encoder,
                       @Value("${app.admin.name}") String name,
                       @Value("${app.admin.cpf}") String cpf,
                       @Value("${app.admin.password}") String password) {
        this.repository = repository;
        this.encoder = encoder;
        this.name = name;
        this.cpf = Cpf.normalize(cpf);
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!repository.existsByCpf(cpf)) {
            repository.save(new UserEntity(name, cpf, LocalDate.of(1990, 1, 1),
                    encoder.encode(password), UserRole.ADMIN));
            log.info("Initial administrator created");
        }
    }
}


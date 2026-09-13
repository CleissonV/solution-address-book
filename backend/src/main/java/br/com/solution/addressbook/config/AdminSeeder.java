package br.com.solution.addressbook.config;

import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRepository;
import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.shared.Cpf;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
    public void run(ApplicationArguments args) {
        validateConfiguration();
        if (repository.existsByCpf(cpf)) return;

        try {
            repository.saveAndFlush(new UserEntity(name, cpf, LocalDate.of(1990, 1, 1),
                    encoder.encode(password), UserRole.ADMIN));
            log.info("Initial administrator created");
        } catch (DataIntegrityViolationException exception) {
            if (!repository.existsByCpf(cpf)) throw exception;
            log.info("Initial administrator already created by another instance");
        }
    }

    private void validateConfiguration() {
        if (!Cpf.isValid(cpf)) {
            throw new IllegalStateException("APP_ADMIN_CPF must contain a valid CPF");
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalStateException("APP_ADMIN_PASSWORD must contain between 8 and 72 characters");
        }
    }
}

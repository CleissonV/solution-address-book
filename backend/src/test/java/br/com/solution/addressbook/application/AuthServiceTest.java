package br.com.solution.addressbook.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.solution.addressbook.api.dto.AuthDtos.LoginRequest;
import br.com.solution.addressbook.api.error.DomainException;
import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRepository;
import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.security.JwtService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void inactiveAccountCannotLogin() {
        UserEntity user = new UserEntity("Usuario", "39053344705", LocalDate.of(1990, 1, 1),
                "hash", UserRole.USER);
        user.deactivate(UUID.randomUUID(), Instant.now());
        when(userRepository.findByCpf("39053344705")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("39053344705", "Password@123")))
                .isInstanceOfSatisfying(DomainException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(403);
                    assertThat(exception.getCode()).isEqualTo("ACCOUNT_INACTIVE");
                });
    }
}

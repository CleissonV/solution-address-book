package br.com.solution.addressbook.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.solution.addressbook.application.dto.AuthDtos.LoginRequest;
import br.com.solution.addressbook.application.error.DomainException;
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
        UserEntity user =
                new UserEntity(
                        "Usuario", "39053344705", LocalDate.of(1990, 1, 1), "hash", UserRole.USER);
        user.deactivate(UUID.randomUUID(), Instant.now());
        when(userRepository.findByCpf("39053344705")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("39053344705", "Password@123")))
                .isInstanceOfSatisfying(
                        DomainException.class,
                        exception -> {
                            assertThat(exception.getCode()).isEqualTo("ACCOUNT_INACTIVE");
                        });
    }

    @Test
    void activeAccountReceivesToken() {
        UserEntity user = user();
        when(userRepository.findByCpf("39053344705")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "hash")).thenReturn(true);
        when(jwtService.issue(any())).thenReturn("signed-token");

        var response = service.login(new LoginRequest("390.533.447-05", "Password@123"));

        assertThat(response.token()).isEqualTo("signed-token");
        assertThat(response.user().cpf()).isEqualTo("39053344705");
    }

    @Test
    void invalidPasswordDoesNotIssueToken() {
        when(userRepository.findByCpf("39053344705")).thenReturn(Optional.of(user()));
        when(passwordEncoder.matches("WrongPassword", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("39053344705", "WrongPassword")))
                .isInstanceOfSatisfying(
                        DomainException.class,
                        exception ->
                                assertThat(exception.getCode()).isEqualTo("INVALID_CREDENTIALS"));
        verifyNoInteractions(jwtService);
    }

    @Test
    void unknownCpfReturnsSameCredentialError() {
        when(userRepository.findByCpf("39053344705")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("39053344705", "Password@123")))
                .isInstanceOfSatisfying(
                        DomainException.class,
                        exception ->
                                assertThat(exception.getCode()).isEqualTo("INVALID_CREDENTIALS"));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    private static UserEntity user() {
        return new UserEntity(
                "Usuario", "39053344705", LocalDate.of(1990, 1, 1), "hash", UserRole.USER);
    }
}

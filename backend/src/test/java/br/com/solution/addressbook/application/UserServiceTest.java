package br.com.solution.addressbook.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.solution.addressbook.api.dto.UserDtos.UpdateUserRequest;
import br.com.solution.addressbook.api.dto.UserDtos.UpdateUserStatusRequest;
import br.com.solution.addressbook.api.error.DomainException;
import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRepository;
import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.domain.user.UserStatus;
import br.com.solution.addressbook.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void adminCanUpdateAnotherUserIncludingRole() {
        UUID requestedId = UUID.randomUUID();
        UserEntity user = user("Usuario", "39053344705", UserRole.USER);
        AuthenticatedUser admin = actor(UUID.randomUUID(), UserRole.ADMIN);
        when(userRepository.findByIdForUpdate(requestedId)).thenReturn(Optional.of(user));

        var result = service.update(requestedId,
                new UpdateUserRequest("Usuario Atualizado", "52998224725",
                        LocalDate.of(1992, 5, 14), UserRole.ADMIN), admin);

        assertThat(result.name()).isEqualTo("Usuario Atualizado");
        assertThat(result.cpf()).isEqualTo("52998224725");
        assertThat(result.role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void commonUserCanUpdateOwnDataWithoutChangingRole() {
        UUID requestedId = UUID.randomUUID();
        UserEntity user = user("Usuario", "39053344705", UserRole.USER);
        AuthenticatedUser actor = actor(requestedId, UserRole.USER);
        when(userRepository.findByIdForUpdate(requestedId)).thenReturn(Optional.of(user));

        var result = service.update(requestedId,
                new UpdateUserRequest("Meu Novo Nome", "39053344705",
                        LocalDate.of(1993, 6, 20), null), actor);

        assertThat(result.name()).isEqualTo("Meu Novo Nome");
        assertThat(result.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void commonUserCannotSendRoleChange() {
        UUID requestedId = UUID.randomUUID();
        UserEntity user = user("Usuario", "39053344705", UserRole.USER);
        AuthenticatedUser actor = actor(requestedId, UserRole.USER);
        when(userRepository.findByIdForUpdate(requestedId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.update(requestedId,
                new UpdateUserRequest("Usuario", "39053344705",
                        LocalDate.of(1990, 1, 1), UserRole.ADMIN), actor))
                .isInstanceOfSatisfying(DomainException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(403);
                    assertThat(exception.getCode()).isEqualTo("ROLE_CHANGE_FORBIDDEN");
                });
    }

    @Test
    void commonUserCannotUpdateAnotherUser() {
        UUID actorId = UUID.randomUUID();
        UUID requestedId = UUID.randomUUID();

        assertThatThrownBy(() -> service.update(requestedId,
                new UpdateUserRequest("Outro", "39053344705",
                        LocalDate.of(1990, 1, 1), null), actor(actorId, UserRole.USER)))
                .isInstanceOfSatisfying(DomainException.class,
                        exception -> assertThat(exception.getStatus().value()).isEqualTo(403));
    }

    @Test
    void adminCanDeactivateAndReactivateAnotherUser() {
        UUID requestedId = UUID.randomUUID();
        UserEntity user = user("Usuario", "39053344705", UserRole.USER);
        AuthenticatedUser admin = actor(UUID.randomUUID(), UserRole.ADMIN);
        when(userRepository.findAllByRoleForUpdate(UserRole.ADMIN)).thenReturn(List.of());
        when(userRepository.findByIdForUpdate(requestedId)).thenReturn(Optional.of(user));

        var inactive = service.updateStatus(requestedId,
                new UpdateUserStatusRequest(UserStatus.INACTIVE), admin);
        var active = service.updateStatus(requestedId,
                new UpdateUserStatusRequest(UserStatus.ACTIVE), admin);

        assertThat(inactive.status()).isEqualTo(UserStatus.INACTIVE);
        assertThat(inactive.deactivatedAt()).isNotNull();
        assertThat(active.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(active.deactivatedAt()).isNull();
    }

    @Test
    void adminCannotDeactivateOwnAccount() {
        UUID adminId = UUID.randomUUID();

        assertThatThrownBy(() -> service.updateStatus(adminId,
                new UpdateUserStatusRequest(UserStatus.INACTIVE), actor(adminId, UserRole.ADMIN)))
                .isInstanceOfSatisfying(DomainException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(409);
                    assertThat(exception.getCode()).isEqualTo("SELF_DEACTIVATION_FORBIDDEN");
                });
    }

    @Test
    void lastActiveAdministratorCannotLoseRole() {
        UUID adminId = UUID.randomUUID();
        UserEntity administrator = user("Admin", "52998224725", UserRole.ADMIN);
        when(userRepository.findAllByRoleForUpdate(UserRole.ADMIN)).thenReturn(List.of(administrator));
        when(userRepository.findByIdForUpdate(adminId)).thenReturn(Optional.of(administrator));

        assertThatThrownBy(() -> service.update(adminId,
                new UpdateUserRequest("Admin", "52998224725",
                        LocalDate.of(1990, 1, 1), UserRole.USER), actor(adminId, UserRole.ADMIN)))
                .isInstanceOfSatisfying(DomainException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo("LAST_ACTIVE_ADMIN"));
    }

    @Test
    void inactiveAccountIsReadOnly() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user("Usuario", "39053344705", UserRole.USER);
        user.deactivate(UUID.randomUUID(), java.time.Instant.now());
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.update(userId,
                new UpdateUserRequest("Novo Nome", "39053344705",
                        LocalDate.of(1990, 1, 1), null), actor(UUID.randomUUID(), UserRole.ADMIN)))
                .isInstanceOfSatisfying(DomainException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo("ACCOUNT_INACTIVE_READ_ONLY"));
    }

    private static UserEntity user(String name, String cpf, UserRole role) {
        return new UserEntity(name, cpf, LocalDate.of(1990, 1, 1), "hash", role);
    }

    private static AuthenticatedUser actor(UUID id, UserRole role) {
        return new AuthenticatedUser(id, "52998224725", "hash", role.name());
    }
}

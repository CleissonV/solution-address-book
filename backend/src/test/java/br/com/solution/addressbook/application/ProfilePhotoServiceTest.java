package br.com.solution.addressbook.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.solution.addressbook.api.error.DomainException;
import br.com.solution.addressbook.domain.user.ProfilePhotoEntity;
import br.com.solution.addressbook.domain.user.ProfilePhotoRepository;
import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ProfilePhotoServiceTest {
    @Mock ProfilePhotoRepository photoRepository;
    @Mock UserService userService;
    private ProfilePhotoService service;

    @BeforeEach
    void setUp() {
        service = new ProfilePhotoService(photoRepository, userService);
    }

    @Test
    void uploadsValidPngAndUpdatesUserVersion() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user();
        byte[] png = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1};
        when(userService.requireAccessible(userId, actor(userId))).thenReturn(user);
        when(photoRepository.findById(userId)).thenReturn(Optional.empty());

        var result = service.upload(userId,
                new MockMultipartFile("file", "profile.png", "text/plain", png), actor(userId));

        assertThat(result.version()).isPositive();
        assertThat(user.getProfilePhotoVersion()).isEqualTo(result.version());
        verify(photoRepository).save(any(ProfilePhotoEntity.class));
    }

    @Test
    void rejectsContentWhoseSignatureIsNotAnImage() {
        UUID userId = UUID.randomUUID();
        when(userService.requireAccessible(userId, actor(userId))).thenReturn(user());

        assertThatThrownBy(() -> service.upload(userId,
                new MockMultipartFile("file", "fake.png", "image/png", "not-an-image".getBytes()), actor(userId)))
                .isInstanceOfSatisfying(DomainException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(422);
                    assertThat(exception.getCode()).isEqualTo("INVALID_PHOTO_TYPE");
                });
    }

    @Test
    void rejectsPhotoLargerThanTwoMegabytes() {
        UUID userId = UUID.randomUUID();
        when(userService.requireAccessible(userId, actor(userId))).thenReturn(user());

        assertThatThrownBy(() -> service.upload(userId,
                new MockMultipartFile("file", "large.png", "image/png",
                        new byte[ProfilePhotoService.MAX_PHOTO_SIZE + 1]), actor(userId)))
                .isInstanceOfSatisfying(DomainException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(413);
                    assertThat(exception.getCode()).isEqualTo("PHOTO_TOO_LARGE");
                });
    }

    @Test
    void removesExistingPhotoAndClearsVersion() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user();
        user.markProfilePhotoChanged(10);
        when(userService.requireAccessible(userId, actor(userId))).thenReturn(user);
        when(photoRepository.existsById(userId)).thenReturn(true);

        service.delete(userId, actor(userId));

        assertThat(user.getProfilePhotoVersion()).isNull();
        verify(photoRepository).deleteById(userId);
    }

    @Test
    void preventsAdministratorFromUploadingAnotherUsersPhoto() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser administrator = actor(UUID.randomUUID(), UserRole.ADMIN);

        assertThatThrownBy(() -> service.upload(userId,
                new MockMultipartFile("file", "profile.png", "image/png", new byte[] {(byte) 0x89}),
                administrator))
                .isInstanceOfSatisfying(DomainException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(403);
                    assertThat(exception.getCode()).isEqualTo("PROFILE_PHOTO_SELF_ONLY");
                });

        verifyNoInteractions(userService, photoRepository);
    }

    @Test
    void preventsAdministratorFromDeletingAnotherUsersPhoto() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser administrator = actor(UUID.randomUUID(), UserRole.ADMIN);

        assertThatThrownBy(() -> service.delete(userId, administrator))
                .isInstanceOfSatisfying(DomainException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(403);
                    assertThat(exception.getCode()).isEqualTo("PROFILE_PHOTO_SELF_ONLY");
                });

        verifyNoInteractions(userService, photoRepository);
    }

    private static UserEntity user() {
        return new UserEntity("Usuario", "39053344705", LocalDate.of(1990, 1, 1), "hash", UserRole.USER);
    }

    private static AuthenticatedUser actor(UUID id) {
        return actor(id, UserRole.USER);
    }

    private static AuthenticatedUser actor(UUID id, UserRole role) {
        return new AuthenticatedUser(id, "39053344705", "hash", role.name());
    }
}

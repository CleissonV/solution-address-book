package br.com.solution.addressbook.application;

import br.com.solution.addressbook.api.dto.UserDtos.ProfilePhotoResponse;
import br.com.solution.addressbook.api.error.DomainException;
import br.com.solution.addressbook.domain.user.ProfilePhotoEntity;
import br.com.solution.addressbook.domain.user.ProfilePhotoRepository;
import br.com.solution.addressbook.security.AuthenticatedUser;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfilePhotoService {
    static final int MAX_PHOTO_SIZE = 2 * 1024 * 1024;

    private final ProfilePhotoRepository photoRepository;
    private final UserService userService;

    public ProfilePhotoService(ProfilePhotoRepository photoRepository, UserService userService) {
        this.photoRepository = photoRepository;
        this.userService = userService;
    }

    @Transactional
    public ProfilePhotoResponse upload(UUID userId, MultipartFile file, AuthenticatedUser actor) {
        assertOwner(userId, actor);
        var user = userService.requireAccessible(userId, actor);
        byte[] content = readAndValidate(file);
        String contentType = detectContentType(content);
        Instant now = Instant.now();
        long previousVersion = user.getProfilePhotoVersion() == null ? 0 : user.getProfilePhotoVersion();
        long version = Math.max(now.toEpochMilli(), previousVersion + 1);

        ProfilePhotoEntity photo = photoRepository.findById(userId)
                .orElseGet(() -> new ProfilePhotoEntity(userId, contentType, content, now));
        photo.update(contentType, content, now);
        photoRepository.save(photo);
        user.markProfilePhotoChanged(version);
        return new ProfilePhotoResponse(version);
    }

    @Transactional(readOnly = true)
    public ProfilePhotoContent get(UUID userId, AuthenticatedUser actor) {
        userService.ensureAccessible(userId, actor);
        return photoRepository.findById(userId)
                .map(photo -> new ProfilePhotoContent(photo.getContentType(), photo.getContent(),
                        photo.getUpdatedAt().toEpochMilli()))
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "PROFILE_PHOTO_NOT_FOUND",
                        "Foto de perfil nao encontrada."));
    }

    @Transactional
    public void delete(UUID userId, AuthenticatedUser actor) {
        assertOwner(userId, actor);
        var user = userService.requireAccessible(userId, actor);
        if (!photoRepository.existsById(userId)) {
            throw new DomainException(HttpStatus.NOT_FOUND, "PROFILE_PHOTO_NOT_FOUND",
                    "Foto de perfil nao encontrada.");
        }
        photoRepository.deleteById(userId);
        user.clearProfilePhoto();
    }

    private static void assertOwner(UUID userId, AuthenticatedUser actor) {
        if (!actor.id().equals(userId)) {
            throw new DomainException(HttpStatus.FORBIDDEN, "PROFILE_PHOTO_SELF_ONLY",
                    "Somente o proprio usuario pode alterar a foto de perfil.");
        }
    }

    private static byte[] readAndValidate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "EMPTY_PROFILE_PHOTO",
                    "Selecione uma foto.");
        }
        if (file.getSize() > MAX_PHOTO_SIZE) {
            throw new DomainException(HttpStatus.PAYLOAD_TOO_LARGE, "PHOTO_TOO_LARGE",
                    "A foto deve ter no maximo 2 MB.");
        }
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "PHOTO_READ_ERROR",
                    "Nao foi possivel ler a foto.");
        }
    }

    static String detectContentType(byte[] content) {
        if (isPng(content)) return "image/png";
        if (isJpeg(content)) return "image/jpeg";
        throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_PHOTO_TYPE",
                "Use uma imagem PNG ou JPEG.");
    }

    private static boolean isPng(byte[] content) {
        byte[] signature = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        if (content.length < signature.length) return false;
        for (int index = 0; index < signature.length; index++) {
            if (content[index] != signature[index]) return false;
        }
        return true;
    }

    private static boolean isJpeg(byte[] content) {
        return content.length >= 3 && content[0] == (byte) 0xff
                && content[1] == (byte) 0xd8 && content[2] == (byte) 0xff;
    }

    public record ProfilePhotoContent(String contentType, byte[] content, long version) {}
}

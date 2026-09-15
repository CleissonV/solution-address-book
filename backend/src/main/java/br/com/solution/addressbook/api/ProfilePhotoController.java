package br.com.solution.addressbook.api;

import br.com.solution.addressbook.application.ProfilePhotoService;
import br.com.solution.addressbook.application.dto.UserDtos.ProfilePhotoResponse;
import br.com.solution.addressbook.security.AuthenticatedUser;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/{userId}/photo")
public class ProfilePhotoController {
    private final ProfilePhotoService profilePhotoService;

    public ProfilePhotoController(ProfilePhotoService profilePhotoService) {
        this.profilePhotoService = profilePhotoService;
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ProfilePhotoResponse> upload(
            @PathVariable UUID userId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok(profilePhotoService.upload(userId, file, actor));
    }

    @GetMapping
    ResponseEntity<byte[]> get(
            @PathVariable UUID userId, @AuthenticationPrincipal AuthenticatedUser actor) {
        var photo = profilePhotoService.get(userId, actor);
        byte[] content = photo.content();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .contentLength(content.length)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePrivate().immutable())
                .eTag('"' + String.valueOf(photo.version()) + '"')
                .body(content);
    }

    @DeleteMapping
    ResponseEntity<Void> delete(
            @PathVariable UUID userId, @AuthenticationPrincipal AuthenticatedUser actor) {
        profilePhotoService.delete(userId, actor);
        return ResponseEntity.noContent().build();
    }
}

package br.com.solution.addressbook.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profile_photos")
public class ProfilePhotoEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "content_type", nullable = false, length = 32)
    private String contentType;

    @Column(nullable = false)
    private byte[] content;

    @Column(name = "size_bytes", nullable = false)
    private int sizeBytes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProfilePhotoEntity() {}

    public ProfilePhotoEntity(UUID userId, String contentType, byte[] content, Instant updatedAt) {
        this.userId = userId;
        update(contentType, content, updatedAt);
    }

    public void update(String contentType, byte[] content, Instant updatedAt) {
        this.contentType = contentType;
        this.content = content.clone();
        this.sizeBytes = content.length;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() { return userId; }
    public String getContentType() { return contentType; }
    public byte[] getContent() { return content.clone(); }
    public int getSizeBytes() { return sizeBytes; }
    public Instant getUpdatedAt() { return updatedAt; }
}

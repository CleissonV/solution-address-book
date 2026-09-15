package br.com.solution.addressbook.domain.user;

import br.com.solution.addressbook.domain.address.AddressEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "profile_photo_version")
    private Long profilePhotoVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @Column(name = "deactivated_by")
    private UUID deactivatedBy;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<AddressEntity> addresses = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntity() {}

    public UserEntity(
            String name, String cpf, LocalDate birthDate, String passwordHash, UserRole role) {
        this.name = name;
        this.cpf = cpf;
        this.birthDate = birthDate;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public void updateProfile(String name, String cpf, LocalDate birthDate, UserRole role) {
        this.name = name;
        this.cpf = cpf;
        this.birthDate = birthDate;
        this.role = role;
    }

    public void markProfilePhotoChanged(long version) {
        this.profilePhotoVersion = version;
    }

    public void clearProfilePhoto() {
        this.profilePhotoVersion = null;
    }

    public void deactivate(UUID actorId, Instant occurredAt) {
        this.status = UserStatus.INACTIVE;
        this.deactivatedAt = occurredAt;
        this.deactivatedBy = actorId;
    }

    public void reactivate() {
        this.status = UserStatus.ACTIVE;
        this.deactivatedAt = null;
        this.deactivatedBy = null;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Long getProfilePhotoVersion() {
        return profilePhotoVersion;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getDeactivatedAt() {
        return deactivatedAt;
    }

    public UUID getDeactivatedBy() {
        return deactivatedBy;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public List<AddressEntity> getAddresses() {
        return addresses;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

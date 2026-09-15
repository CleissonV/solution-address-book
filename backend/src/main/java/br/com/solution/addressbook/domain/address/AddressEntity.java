package br.com.solution.addressbook.domain.address;

import br.com.solution.addressbook.domain.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "addresses")
@EntityListeners(AuditingEntityListener.class)
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "zip_code", nullable = false, length = 8)
    private String zipCode;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(length = 120)
    private String complement;

    @Column(nullable = false, length = 180)
    private String street;

    @Column(nullable = false, length = 120)
    private String neighborhood;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AddressEntity() {}

    public AddressEntity(
            UserEntity user,
            String zipCode,
            String number,
            String complement,
            String street,
            String neighborhood,
            String city,
            String state,
            boolean primary) {
        this.user = user;
        update(zipCode, number, complement, street, neighborhood, city, state);
        this.primary = primary;
    }

    public void update(
            String zipCode,
            String number,
            String complement,
            String street,
            String neighborhood,
            String city,
            String state) {
        this.zipCode = zipCode;
        this.number = number;
        this.complement = complement;
        this.street = street;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getStreet() {
        return street;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public boolean isPrimary() {
        return primary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

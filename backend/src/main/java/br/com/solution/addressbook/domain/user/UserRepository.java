package br.com.solution.addressbook.domain.user;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    @EntityGraph(attributePaths = "addresses")
    Optional<UserEntity> findWithAddressesById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.id = :id")
    Optional<UserEntity> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.role = :role order by u.id")
    List<UserEntity> findAllByRoleForUpdate(@Param("role") UserRole role);
}

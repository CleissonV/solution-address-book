package br.com.solution.addressbook.domain.address;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AddressEntity a where a.user.id = :userId order by a.createdAt asc")
    List<AddressEntity> findAllForUpdate(@Param("userId") UUID userId);
}

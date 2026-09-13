package br.com.solution.addressbook.domain.user;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilePhotoRepository extends JpaRepository<ProfilePhotoEntity, UUID> {}

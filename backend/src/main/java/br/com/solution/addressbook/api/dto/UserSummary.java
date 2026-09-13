package br.com.solution.addressbook.api.dto;

import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.domain.user.UserStatus;
import java.time.LocalDate;
import java.util.UUID;

public record UserSummary(UUID id, String name, String cpf, LocalDate birthDate, UserRole role,
                          Long profilePhotoVersion, UserStatus status) {}

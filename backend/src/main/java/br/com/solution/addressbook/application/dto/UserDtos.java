package br.com.solution.addressbook.application.dto;

import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.domain.user.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class UserDtos {
    private UserDtos() {}

    public record CreateUserRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 14) String cpf,
            @NotNull @Past LocalDate birthDate,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotNull UserRole role) {}

    public record UpdateUserRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 14) String cpf,
            @NotNull @Past LocalDate birthDate,
            UserRole role) {}

    public record UpdateUserStatusRequest(@NotNull UserStatus status) {}

    public record UserDetailsResponse(
            UUID id,
            String name,
            String cpf,
            LocalDate birthDate,
            UserRole role,
            Long profilePhotoVersion,
            UserStatus status,
            Instant deactivatedAt,
            List<AddressDtos.AddressResponse> addresses) {}

    public record ProfilePhotoResponse(long version) {}
}

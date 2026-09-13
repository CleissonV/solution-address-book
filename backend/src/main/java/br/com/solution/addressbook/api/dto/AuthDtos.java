package br.com.solution.addressbook.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank String cpf,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {}

    public record LoginResponse(String token, UserSummary user) {}
}


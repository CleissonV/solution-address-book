package br.com.solution.addressbook.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class AddressDtos {
    private AddressDtos() {}

    public record UpsertAddressRequest(
            @NotBlank @Size(max = 9) String zipCode,
            @NotBlank @Size(max = 20) String number,
            @Size(max = 120) String complement,
            boolean primary
    ) {}

    public record AddressResponse(
            UUID id,
            String zipCode,
            String number,
            String complement,
            String street,
            String neighborhood,
            String city,
            String state,
            boolean primary
    ) {}

    public record PostalCodeResponse(
            String zipCode,
            String street,
            String neighborhood,
            String city,
            String state
    ) {}
}

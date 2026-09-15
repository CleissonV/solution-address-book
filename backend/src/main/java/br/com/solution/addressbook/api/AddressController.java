package br.com.solution.addressbook.api;

import br.com.solution.addressbook.application.AddressService;
import br.com.solution.addressbook.application.dto.AddressDtos.AddressResponse;
import br.com.solution.addressbook.application.dto.AddressDtos.UpsertAddressRequest;
import br.com.solution.addressbook.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    ResponseEntity<AddressResponse> create(
            @PathVariable UUID userId,
            @Valid @RequestBody UpsertAddressRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        AddressResponse created = addressService.create(userId, request, actor);
        return ResponseEntity.created(
                        URI.create("/api/users/" + userId + "/addresses/" + created.id()))
                .body(created);
    }

    @PutMapping("/{addressId}")
    ResponseEntity<AddressResponse> update(
            @PathVariable UUID userId,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpsertAddressRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok(addressService.update(userId, addressId, request, actor));
    }

    @PatchMapping("/{addressId}/primary")
    ResponseEntity<AddressResponse> setPrimary(
            @PathVariable UUID userId,
            @PathVariable UUID addressId,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok(addressService.setPrimary(userId, addressId, actor));
    }

    @DeleteMapping("/{addressId}")
    ResponseEntity<Void> delete(
            @PathVariable UUID userId,
            @PathVariable UUID addressId,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        addressService.delete(userId, addressId, actor);
        return ResponseEntity.noContent().build();
    }
}

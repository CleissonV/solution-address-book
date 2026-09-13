package br.com.solution.addressbook.application;

import br.com.solution.addressbook.api.dto.AddressDtos.AddressResponse;
import br.com.solution.addressbook.api.dto.AddressDtos.PostalCodeResponse;
import br.com.solution.addressbook.api.dto.AddressDtos.UpsertAddressRequest;
import br.com.solution.addressbook.api.error.DomainException;
import br.com.solution.addressbook.domain.address.AddressEntity;
import br.com.solution.addressbook.domain.address.AddressRepository;
import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.infrastructure.viacep.ViaCepService;
import br.com.solution.addressbook.security.AuthenticatedUser;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserService userService;
    private final ViaCepService viaCepService;

    public AddressService(AddressRepository addressRepository, UserService userService, ViaCepService viaCepService) {
        this.addressRepository = addressRepository;
        this.userService = userService;
        this.viaCepService = viaCepService;
    }

    @Transactional
    public AddressResponse create(UUID userId, UpsertAddressRequest request, AuthenticatedUser actor) {
        UserEntity user = userService.requireAccessible(userId, actor);
        PostalCodeResponse postalCode = viaCepService.lookup(request.zipCode());
        List<AddressEntity> addresses = addressRepository.findAllForUpdate(userId);
        boolean primary = addresses.isEmpty() || request.primary();
        if (primary && !addresses.isEmpty()) {
            demoteAll(addresses);
            addressRepository.flush();
        }
        AddressEntity address = new AddressEntity(user, postalCode.zipCode(), request.number().trim(),
                trimToNull(request.complement()), postalCode.street(), postalCode.neighborhood(),
                postalCode.city(), postalCode.state(), primary);
        return UserMapper.toAddress(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(UUID userId, UUID addressId, UpsertAddressRequest request,
                                  AuthenticatedUser actor) {
        userService.requireAccessible(userId, actor);
        PostalCodeResponse postalCode = viaCepService.lookup(request.zipCode());
        List<AddressEntity> addresses = addressRepository.findAllForUpdate(userId);
        AddressEntity address = findIn(addresses, addressId);
        address.update(postalCode.zipCode(), request.number().trim(), trimToNull(request.complement()),
                postalCode.street(), postalCode.neighborhood(), postalCode.city(), postalCode.state());
        if (request.primary()) {
            demoteAll(addresses);
            addressRepository.flush();
            address.setPrimary(true);
        } else if (address.isPrimary() && addresses.size() > 1) {
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "PRIMARY_ADDRESS_REQUIRED",
                    "Defina outro endereco principal antes de remover esta marcacao.");
        }
        return UserMapper.toAddress(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse setPrimary(UUID userId, UUID addressId, AuthenticatedUser actor) {
        userService.requireAccessible(userId, actor);
        List<AddressEntity> addresses = addressRepository.findAllForUpdate(userId);
        AddressEntity target = findIn(addresses, addressId);
        demoteAll(addresses);
        addressRepository.flush();
        target.setPrimary(true);
        return UserMapper.toAddress(target);
    }

    @Transactional
    public void delete(UUID userId, UUID addressId, AuthenticatedUser actor) {
        userService.requireAccessible(userId, actor);
        List<AddressEntity> addresses = addressRepository.findAllForUpdate(userId);
        AddressEntity target = findIn(addresses, addressId);
        boolean wasPrimary = target.isPrimary();
        addressRepository.delete(target);
        if (wasPrimary) {
            addressRepository.flush();
            addresses.stream().filter(address -> !address.getId().equals(addressId)).findFirst()
                    .ifPresent(address -> address.setPrimary(true));
        }
    }

    private static AddressEntity findIn(List<AddressEntity> addresses, UUID id) {
        return addresses.stream().filter(address -> address.getId().equals(id)).findFirst()
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND",
                        "Endereco nao encontrado."));
    }

    private static void demoteAll(List<AddressEntity> addresses) {
        addresses.forEach(address -> address.setPrimary(false));
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

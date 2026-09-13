package br.com.solution.addressbook.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.solution.addressbook.application.dto.AddressDtos.PostalCodeResponse;
import br.com.solution.addressbook.application.dto.AddressDtos.UpsertAddressRequest;
import br.com.solution.addressbook.application.error.DomainException;
import br.com.solution.addressbook.application.port.PostalCodeLookup;
import br.com.solution.addressbook.domain.address.AddressEntity;
import br.com.solution.addressbook.domain.address.AddressRepository;
import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {
    @Mock AddressRepository addressRepository;
    @Mock UserService userService;
    @Mock PostalCodeLookup postalCodeLookup;
    private AddressService service;

    @BeforeEach
    void setUp() {
        service = new AddressService(addressRepository, userService, postalCodeLookup);
    }

    @Test
    void firstAddressIsAlwaysPrimary() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity("Ana", "52998224725", LocalDate.of(1990, 1, 1), "hash", UserRole.USER);
        AuthenticatedUser actor = new AuthenticatedUser(userId, "52998224725", "hash", "USER");
        when(userService.requireAccessible(userId, actor)).thenReturn(user);
        when(postalCodeLookup.lookup("20040020"))
                .thenReturn(new PostalCodeResponse("20040020", "Rua da Assembleia", "Centro", "Rio de Janeiro", "RJ"));
        when(addressRepository.findAllForUpdate(userId)).thenReturn(List.of());
        when(addressRepository.save(any(AddressEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(userId, new UpsertAddressRequest("20040020", "10", null, false), actor);

        assertThat(result.primary()).isTrue();
        assertThat(result.city()).isEqualTo("Rio de Janeiro");
    }

    @Test
    void settingAnotherAddressAsPrimaryDemotesPreviousOne() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user();
        AddressEntity previous = address(user, true);
        AddressEntity target = address(user, false);
        when(addressRepository.findAllForUpdate(userId)).thenReturn(List.of(previous, target));

        var result = service.setPrimary(userId, target.getId(), actor(userId));

        assertThat(previous.isPrimary()).isFalse();
        assertThat(target.isPrimary()).isTrue();
        assertThat(result.id()).isEqualTo(target.getId());
        verify(addressRepository).flush();
    }

    @Test
    void deletingPrimaryAddressPromotesRemainingAddress() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user();
        AddressEntity primary = address(user, true);
        AddressEntity remaining = address(user, false);
        when(addressRepository.findAllForUpdate(userId)).thenReturn(List.of(primary, remaining));

        service.delete(userId, primary.getId(), actor(userId));

        verify(addressRepository).delete(primary);
        verify(addressRepository).flush();
        assertThat(remaining.isPrimary()).isTrue();
    }

    @Test
    void unknownAddressCannotBeChanged() {
        UUID userId = UUID.randomUUID();
        when(addressRepository.findAllForUpdate(userId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.setPrimary(userId, UUID.randomUUID(), actor(userId)))
                .isInstanceOfSatisfying(DomainException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo("ADDRESS_NOT_FOUND"));
    }

    private static UserEntity user() {
        return new UserEntity("Ana", "52998224725", LocalDate.of(1990, 1, 1), "hash", UserRole.USER);
    }

    private static AuthenticatedUser actor(UUID id) {
        return new AuthenticatedUser(id, "52998224725", "hash", "USER");
    }

    private static AddressEntity address(UserEntity user, boolean primary) {
        AddressEntity address = new AddressEntity(user, "20040020", "10", null,
                "Rua da Assembleia", "Centro", "Rio de Janeiro", "RJ", primary);
        ReflectionTestUtils.setField(address, "id", UUID.randomUUID());
        return address;
    }
}

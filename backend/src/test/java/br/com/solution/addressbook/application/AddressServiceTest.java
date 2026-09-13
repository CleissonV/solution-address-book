package br.com.solution.addressbook.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.solution.addressbook.api.dto.AddressDtos.PostalCodeResponse;
import br.com.solution.addressbook.api.dto.AddressDtos.UpsertAddressRequest;
import br.com.solution.addressbook.domain.address.AddressEntity;
import br.com.solution.addressbook.domain.address.AddressRepository;
import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.infrastructure.viacep.ViaCepService;
import br.com.solution.addressbook.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {
    @Mock AddressRepository addressRepository;
    @Mock UserService userService;
    @Mock ViaCepService viaCepService;
    private AddressService service;

    @BeforeEach
    void setUp() {
        service = new AddressService(addressRepository, userService, viaCepService);
    }

    @Test
    void firstAddressIsAlwaysPrimary() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity("Ana", "52998224725", LocalDate.of(1990, 1, 1), "hash", UserRole.USER);
        AuthenticatedUser actor = new AuthenticatedUser(userId, "52998224725", "hash", "USER");
        when(userService.requireAccessible(userId, actor)).thenReturn(user);
        when(viaCepService.lookup("20040020"))
                .thenReturn(new PostalCodeResponse("20040020", "Rua da Assembleia", "Centro", "Rio de Janeiro", "RJ"));
        when(addressRepository.findAllForUpdate(userId)).thenReturn(List.of());
        when(addressRepository.save(any(AddressEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(userId, new UpsertAddressRequest("20040020", "10", null, false), actor);

        assertThat(result.primary()).isTrue();
        assertThat(result.city()).isEqualTo("Rio de Janeiro");
    }
}


package br.com.solution.addressbook.application;

import br.com.solution.addressbook.application.dto.AddressDtos.AddressResponse;
import br.com.solution.addressbook.application.dto.UserDtos.UserDetailsResponse;
import br.com.solution.addressbook.application.dto.UserSummary;
import br.com.solution.addressbook.domain.address.AddressEntity;
import br.com.solution.addressbook.domain.user.UserEntity;

public final class UserMapper {
    private UserMapper() {}

    public static UserSummary toSummary(UserEntity user) {
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getCpf(),
                user.getBirthDate(),
                user.getRole(),
                user.getProfilePhotoVersion(),
                user.getStatus());
    }

    public static UserDetailsResponse toDetails(UserEntity user) {
        return new UserDetailsResponse(
                user.getId(),
                user.getName(),
                user.getCpf(),
                user.getBirthDate(),
                user.getRole(),
                user.getProfilePhotoVersion(),
                user.getStatus(),
                user.getDeactivatedAt(),
                user.getAddresses().stream().map(UserMapper::toAddress).toList());
    }

    public static AddressResponse toAddress(AddressEntity address) {
        return new AddressResponse(
                address.getId(),
                address.getZipCode(),
                address.getNumber(),
                address.getComplement(),
                address.getStreet(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.isPrimary());
    }
}

package br.com.solution.addressbook.security;

import br.com.solution.addressbook.domain.user.UserEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AuthenticatedUser(
        UUID id,
        String cpf,
        String password,
        String role
) implements UserDetails {
    public static AuthenticatedUser from(UserEntity user) {
        return new AuthenticatedUser(user.getId(), user.getCpf(), user.getPasswordHash(), user.getRole().name());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() { return cpf; }

    @Override
    public String getPassword() { return password; }
}

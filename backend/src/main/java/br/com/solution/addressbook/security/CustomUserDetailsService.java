package br.com.solution.addressbook.security;

import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRepository;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        try {
            return userRepository
                    .findById(UUID.fromString(id))
                    .filter(UserEntity::isActive)
                    .map(AuthenticatedUser::from)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException("Invalid user id", exception);
        }
    }
}

package br.com.solution.addressbook.application;

import br.com.solution.addressbook.application.dto.AuthDtos.LoginRequest;
import br.com.solution.addressbook.application.dto.AuthDtos.LoginResponse;
import br.com.solution.addressbook.application.error.DomainException;
import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRepository;
import br.com.solution.addressbook.security.AuthenticatedUser;
import br.com.solution.addressbook.security.JwtService;
import br.com.solution.addressbook.shared.Cpf;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByCpf(Cpf.normalize(request.cpf()))
                .orElseThrow(AuthService::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) throw invalidCredentials();
        if (!user.isActive()) {
            throw new DomainException("ACCOUNT_INACTIVE",
                    "Conta desativada. Procure um administrador.");
        }
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        return new LoginResponse(jwtService.issue(principal), UserMapper.toSummary(user));
    }

    private static DomainException invalidCredentials() {
        return new DomainException("INVALID_CREDENTIALS", "CPF ou senha invalidos.");
    }
}

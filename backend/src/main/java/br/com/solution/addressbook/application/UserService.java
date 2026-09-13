package br.com.solution.addressbook.application;

import br.com.solution.addressbook.api.dto.UserDtos.CreateUserRequest;
import br.com.solution.addressbook.api.dto.UserDtos.UpdateUserRequest;
import br.com.solution.addressbook.api.dto.UserDtos.UpdateUserStatusRequest;
import br.com.solution.addressbook.api.dto.UserDtos.UserDetailsResponse;
import br.com.solution.addressbook.api.dto.UserSummary;
import br.com.solution.addressbook.api.error.DomainException;
import br.com.solution.addressbook.domain.user.UserEntity;
import br.com.solution.addressbook.domain.user.UserRepository;
import br.com.solution.addressbook.domain.user.UserRole;
import br.com.solution.addressbook.domain.user.UserStatus;
import br.com.solution.addressbook.security.AuthenticatedUser;
import br.com.solution.addressbook.shared.Cpf;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserSummary create(CreateUserRequest request) {
        String cpf = Cpf.normalize(request.cpf());
        if (!Cpf.isValid(cpf)) {
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_CPF", "CPF invalido.");
        }
        if (userRepository.existsByCpf(cpf)) {
            throw new DomainException(HttpStatus.CONFLICT, "CPF_ALREADY_EXISTS", "CPF ja cadastrado.");
        }
        UserEntity user = new UserEntity(request.name().trim(), cpf, request.birthDate(),
                passwordEncoder.encode(request.password()), request.role());
        return UserMapper.toSummary(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserSummary> list() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream().map(UserMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public UserDetailsResponse get(UUID requestedId, AuthenticatedUser actor) {
        assertCanAccess(actor, requestedId);
        return userRepository.findWithAddressesById(requestedId)
                .map(UserMapper::toDetails)
                .orElseThrow(UserService::notFound);
    }

    @Transactional(readOnly = true)
    public UserDetailsResponse me(AuthenticatedUser actor) {
        return get(actor.id(), actor);
    }

    @Transactional
    public UserDetailsResponse update(UUID requestedId, UpdateUserRequest request, AuthenticatedUser actor) {
        assertCanAccess(actor, requestedId);
        List<UserEntity> administrators = "ADMIN".equals(actor.role()) && request.role() != null
                ? userRepository.findAllByRoleForUpdate(UserRole.ADMIN) : List.of();
        UserEntity user = userRepository.findByIdForUpdate(requestedId).orElseThrow(UserService::notFound);
        assertActive(user);
        String cpf = Cpf.normalize(request.cpf());
        if (!Cpf.isValid(cpf)) {
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_CPF", "CPF invalido.");
        }
        if (!cpf.equals(user.getCpf()) && userRepository.existsByCpf(cpf)) {
            throw new DomainException(HttpStatus.CONFLICT, "CPF_ALREADY_EXISTS", "CPF ja cadastrado.");
        }

        boolean admin = "ADMIN".equals(actor.role());
        if (!admin && request.role() != null) {
            throw new DomainException(HttpStatus.FORBIDDEN, "ROLE_CHANGE_FORBIDDEN",
                    "Usuario comum nao pode alterar nivel de acesso.");
        }

        var role = admin && request.role() != null ? request.role() : user.getRole();
        if (user.getRole() == UserRole.ADMIN && role == UserRole.USER
                && activeAdministrators(administrators) <= 1) {
            throw new DomainException(HttpStatus.CONFLICT, "LAST_ACTIVE_ADMIN",
                    "O ultimo administrador ativo nao pode perder o nivel de acesso.");
        }
        user.updateProfile(request.name().trim(), cpf, request.birthDate(), role);
        return UserMapper.toDetails(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserDetailsResponse updateStatus(UUID requestedId, UpdateUserStatusRequest request,
                                            AuthenticatedUser actor) {
        if (request.status() == UserStatus.INACTIVE && actor.id().equals(requestedId)) {
            throw new DomainException(HttpStatus.CONFLICT, "SELF_DEACTIVATION_FORBIDDEN",
                    "Voce nao pode desativar a propria conta.");
        }

        List<UserEntity> administrators = userRepository.findAllByRoleForUpdate(UserRole.ADMIN);
        UserEntity user = userRepository.findByIdForUpdate(requestedId).orElseThrow(UserService::notFound);
        if (user.getStatus() == request.status()) return UserMapper.toDetails(user);

        if (request.status() == UserStatus.INACTIVE) {
            if (user.getRole() == UserRole.ADMIN && activeAdministrators(administrators) <= 1) {
                throw new DomainException(HttpStatus.CONFLICT, "LAST_ACTIVE_ADMIN",
                        "O ultimo administrador ativo nao pode ser desativado.");
            }
            user.deactivate(actor.id(), Instant.now());
        } else {
            user.reactivate();
        }
        return UserMapper.toDetails(user);
    }

    public UserEntity requireAccessible(UUID requestedId, AuthenticatedUser actor) {
        assertCanAccess(actor, requestedId);
        UserEntity user = userRepository.findByIdForUpdate(requestedId).orElseThrow(UserService::notFound);
        assertActive(user);
        return user;
    }

    @Transactional(readOnly = true)
    public void ensureAccessible(UUID requestedId, AuthenticatedUser actor) {
        assertCanAccess(actor, requestedId);
        if (!userRepository.existsById(requestedId)) throw notFound();
    }

    private void assertCanAccess(AuthenticatedUser actor, UUID requestedId) {
        if (!"ADMIN".equals(actor.role()) && !actor.id().equals(requestedId)) {
            throw new DomainException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "Voce nao pode acessar dados de outro usuario.");
        }
    }

    private static long activeAdministrators(List<UserEntity> administrators) {
        return administrators.stream().filter(UserEntity::isActive).count();
    }

    private static void assertActive(UserEntity user) {
        if (!user.isActive()) {
            throw new DomainException(HttpStatus.CONFLICT, "ACCOUNT_INACTIVE_READ_ONLY",
                    "Reative a conta antes de alterar seus dados.");
        }
    }

    private static DomainException notFound() {
        return new DomainException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario nao encontrado.");
    }
}

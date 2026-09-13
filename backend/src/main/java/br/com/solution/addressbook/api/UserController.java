package br.com.solution.addressbook.api;

import br.com.solution.addressbook.api.dto.UserDtos.CreateUserRequest;
import br.com.solution.addressbook.api.dto.UserDtos.UserDetailsResponse;
import br.com.solution.addressbook.api.dto.UserDtos.UpdateUserRequest;
import br.com.solution.addressbook.api.dto.UserDtos.UpdateUserStatusRequest;
import br.com.solution.addressbook.api.dto.UserSummary;
import br.com.solution.addressbook.application.UserService;
import br.com.solution.addressbook.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping
    ResponseEntity<UserSummary> create(@Valid @RequestBody CreateUserRequest request) {
        UserSummary created = userService.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(created);
    }

    @GetMapping
    ResponseEntity<List<UserSummary>> list() {
        return ResponseEntity.ok(userService.list());
    }

    @GetMapping("/me")
    ResponseEntity<UserDetailsResponse> me(@AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok(userService.me(actor));
    }

    @GetMapping("/{id}")
    ResponseEntity<UserDetailsResponse> get(@PathVariable UUID id,
                                            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok(userService.get(id, actor));
    }

    @PutMapping("/{id}")
    ResponseEntity<UserDetailsResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateUserRequest request,
                                               @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok(userService.update(id, request, actor));
    }

    @PatchMapping("/{id}/status")
    ResponseEntity<UserDetailsResponse> updateStatus(@PathVariable UUID id,
                                                     @Valid @RequestBody UpdateUserStatusRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok(userService.updateStatus(id, request, actor));
    }
}

package br.com.solution.addressbook.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET =
            "VGhpcy1pcy1hLXRlc3Qta2V5LXRoYXQtaXMtYXQtbGVhc3QtMzItYnl0ZXMtbG9uZw==";

    @Test
    void issuedTokenCanBeValidatedByConfiguredIssuer() {
        JwtService service =
                new JwtService(SECRET, Duration.ofHours(1), "solution-address-book-test");
        UUID id = UUID.randomUUID();

        String token = service.issue(new AuthenticatedUser(id, "52998224725", "hash", "ADMIN"));

        assertThat(service.subject(token)).isEqualTo(id.toString());
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        JwtService issuer = new JwtService(SECRET, Duration.ofHours(1), "another-system");
        JwtService verifier =
                new JwtService(SECRET, Duration.ofHours(1), "solution-address-book-test");
        String token =
                issuer.issue(
                        new AuthenticatedUser(UUID.randomUUID(), "52998224725", "hash", "ADMIN"));

        assertThatThrownBy(() -> verifier.subject(token)).isInstanceOf(JwtException.class);
    }
}

package dev.tasksys.config;

import dev.tasksys.model.User;
import io.jsonwebtoken.Claims;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set test values for JWT configuration
        ReflectionTestUtils.setField(jwtUtil, "secret", "myTestSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24 hours

        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
    }

    @Test
    void shouldGenerateValidJwtToken() {
        // When
        String token = jwtUtil.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    void shouldExtractExpirationFromToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertThat(expiration).isAfter(new Date());
        assertThat(expiration.getTime()).isCloseTo(System.currentTimeMillis() + 86400000L, Percentage.withPercentage(5000L) // Allow 5 second tolerance
        );
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isValid = jwtUtil.validateToken(token, testUser);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        // Given
        String token = jwtUtil.generateToken(testUser);
        User differentUser = new User("differentuser", "different@example.com", "password");

        // When
        Boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldValidateTokenWithoutUserDetails() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "malformed";

        // When
        Boolean isValid = jwtUtil.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldExtractClaimsFromToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String subject = jwtUtil.extractClaim(token, Claims::getSubject);
        Date issuedAt = jwtUtil.extractClaim(token, Claims::getIssuedAt);

        // Then
        assertThat(subject).isEqualTo("testuser");
        assertThat(issuedAt).isBefore(new Date());
        assertThat(issuedAt.getTime()).isCloseTo(System.currentTimeMillis(), Percentage.withPercentage(5000L) // Allow 5 second tolerance
        );
    }

    @Test
    void shouldHandleExpiredToken() throws InterruptedException {
        // Given - Create JwtUtil with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", "myTestSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", 1L); // 1 millisecond

        String token = shortExpirationJwtUtil.generateToken(testUser);

        // Wait for token to expire
        Thread.sleep(10);

        // When
        Boolean isValid = shortExpirationJwtUtil.validateToken(token, testUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldGenerateDifferentTokensForSameUser() {
        // Given
        String token1 = jwtUtil.generateToken(testUser);

        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtUtil.generateToken(testUser);

        // When & Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.extractUsername(token1)).isEqualTo(jwtUtil.extractUsername(token2));
        assertThat(jwtUtil.validateToken(token1, testUser)).isTrue();
        assertThat(jwtUtil.validateToken(token2, testUser)).isTrue();
    }
}
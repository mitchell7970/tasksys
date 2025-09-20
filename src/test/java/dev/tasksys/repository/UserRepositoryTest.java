package dev.tasksys.repository;

import dev.tasksys.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        // Given
        User user = new User("testuser", "test@example.com", "password");

        // When
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByUsername() {
        // Given
        User user = new User("findme", "findme@example.com", "password");
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByUsername("findme");
        Optional<User> notFound = userRepository.findByUsername("notfound");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("findme");
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = new User("emailuser", "findme@example.com", "password");
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByEmail("findme@example.com");
        Optional<User> notFound = userRepository.findByEmail("notfound@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("findme@example.com");
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldCheckIfUsernameExists() {
        // Given
        User user = new User("existinguser", "existing@example.com", "password");
        entityManager.persist(user);
        entityManager.flush();

        // When
        Boolean exists = userRepository.existsByUsername("existinguser");
        Boolean notExists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Given
        User user = new User("emailcheckuser", "existing@example.com", "password");
        entityManager.persist(user);
        entityManager.flush();

        // When
        Boolean exists = userRepository.existsByEmail("existing@example.com");
        Boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldEnforceUniqueUsername() {
        // Given
        User user1 = new User("uniqueuser", "user1@example.com", "password");
        User user2 = new User("uniqueuser", "user2@example.com", "password");

        // When & Then
        entityManager.persist(user1);
        entityManager.flush();

        // This should throw an exception due to unique constraint
        try {
            entityManager.persist(user2);
            entityManager.flush();
            assertThat(false).isTrue(); // Should not reach here
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    void shouldEnforceUniqueEmail() {
        // Given
        User user1 = new User("user1", "unique@example.com", "password");
        User user2 = new User("user2", "unique@example.com", "password");

        // When & Then
        entityManager.persist(user1);
        entityManager.flush();

        // This should throw an exception due to unique constraint
        try {
            entityManager.persist(user2);
            entityManager.flush();
            assertThat(false).isTrue(); // Should not reach here
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    void shouldSetCreatedAtAutomatically() {
        // Given
        User user = new User("timeuser", "time@example.com", "password");

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getEnabled()).isTrue();
    }
}
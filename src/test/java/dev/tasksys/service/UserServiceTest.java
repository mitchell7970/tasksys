package dev.tasksys.service;

import dev.tasksys.model.User;
import dev.tasksys.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
    }

    @Test
    void shouldLoadUserByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = userService.loadUserByUsername("testuser");

        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent")).isInstanceOf(UsernameNotFoundException.class).hasMessage("User not found: nonexistent");
    }

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser("newuser", "new@example.com", "password123");

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser("existinguser", "new@example.com", "password123")).isInstanceOf(IllegalArgumentException.class).hasMessage("Username already exists");

        verify(userRepository).existsByUsername("existinguser");
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser("newuser", "existing@example.com", "password123")).isInstanceOf(IllegalArgumentException.class).hasMessage("Email already exists");

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldFindUserByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findByUsername("testuser");

        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldThrowExceptionWhenFindingNonExistentUser() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findByUsername("nonexistent")).isInstanceOf(UsernameNotFoundException.class).hasMessage("User not found: nonexistent");
    }
}
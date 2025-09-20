package dev.tasksys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tasksys.config.JwtUtil;
import dev.tasksys.model.AuthDto;
import dev.tasksys.model.User;
import dev.tasksys.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private AuthDto.LoginRequest loginRequest;
    private AuthDto.RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);

        loginRequest = new AuthDto.LoginRequest("testuser", "password123");
        registerRequest = new AuthDto.RegisterRequest("testuser", "test@example.com", "password123");
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Given
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk()).andExpect(jsonPath("$.token").value("jwt-token")).andExpect(jsonPath("$.username").value("testuser")).andExpect(jsonPath("$.email").value("test@example.com")).andExpect(jsonPath("$.type").value("Bearer"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByUsername("testuser");
        verify(jwtUtil).generateToken(testUser);
    }

    @Test
    void shouldReturnBadRequestForInvalidCredentials() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid username or password"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userService);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        // Given
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().isOk()).andExpect(jsonPath("$.token").value("jwt-token")).andExpect(jsonPath("$.username").value("testuser")).andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).createUser("testuser", "test@example.com", "password123");
        verify(jwtUtil).generateToken(testUser);
    }

    @Test
    void shouldReturnBadRequestForExistingUsername() throws Exception {
        // Given
        when(userService.createUser(anyString(), anyString(), anyString())).thenThrow(new IllegalArgumentException("Username already exists"));

        // When & Then
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Username already exists"));

        verify(userService).createUser("testuser", "test@example.com", "password123");
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void shouldReturnBadRequestForExistingEmail() throws Exception {
        // Given
        when(userService.createUser(anyString(), anyString(), anyString())).thenThrow(new IllegalArgumentException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void shouldReturnBadRequestForInvalidLoginData() throws Exception {
        // Given
        AuthDto.LoginRequest invalidRequest = new AuthDto.LoginRequest("", "");

        // When & Then
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.username").exists()).andExpect(jsonPath("$.password").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidRegisterData() throws Exception {
        // Given
        AuthDto.RegisterRequest invalidRequest = new AuthDto.RegisterRequest("", "invalid-email", "123");

        // When & Then
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.username").exists()).andExpect(jsonPath("$.email").exists()).andExpect(jsonPath("$.password").exists());
    }
}
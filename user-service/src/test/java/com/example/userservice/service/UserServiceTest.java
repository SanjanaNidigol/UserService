package com.example.userservice.service;

import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .username("alice123")
                .firstName("Alice")
                .lastName("Johnson")
                .dob(LocalDate.of(1990, 1, 1))
                .email("alice@example.com")
                .mobile("9999999999")
                .passwordHash(BCrypt.hashpw("SecurePass123!", BCrypt.gensalt()))
                .mpin("1234")
                .failedMpinAttempts(0)
                .accountStatus(User.AccountStatus.ACTIVE)
                .role(User.Role.USER)
                .build();
    }

    // --- Register Test ---
    @Test
    void testRegisterUser_Success() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("bob123");
        request.setFirstName("Bob");
        request.setLastName("Marley");
        request.setDob(LocalDate.of(1992, 5, 12));
        request.setEmail("bob@example.com");
        request.setMobile("8888888888");
        request.setPassword("Pass@123");
        request.setMpin("5678");
        request.setAddress("Street 1");
        request.setState("State");
        request.setPincode("123456");
        request.setCountry("USA");
        request.setGender("Male");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        User savedUser = userService.register(request);

        assertNotNull(savedUser.getId());
        assertEquals("bob123", savedUser.getUsername());
        verify(kafkaTemplate).send(eq("user-events"), contains("USER_REGISTERED"));
    }

    // --- Get by ID Test ---
    @Test
    void testGetUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertEquals("alice123", result.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getUserById(99L));
        assertTrue(ex.getMessage().contains("User not found"));
    }

    // --- Get All Users ---
    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("alice123", users.get(0).getUsername());
    }

    // --- Login Tests ---
    @Test
    void testLogin_Success() {
        when(userRepository.findByUsername("alice123")).thenReturn(Optional.of(user));

        User loggedInUser = userService.login("alice123", "SecurePass123!");

        assertEquals("alice123", loggedInUser.getUsername());
    }

    @Test
    void testLogin_InvalidPassword() {
        when(userRepository.findByUsername("alice123")).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login("alice123", "WrongPass"));
        assertTrue(ex.getMessage().contains("Invalid password"));
    }

    @Test
    void testLogin_Deactivated() {
        user.setAccountStatus(User.AccountStatus.DEACTIVATED);
        when(userRepository.findByUsername("alice123")).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login("alice123", "SecurePass123!"));
        assertTrue(ex.getMessage().contains("deactivated"));
    }

    @Test
    void testLogin_LockedAccount() {
        user.setAccountStatus(User.AccountStatus.LOCKED);
        user.setAccountLockedUntil(LocalDateTime.now().plusHours(2));
        when(userRepository.findByUsername("alice123")).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login("alice123", "SecurePass123!"));
        assertTrue(ex.getMessage().contains("locked"));
    }

    // --- Login with MPIN ---
    @Test
    void testLoginWithMpin_Success() {
        when(userRepository.findByMobile("9999999999")).thenReturn(Optional.of(user));

        User result = userService.loginWithMpin("9999999999", "1234");

        assertEquals("alice123", result.getUsername());
        assertEquals(0, result.getFailedMpinAttempts());
    }

    @Test
    void testLoginWithMpin_InvalidMpin() {
        when(userRepository.findByMobile("9999999999")).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.loginWithMpin("9999999999", "0000"));
        assertTrue(ex.getMessage().contains("Invalid MPIN"));
    }

    @Test
    void testLoginWithMpin_ThreeFailedAttemptsLocksAccount() {
        when(userRepository.findByMobile("9999999999")).thenReturn(Optional.of(user));

        for (int i = 0; i < 3; i++) {
            try {
                userService.loginWithMpin("9999999999", "0000");
            } catch (RuntimeException ignored) {}
        }

        assertEquals(User.AccountStatus.LOCKED, user.getAccountStatus());
        assertNotNull(user.getAccountLockedUntil());
    }

    // --- Deactivate Account ---
    @Test
    void testDeactivateAccount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deactivateAccount(1L);

        assertEquals(User.AccountStatus.DEACTIVATED, user.getAccountStatus());
        verify(userRepository).save(user);
    }
}

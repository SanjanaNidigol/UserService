package com.example.userservice.service;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate; // Kafka integration

    // Register a new user and publish event to Kafka
    public User register(UserRegistrationRequest request) {

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .pan(request.getPan())
                .mobile(request.getMobile())
                .email(request.getEmail())
                .passwordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                .mpin(request.getMpin())
                .failedMpinAttempts(0)
                .address(request.getAddress())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .gender(request.getGender())
                .accountStatus(User.AccountStatus.ACTIVE)
                .role(User.Role.USER)
                .build();

        User savedUser = userRepository.save(user);
//        kafkaTemplate.send("user-events", "USER_REGISTERED:" + savedUser.getId());
//        return savedUser;
        String kafkaMessage = "USER_REGISTERED:" + savedUser.getId() + ":" + savedUser.getEmail();
        kafkaTemplate.send("user-events", kafkaMessage);
        return savedUser;
    }

    // Get user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    // Get user by mobile
    public User getUserByMobile(String mobile) {
        return userRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("User not found with mobile: " + mobile));
    }

    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        userRepository.delete(user);
    }

    // Get all users by their account status
    public List<User> getUsersByAccountStatus(User.AccountStatus status) {
        return userRepository.findByAccountStatus(status);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check account lock first
        if (user.getAccountStatus() == User.AccountStatus.LOCKED
                && user.getAccountLockedUntil() != null
                && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account is locked until " + user.getAccountLockedUntil());
        }

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.getAccountStatus() == User.AccountStatus.DEACTIVATED) {
            throw new RuntimeException("Account is deactivated. Contact support.");
        }

        if (user.getAccountStatus() != User.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        return user; // login successful
    }


    public User loginWithMpin(String identifier, String mpin) {
        User user = userRepository.findByMobile(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAccountStatus() == User.AccountStatus.DEACTIVATED) {
            throw new RuntimeException("Account is deactivated. Contact support.");
        }

        // Check if account is locked
        if (user.getAccountStatus() == User.AccountStatus.LOCKED
                && user.getAccountLockedUntil() != null
                && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account is locked until " + user.getAccountLockedUntil());
        }

        if (!user.getMpin().equals(mpin)) {
            int attempts = user.getFailedMpinAttempts() + 1;
            user.setFailedMpinAttempts(attempts);

            if (attempts >= 3) {
                user.setAccountStatus(User.AccountStatus.LOCKED);
                user.setAccountLockedUntil(LocalDateTime.now().plusDays(1));
                user.setFailedMpinAttempts(0);
            }

            userRepository.save(user);
            throw new RuntimeException("Invalid MPIN. Attempt " + attempts + " of 3.");
        }

        // Successful login
        user.setFailedMpinAttempts(0);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        return user;
    }


    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(User.AccountStatus.DEACTIVATED);
        userRepository.save(user);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password matches
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        if (BCrypt.checkpw(newPassword, user.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as the old password");
        }

        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPasswordHash(newHashedPassword);

        userRepository.save(user);
    }

    public void resetMpin(Long userId, String newMpin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!newMpin.matches("(\\d{4}|\\d{6})")) {
            throw new RuntimeException("MPIN must be exactly 4 or 6 digits");
        }

        if (newMpin.equals(user.getMpin())) {
            throw new RuntimeException("New MPIN cannot be the same as the old MPIN");
        }

        user.setMpin(newMpin);
        user.setFailedMpinAttempts(0);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setAccountLockedUntil(null);

        userRepository.save(user);
    }
}

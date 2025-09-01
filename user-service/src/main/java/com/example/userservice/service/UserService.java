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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate; // Kafka integration

    // Register a new user and publish event to Kafka
    public User register(UserRegistrationRequest request) {
//        if (!request.getPassword().equals(request.getConfirmPassword())) {
//            throw new IllegalArgumentException("Passwords do not match");
//        }

//        User user = User.builder()
//                .firstName(request.getFirstName())
//                .lastName(request.getLastName())
//                .dob(request.getDob())
//                .pan(request.getPan())
//                .mobile(request.getMobile())
//                .email(request.getEmail())
//                .passwordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
//                .mpin(request.getMpin())
//                .failedMpinAttempts(0)
//                .build();

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

        // ✅ Publish event to Kafka
//        kafkaTemplate.send("user-events", "USER_REGISTERED:" + savedUser.getId());
//
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



}


//package com.example.userservice.service;
//
//import com.example.userservice.dto.UserRegistrationRequest;
//import com.example.userservice.entity.PasswordHistory;
//import com.example.userservice.entity.User;
//import com.example.userservice.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.security.crypto.bcrypt.BCrypt;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.util.List;
//
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//
//    private final UserRepository userRepository;
//
//    public User register(UserRegistrationRequest request) {
//        if (!request.getPassword().equals(request.getConfirmPassword())) {
//            throw new IllegalArgumentException("Passwords do not match");
//        }
//
//        User user = User.builder()
//                .firstName(request.getFirstName())
//                .lastName(request.getLastName())
//                .dob(request.getDob())
//                .pan(request.getPan())
//                .mobile(request.getMobile())
//                .email(request.getEmail())
//                .passwordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
//                .mpin(request.getMpin())
//                .failedMpinAttempts(0)
//                .build();
//
//        return userRepository.save(user);
//    }
//
//    // ✅ Get user by ID
//    public User getUserById(Long id) {
//        return userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
//    }
//
//    // ✅ Get all users
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//
//    // ✅ Get user by email (optional, useful for login)
//    public User getUserByEmail(String email) {
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
//    }
//
//    public User getUserByMobile(String mobile) {
//        return userRepository.findByMobile(mobile)
//                .orElseThrow(() -> new RuntimeException("User not found with mobile: " + mobile));
//    }
//}
//
//
////package com.example.userservice.service;
////
////import com.example.userservice.entity.PasswordHistory;
////import com.example.userservice.entity.User;
////import com.example.userservice.repository.UserRepository;
////import lombok.RequiredArgsConstructor;
////import org.springframework.kafka.core.KafkaTemplate;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////import java.time.Duration;
////import java.time.Instant;
////
////@Service
////@RequiredArgsConstructor
////public class UserService {
////    private final UserRepository repo;
////    private final KafkaTemplate<String, String> kafka;
////
////    private static final int MAX_ATTEMPTS = 3;
////    private static final Duration LOCK_DURATION = Duration.ofHours(24);
////
////    @Transactional
////    public User register(String email, String mpin) {
////        User u = User.builder().email(email).mpin(mpin).build();
////        u = repo.save(u);
////        kafka.send("user-events", "USER_REGISTERED:" + u.getId());
////        return u;
////    }
////
////    @Transactional
////    public boolean verifyMpin(Long userId, String mpin) {
////        User u = repo.findById(userId).orElseThrow();
////        Instant now = Instant.now();
////
////        if (u.getMpinLockedUntil() != null && u.getMpinLockedUntil().isAfter(now)) {
////            kafka.send("user-events", "USER_LOCKED:" + u.getId());
////            return false;
////        }
////
////        if (u.getMpin().equals(mpin)) {
////            u.setFailedMpinAttempts(0);
////            u.setMpinLockedUntil(null);
////            repo.save(u);
////            kafka.send("user-events", "MPIN_OK:" + u.getId());
////            return true;
////        } else {
////            int attempts = u.getFailedMpinAttempts() + 1;
////            u.setFailedMpinAttempts(attempts);
////            if (attempts >= MAX_ATTEMPTS) {
////                u.setMpinLockedUntil(now.plus(LOCK_DURATION));
////                kafka.send("user-events", "MPIN_LOCKED_24H:" + u.getId());
////            } else {
////                kafka.send("user-events", "MPIN_FAIL:" + u.getId() + ":attempt=" + attempts);
////            }
////            repo.save(u);
////            return false;
////        }
////    }
////
////    @Transactional
////    public void changePassword(Long userId, String newPass) {
////        User u = repo.findById(userId).orElseThrow();
////        PasswordHistory ph = PasswordHistory.builder()
////                .user(u)
////                .password(newPass)
////                .changedAt(Instant.now())
////                .build();
////        u.getPasswordHistory().add(ph);
////        // simplistic: also set mpin as "password" for demo parity or manage separately
////        u.setMpin(newPass);
////        repo.save(u);
////        kafka.send("user-events", "PASSWORD_CHANGED:" + u.getId());
////    }
////}

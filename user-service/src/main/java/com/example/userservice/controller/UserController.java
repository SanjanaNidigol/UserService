package com.example.userservice.controller;

import com.example.userservice.dto.UserLoginRequest;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    //  Register a user
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUserById(id));
    }

    //  Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.getUserByEmail(email));
    }


    @GetMapping("/mobile/{mobile}")
    public ResponseEntity<User> getUserByMobile(@PathVariable String mobile) {
        return ResponseEntity.ok(service.getUserByMobile(mobile));
    }

    // Get users by account status
//    @GetMapping("/status/{status}")
//    public ResponseEntity<List<User>> getUsersByAccountStatus(@PathVariable User.AccountStatus status) {
//        return ResponseEntity.ok(service.getUsersByAccountStatus(status));
//    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody UserLoginRequest request) {
        User user = service.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login/mpin")
    public ResponseEntity<User> loginWithMpin(@RequestBody Map<String, String> request) {
        String identifier = request.get("mobile/email"); // mobile or email
        String mpin = request.get("mpin");
        return ResponseEntity.ok(service.loginWithMpin(identifier, mpin));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        service.deactivateAccount(id);
        return ResponseEntity.ok("User account deactivated successfully.");
    }

    // GET /api/users/status/ACTIVE
    @GetMapping("/status/{status}")
    public ResponseEntity<List<User>> getUsersByAccountStatus(@PathVariable String status) {
        User.AccountStatus accountStatus;
        try {
            accountStatus = User.AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        List<User> users = service.getUsersByAccountStatus(accountStatus);
        return ResponseEntity.ok(users);
    }

}


//package com.example.userservice.controller;
//
//import com.example.userservice.entity.User;
//import com.example.userservice.service.UserService;
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/users")
//@RequiredArgsConstructor
//public class UserController {
//
//    private final UserService service;
//
//    @PostMapping("/register")
//    public ResponseEntity<User> register(@RequestBody RegisterRequest req) {
//        return ResponseEntity.ok(service.register(req.getEmail(), req.getMpin()));
//    }
//
//    @PostMapping("/{id}/verify-mpin")
//    public ResponseEntity<Boolean> verify(@PathVariable Long id, @RequestBody VerifyRequest req) {
//        return ResponseEntity.ok(service.verifyMpin(id, req.getMpin()));
//    }
//
//    @PostMapping("/{id}/change-password")
//    public ResponseEntity<Void> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest req) {
//        service.changePassword(id, req.getNewPassword());
//        return ResponseEntity.noContent().build();
//    }
//
//    @Data
//    public static class RegisterRequest { @Email String email; @NotBlank String mpin; }
//    @Data
//    public static class VerifyRequest { @NotBlank String mpin; }
//    @Data
//    public static class ChangePasswordRequest { @NotBlank String newPassword; }
//}

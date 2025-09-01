package com.example.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private LocalDate dob;

    @Column(unique = true, nullable = false)
    private String pan;

//    @Column(unique = true, nullable = false)
//    private String mobile;

    @Column(name = "mobile_number", nullable = false, length = 10)
    private String mobile;


    @Column(unique = true, nullable = false)
    private String email;

    /** Raw password (not persisted) */
    @JsonIgnore
    @Transient
    private String password;

    /** Stored hashed password */
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String mpin;

    private int failedMpinAttempts;

    private LocalDateTime accountLockedUntil;
}


//package com.example.userservice.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "users")
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//public class User {
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true)
//    private String email;
//
//    @Column(nullable = false)
//    private String mpin;
//
//    private int failedMpinAttempts = 0;
//
//    private Instant mpinLockedUntil; // if not null and in future -> locked
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<PasswordHistory> passwordHistory = new ArrayList<>();
//}

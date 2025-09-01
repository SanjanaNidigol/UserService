package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String passwordHash;

    private LocalDateTime changedAt;
}



//package com.example.userservice.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.Instant;
//
//@Entity
//@Table(name = "password_history")
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//public class PasswordHistory {
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @Column(nullable = false)
//    private String password;
//
//    @Column(nullable = false)
//    private Instant changedAt;
//}

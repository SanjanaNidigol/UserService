package com.example.userservice.repository;

import com.example.userservice.entity.User;
import com.example.userservice.entity.User.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);
    List<User> findByAccountStatus(AccountStatus status);
    Optional<User> findByUsername(String username);
}


//package com.example.userservice.repository;
//
//import com.example.userservice.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface UserRepository extends JpaRepository<User, Long> {
//    Optional<User> findByEmail(String email);
//}

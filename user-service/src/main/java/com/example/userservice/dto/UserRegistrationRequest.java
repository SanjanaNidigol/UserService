package com.example.userservice.dto;
import com.example.userservice.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserRegistrationRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private LocalDate dob;

    @NotBlank
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format")
    private String pan;

    @NotBlank
    @Size(min = 10, max = 10, message = "Mobile number must be 10 digits")
    private String mobile;

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    @NotBlank
    @Pattern(regexp = "(\\d{4}|\\d{6})", message = "MPIN must be exactly 4 or 6 digits")
    private String mpin;

    private String address;

    private String state;

    @Size(max = 10)
    private String pincode;

    private String country;

    private String gender;

//    @NotNull
//    private User.Role role;
}

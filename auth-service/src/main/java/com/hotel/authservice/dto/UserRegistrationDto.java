package com.hotel.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {

    @NotBlank(message = "Please enter username")
    private String username;

    @NotBlank(message = "Please enter password")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String confirmPassword;

    boolean passwordMatching;

    @NotBlank(message = "Please enter your first name")
    private String firstName;

    @NotBlank(message = "Please enter your last name")
    private String lastName;

    @NotBlank(message = "Please enter your email address")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Please enter your phone number")
    private String phoneNumber;

    public static UserRegistrationDto empty() {
        return new UserRegistrationDto();
    }
}

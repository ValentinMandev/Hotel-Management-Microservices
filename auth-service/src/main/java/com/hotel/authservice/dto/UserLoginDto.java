package com.hotel.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDto {

    @NotBlank(message = "Please enter your username")
    private String username;

    @NotBlank(message = "Please enter your password")
    private String password;
}

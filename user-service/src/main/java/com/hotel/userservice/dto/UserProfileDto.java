package com.hotel.userservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDto {

    @NotEmpty(message = "Please enter your email address")
    private String email;

    @NotEmpty(message = "Please enter your first name")
    private String firstName;

    @NotEmpty(message = "Please enter your last name")
    private String lastName;

    @NotEmpty(message = "Please enter your phone number")
    private String phoneNumber;

}

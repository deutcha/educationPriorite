package com.devpro.devlearningroadmapmanager.securities.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class UserDto implements Serializable {
    UUID id;
    String email;
    String username;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String role;

    @Data
    public static class UserSaveDto implements Serializable {
        @NotNull
        @Email
        @Size(max = 255)
        String email;

        @NotNull
        @Size(min = 3, max = 50)
        String username;

        String passwordHash;

        String role;
    }

    @Data
    public static class LogingUserDto implements Serializable {
        @NotNull
        String email;
        String password;

    }
}
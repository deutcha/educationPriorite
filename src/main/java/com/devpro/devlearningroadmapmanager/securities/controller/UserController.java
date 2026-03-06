package com.devpro.devlearningroadmapmanager.securities.controller;

import com.devpro.devlearningroadmapmanager.securities.dtos.UserDto;
import com.devpro.devlearningroadmapmanager.securities.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final IUserService userService;

    // <editor-fold defaultstate="collapsed" desc="USERS">

    @Operation(summary = "Lister les utilisateurs")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(required = false) String email
    ) {
        List<UserDto> result = userService.getAllUsers(email);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Récupérer un utilisateur par ID")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Modifier un utilisateur")
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID userId,
            @RequestBody @Valid UserDto.UserSaveDto userSaveDto
    ) {
        UserDto updated = userService.saveUser(userId, userSaveDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // </editor-fold>
}

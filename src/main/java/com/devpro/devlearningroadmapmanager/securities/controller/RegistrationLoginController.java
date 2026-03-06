package com.devpro.devlearningroadmapmanager.securities.controller;

import com.devpro.devlearningroadmapmanager.securities.dtos.UserDto;
import com.devpro.devlearningroadmapmanager.securities.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationLoginController {


    private final IUserService userService;


    // ==============================
    // ACTIONS (POST)
    // ==============================

    /**
     * Traitement de l'inscription
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto.UserSaveDto user) {

        return ResponseEntity.ok(userService.saveUser(null, user));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDto.LogingUserDto request) {

        return new ResponseEntity<>(userService.loginUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/expired/{token}")
    public ResponseEntity<Boolean> tokenExpired(@PathVariable String token) {
        return ResponseEntity.ok(userService.tokenIsExpired(token));
    }

    /**
     * NOUVEAU - Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        // Récupérer le token depuis le header Authorization
        String token = request.getHeader("Authorization");

        Map<String, Object> response = userService.logoutUser(token);

        if (Boolean.TRUE.equals(response.get("success"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).body(response);
        }
    }

}
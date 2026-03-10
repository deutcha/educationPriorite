package com.devpro.devlearningroadmapmanager.securities.service;

import com.devpro.devlearningroadmapmanager.securities.dtos.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface IUserService {

    Page<UserDto> getAllUsers(
            String search,
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            Pageable pageable
    );

    UserDto getUserById(UUID userId);

    UserDto saveUser(UUID userId, UserDto.UserSaveDto userSaveDto);

    void deleteUser(UUID userId);

   Map<String, Object> loginUser(UserDto.LogingUserDto request);

   Map<String, Object> logoutUser(String token);

  boolean tokenIsExpired(String token);

}
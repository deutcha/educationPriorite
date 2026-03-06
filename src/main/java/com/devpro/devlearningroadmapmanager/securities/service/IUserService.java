package com.devpro.devlearningroadmapmanager.securities.service;

import com.devpro.devlearningroadmapmanager.securities.dtos.UserDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IUserService {

    List<UserDto> getAllUsers(
            String email
    );

    UserDto getUserById(UUID userId);

    UserDto saveUser(UUID userId, UserDto.UserSaveDto userSaveDto);

    void deleteUser(UUID userId);

   Map<String, Object> loginUser(UserDto.LogingUserDto request);

   Map<String, Object> logoutUser(String token);

  boolean tokenIsExpired(String token);

}
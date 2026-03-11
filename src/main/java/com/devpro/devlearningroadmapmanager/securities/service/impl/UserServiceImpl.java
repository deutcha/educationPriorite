package com.devpro.devlearningroadmapmanager.securities.service.impl;


import com.devpro.devlearningroadmapmanager.securities.configuration.JwtUtils;
import com.devpro.devlearningroadmapmanager.securities.dtos.UserDto;
import com.devpro.devlearningroadmapmanager.securities.entities.User;
import com.devpro.devlearningroadmapmanager.securities.mappers.UserMapper;
import com.devpro.devlearningroadmapmanager.securities.repositories.UserRepository;
import com.devpro.devlearningroadmapmanager.securities.service.IUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {

    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    @Override
    public Page<UserDto> getAllUsers(String search,
                                     LocalDateTime dateDebut,
                                     LocalDateTime dateFin,
                                     Pageable pageable) {
        Specification<User> specification = ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate predicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern)
                );
                predicates.add(predicate);
            }

            if (dateDebut != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt").as(LocalDateTime.class), dateDebut));
            }

            if (dateFin != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt").as(LocalDateTime.class), dateFin));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        Page<User> users = userRepository.findAll(specification, pageable);

        return users.map(userMapper::toDto);

    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User non trouvé avec l'ID: " + userId));

        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public UserDto saveUser(UUID userId, UserDto.UserSaveDto userSaveDto) {

        User user;

        if (userId == null) {

            if (userRepository.findByEmail(userSaveDto.getEmail()) != null) {
                throw new RuntimeException("email already exists");
            }

            if (userRepository.findByUsername(userSaveDto.getUsername()) != null) {
                throw new RuntimeException("username already exists");
            }

            user = userMapper.toEntity(userSaveDto);


        } else {

            user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "User non trouvé"
                    ));

            if (!user.getEmail().equals(userSaveDto.getEmail()) && userRepository.findByEmail(userSaveDto.getEmail()) != null) {
                throw new RuntimeException("email already exists");
            }

            if (!user.getUsername().equals(userSaveDto.getUsername()) && userRepository.findByUsername(userSaveDto.getUsername()) != null) {
                throw new RuntimeException("username already exists");
            }

            userMapper.partialUpdate(userSaveDto, user);
        }

        user.setRole(userSaveDto.getRole());
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        if (userSaveDto.getPasswordHash() != null && !userSaveDto.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userSaveDto.getPasswordHash()));
        }

        User saved = userRepository.save(user);

        return userMapper.toDto(saved);
    }

    @Transactional
    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User non trouvé avec l'ID: " + userId
                ));

        userRepository.delete(user);
    }

    @Transactional
    @Override
    public Map<String, Object> loginUser(UserDto.LogingUserDto request){
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {

                User user = userRepository.findByEmail(request.getEmail());

                Map<String, Object> claims = new HashMap<>();
                claims.put("role", user.getRole());
                claims.put("userId", user.getId().toString());
                claims.put("email", user.getEmail());

                String token = jwtUtils.generateToken(claims, request.getEmail());



                response.put("success", true);
                response.put("message", "Authentification réussie");
                response.put("token", token);
                response.put("username", request.getEmail());

                return response;
            }

        } catch (Exception e) {
            throw new RuntimeException("username or password invalid");
        }

        return  response;
    }

    @Transactional
    @Override
    public Map<String, Object> logoutUser(String token) {
        Map<String, Object> response = new HashMap<>();

        try {
           String username = jwtUtils.logout(token);

           if (username.equals("Token manquant")) {
               return errorResponse(username);
           }

            response.put("success", true);
            response.put("message", "Déconnecté avec succès");
            response.put("username", username);

            return response;

        } catch (Exception e) {
            return errorResponse("Erreur: " + e.getMessage());
        }
    }

    @Override
    public boolean tokenIsExpired(String token) {
        return jwtUtils.isTokenExpired(token);
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

}
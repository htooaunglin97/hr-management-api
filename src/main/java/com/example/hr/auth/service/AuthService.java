package com.example.hr.auth.service;

import com.example.hr.auth.dto.AuthResponse;
import com.example.hr.auth.dto.LoginRequest;
import com.example.hr.auth.dto.RegisterRequest;
import com.example.hr.auth.security.JwtService;
import com.example.hr.shared.exception.ResourceNotFoundException;
import com.example.hr.users.entity.Role;
import com.example.hr.users.entity.RoleEnum;
import com.example.hr.users.entity.User;
import com.example.hr.users.repository.RoleRepository;
import com.example.hr.users.repository.UserRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email().toLowerCase())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role defaultRole = roleRepository.findByName(RoleEnum.ROLE_EMPLOYEE)
                .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_EMPLOYEE not found. Seed roles first."));

        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email().toLowerCase());
        user.setPassword_hash(passwordEncoder.encode(req.password()));
        user.getRoles().add(defaultRole);

        userRepository.save(user);

        Set<String> roles = user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());
        String token = jwtService.generateAccessToken(user.getEmail(), roles);

        return new AuthResponse(token, "Bearer", jwtService.accessTokenSeconds(), user.getEmail(), roles);
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email().toLowerCase(), req.password())
        );

        String email = auth.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<String> roles = user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());
        String token = jwtService.generateAccessToken(email, roles);

        return new AuthResponse(token, "Bearer", jwtService.accessTokenSeconds(), email, roles);
    }
}

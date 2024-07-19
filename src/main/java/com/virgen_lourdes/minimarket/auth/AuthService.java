package com.virgen_lourdes.minimarket.auth;

import com.virgen_lourdes.minimarket.entity.User;
import com.virgen_lourdes.minimarket.entity.enums.Role;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.NotFoundException;
import com.virgen_lourdes.minimarket.jwt.JwtService;
import com.virgen_lourdes.minimarket.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    IUserRepository userRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        try {
            User user = User.builder()
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .role(Role.USER)
                    .isActive(true)
                    .build();
            userRepository.save(user);

            String token = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .accessToken(token)
                    .user(user)
                    .build();

        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

            if (!user.getIsActive()) {
                throw new LockedException("Cuenta bloqueada, contacta con soporte");
            }

            String token = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .accessToken(token)
                    .user(user)
                    .build();
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (LockedException e) {
            throw new LockedException(e.getMessage());
        }
    }
}

package com.virgen_lourdes.minimarket.auth;

import com.virgen_lourdes.minimarket.dto.responseDto.UserResponseDto;
import com.virgen_lourdes.minimarket.entity.User;
import com.virgen_lourdes.minimarket.entity.enums.Role;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.AuthenticationFailedException;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.NotFoundException;
import com.virgen_lourdes.minimarket.jwt.JwtService;
import com.virgen_lourdes.minimarket.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    IUserRepository userRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Transactional(rollbackFor = Exception.class)
    public AuthResponse register(RegisterRequest request) {
        try {
            User user = User.builder()
                    .name(request.getName())
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.EMPLOYEE)
                    .isActive(true)
                    .build();
            userRepository.save(user);

            String token = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .accessToken(token)
                    .user(UserResponseDto.of(user))
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Ese usuario ya existe. Por favor, elige otro nombre de usuario.");
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                    .user(UserResponseDto.of(user))
                    .build();
        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Nombre de usuario o contraseña incorrectos");
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (LockedException e) {
            throw new AuthenticationFailedException(e.getMessage());
        }
    }
}

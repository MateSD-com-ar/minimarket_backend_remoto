package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.UserDto;
import com.virgen_lourdes.minimarket.dto.responseDto.UserResponseDto;
import com.virgen_lourdes.minimarket.entity.User;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.NotFoundException;
import com.virgen_lourdes.minimarket.repository.IUserRepository;
import com.virgen_lourdes.minimarket.service.ICrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Service
public class UserService implements ICrudService<UserDto, UserResponseDto, Long>, UserDetailsService {

    @Autowired
    IUserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto create(UserDto userDto) {
        return null;
    }

    @Override
    public List<UserResponseDto> read(UserDto userDto) {
        try {
            return filterUsers(userDto, userRepository.findAll())
                    .stream()
                    .map(UserResponseDto::of)
                    .toList();
        } catch (MethodArgumentTypeMismatchException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public UserResponseDto update(UserDto userDto, Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            if (userDto.getName() != null) {
                user.setName(userDto.getName());
            }
            if (userDto.getUsername() != null) {
                if (userRepository.existsByUsername(userDto.getUsername())) {
                    throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario, intente con otro");
                }
                user.setUsername(userDto.getUsername());
            }
            if (userDto.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            user = userRepository.save(user);
            return UserResponseDto.of(user);
        } catch (MethodArgumentTypeMismatchException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            userRepository.delete(user);
        } catch (MethodArgumentTypeMismatchException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @Override
    public void deactivateUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            user.setIsActive(false);
            userRepository.save(user);
        } catch (MethodArgumentTypeMismatchException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return new org.springframework.security.core.userdetails.User(
                userDetails.getUsername(),
                "",
                userDetails.getAuthorities()
        );

    }

    List<User> filterUsers(UserDto userDto, List<User> users) {
        List<User> filteredUsers = users.stream()
                .filter(user -> userDto.getId() == null ||
                        user.getId().equals(userDto.getId()))
                .filter(user -> userDto.getName() == null ||
                        user.getName().toLowerCase().contains(userDto.getName().toLowerCase()))
                .filter(user -> userDto.getUsername() == null ||
                        user.getUsername().toLowerCase().contains((userDto.getUsername().toLowerCase())))
                .filter(user -> userDto.getRole() == null ||
                        user.getRole().toString().equalsIgnoreCase(userDto.getRole()))
                .filter(user -> userDto.getIsActive() == null ||
                        user.getIsActive().equals(userDto.getIsActive()))
                .toList();

        if (filteredUsers.isEmpty()) {
            throw new NotFoundException("No se encontraron usuarios");
        }
        return filteredUsers;
    }

}

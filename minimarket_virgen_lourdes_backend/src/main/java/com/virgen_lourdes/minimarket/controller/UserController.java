package com.virgen_lourdes.minimarket.controller;

import com.virgen_lourdes.minimarket.dto.UserDto;
import com.virgen_lourdes.minimarket.dto.responseDto.UserResponseDto;
import com.virgen_lourdes.minimarket.service.ICrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private ICrudService<UserDto, UserResponseDto, Long> userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(@ModelAttribute UserDto userDto) {
        return ResponseEntity.ok(userService.read(userDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.update(userDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    @DeleteMapping("/deactivate/{id}")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("Usuario desactivado correctamente");
    }
}

package com.virgen_lourdes.minimarket.dto.responseDto;

import com.virgen_lourdes.minimarket.entity.User;
import lombok.Data;

@Data
public class UserResponseDto {

    private Long id;
    private String name;
    private String username;
    private String role;
    private Boolean isActive;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.role = user.getRole().name();
        this.isActive = user.getIsActive();
    }

    public static UserResponseDto of(User user) {
        return new UserResponseDto(user);
    }

}

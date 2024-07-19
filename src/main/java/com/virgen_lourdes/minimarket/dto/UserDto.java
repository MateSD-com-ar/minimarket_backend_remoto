package com.virgen_lourdes.minimarket.dto;

import com.virgen_lourdes.minimarket.entity.User;
import lombok.Data;

@Data
public class UserDto {

    public Long id;
    public String username;
    public String password;
    public String role;
    public Boolean isActive;

    public UserDto(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole().name();
        this.isActive = user.getIsActive();
    }

    public static UserDto of(User user) {
        return new UserDto(user);
    }

}

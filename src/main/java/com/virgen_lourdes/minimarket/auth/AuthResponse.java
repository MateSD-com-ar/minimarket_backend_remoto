package com.virgen_lourdes.minimarket.auth;

import com.virgen_lourdes.minimarket.dto.responseDto.UserResponseDto;
import com.virgen_lourdes.minimarket.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String accessToken;
    private UserResponseDto user;

}

package com.virgen_lourdes.minimarket.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * This class represents a request for registering a new user.
 * It contains no additional fields or methods beyond the base class.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es requerido")
    private String username;

    @NotBlank(message = "La contraseña es requerida")
    private String password;
}

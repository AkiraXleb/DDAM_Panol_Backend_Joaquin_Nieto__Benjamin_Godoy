package com.mpaz.panol.usuarios.entities;

import lombok.Data;

// Cuerpo de POST /api/usuarios/login
@Data
public class LoginRequest {
    private String email;
    private String password;
}

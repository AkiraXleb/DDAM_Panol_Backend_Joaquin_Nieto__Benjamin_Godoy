package com.mpaz.panol.usuarios.entities;

// Lo que se envía a la app: el usuario sin su contraseña
public record UsuarioRespuesta(Long id, String nombre, String email, String cargo, String departamento) {

    public static UsuarioRespuesta de(Usuario u) {
        return new UsuarioRespuesta(u.getId(), u.getNombre(), u.getEmail(), u.getCargo(), u.getDepartamento());
    }
}

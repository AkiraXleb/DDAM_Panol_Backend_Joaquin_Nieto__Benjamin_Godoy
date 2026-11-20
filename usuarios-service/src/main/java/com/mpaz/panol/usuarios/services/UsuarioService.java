package com.mpaz.panol.usuarios.services;

import java.util.List;

import com.mpaz.panol.usuarios.entities.Usuario;

public interface UsuarioService {

    List<Usuario> obtenerTodos();

    Usuario crear(Usuario usuario);

    void eliminar(Long id);

    Usuario login(String email, String password);

}

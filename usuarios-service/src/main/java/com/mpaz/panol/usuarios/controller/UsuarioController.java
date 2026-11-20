package com.mpaz.panol.usuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mpaz.panol.usuarios.entities.LoginRequest;
import com.mpaz.panol.usuarios.entities.Usuario;
import com.mpaz.panol.usuarios.entities.UsuarioRespuesta;
import com.mpaz.panol.usuarios.services.UsuarioService;

@RestController
@RequestMapping("api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioRespuesta> listarUsuarios() {
        return usuarioService.obtenerTodos().stream().map(UsuarioRespuesta::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespuesta crearUsuario(@RequestBody Usuario usuario) {
        return UsuarioRespuesta.de(usuarioService.crear(usuario));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }

    @PostMapping("/login")
    public UsuarioRespuesta login(@RequestBody LoginRequest datos) {
        return UsuarioRespuesta.de(usuarioService.login(datos.getEmail(), datos.getPassword()));
    }

}

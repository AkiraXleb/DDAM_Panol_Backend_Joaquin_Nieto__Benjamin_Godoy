package com.mpaz.panol.usuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mpaz.panol.usuarios.entities.Usuario;
import com.mpaz.panol.usuarios.repository.UsuarioRepository;
import com.mpaz.panol.usuarios.services.UsuarioServiceImpl;

// Pruebas unitarias del servicio de usuarios (sin base de datos: el repositorio es un mock)
class UsuarioServiceImplTest {

    private UsuarioRepository repo;
    private UsuarioServiceImpl service;

    @BeforeEach
    void preparar() {
        repo = mock(UsuarioRepository.class);
        service = new UsuarioServiceImpl(repo);
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario usuario(String email) {
        return new Usuario("Ana Pérez", email, "clave123", null, "Ciencias");
    }

    private HttpStatus estadoDe(ResponseStatusException e) {
        return HttpStatus.valueOf(e.getStatusCode().value());
    }

    @Test
    void elPrefijoDelCorreoDefineElCargo() {
        assertEquals("Docente", service.crear(usuario("prof.ana@escuelamarcelapaz.cl")).getCargo());
        assertEquals("Encargado TI", service.crear(usuario("ti.ana@escuelamarcelapaz.cl")).getCargo());
        assertEquals("Inspector", service.crear(usuario("insp.ana@escuelamarcelapaz.cl")).getCargo());
        assertEquals("Super Admin", service.crear(usuario("admin.ana@escuelamarcelapaz.cl")).getCargo());
    }

    @Test
    void elCorreoSeGuardaEnMinusculas() {
        Usuario creado = service.crear(usuario("  PROF.Ana@EscuelaMarcelaPaz.cl "));
        assertEquals("prof.ana@escuelamarcelapaz.cl", creado.getEmail());
    }

    @Test
    void correoSinPrefijoValidoLanzaBadRequest() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.crear(usuario("ana@escuelamarcelapaz.cl")));
        assertEquals(HttpStatus.BAD_REQUEST, estadoDe(e));
        verify(repo, never()).save(any(Usuario.class));
    }

    @Test
    void correoConDominioDistintoLanzaBadRequest() {
        assertThrows(ResponseStatusException.class, () -> service.crear(usuario("prof.ana@gmail.com")));
    }

    @Test
    void correoConDobleArrobaLanzaBadRequest() {
        assertThrows(ResponseStatusException.class,
                () -> service.crear(usuario("ti.roberto@@escuelamarcelapaz.cl")));
    }

    @Test
    void correoRepetidoLanzaConflict() {
        when(repo.existsByEmail("prof.ana@escuelamarcelapaz.cl")).thenReturn(true);
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.crear(usuario("prof.ana@escuelamarcelapaz.cl")));
        assertEquals(HttpStatus.CONFLICT, estadoDe(e));
    }

    @Test
    void contrasenaCortaLanzaBadRequest() {
        Usuario corta = new Usuario("Ana Pérez", "prof.ana@escuelamarcelapaz.cl", "123", null, "Ciencias");
        assertThrows(ResponseStatusException.class, () -> service.crear(corta));
    }

    @Test
    void noSePuedeEliminarUnSuperAdmin() {
        Usuario admin = new Usuario("Admin", "admin.panol@escuelamarcelapaz.cl", "clave123", "Super Admin", "Dirección");
        when(repo.findById(4L)).thenReturn(Optional.of(admin));
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.eliminar(4L));
        assertEquals(HttpStatus.CONFLICT, estadoDe(e));
        verify(repo, never()).delete(any(Usuario.class));
    }

    @Test
    void eliminarUnDocenteLoBorra() {
        Usuario docente = new Usuario("Ana", "prof.ana@escuelamarcelapaz.cl", "clave123", "Docente", "Ciencias");
        when(repo.findById(1L)).thenReturn(Optional.of(docente));
        service.eliminar(1L);
        verify(repo).delete(docente);
    }

    @Test
    void loginCorrectoDevuelveElUsuario() {
        Usuario docente = new Usuario("Ana", "prof.ana@escuelamarcelapaz.cl", "clave123", "Docente", "Ciencias");
        when(repo.findByEmail("prof.ana@escuelamarcelapaz.cl")).thenReturn(Optional.of(docente));
        assertEquals("Ana", service.login("prof.ana@escuelamarcelapaz.cl", "clave123").getNombre());
    }

    @Test
    void loginConContrasenaIncorrectaLanzaUnauthorized() {
        Usuario docente = new Usuario("Ana", "prof.ana@escuelamarcelapaz.cl", "clave123", "Docente", "Ciencias");
        when(repo.findByEmail("prof.ana@escuelamarcelapaz.cl")).thenReturn(Optional.of(docente));
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.login("prof.ana@escuelamarcelapaz.cl", "otra"));
        assertEquals(HttpStatus.UNAUTHORIZED, estadoDe(e));
    }

    @Test
    void loginConCorreoInexistenteLanzaUnauthorized() {
        when(repo.findByEmail("prof.nadie@escuelamarcelapaz.cl")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.login("prof.nadie@escuelamarcelapaz.cl", "x"));
    }
}

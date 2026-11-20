package com.mpaz.panol.usuarios.services;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mpaz.panol.usuarios.entities.Usuario;
import com.mpaz.panol.usuarios.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    public static final String CARGO_SUPER_ADMIN = "Super Admin";

    // El prefijo del correo define el cargo del usuario
    private static final Map<String, String> CARGO_POR_PREFIJO = Map.of(
            "prof", "Docente",
            "insp", "Inspector",
            "ti", "Encargado TI",
            "admin", CARGO_SUPER_ADMIN);

    // Formato exacto: prefijo.nombre@escuelamarcelapaz.cl (minúsculas, sin espacios)
    private static final Pattern FORMATO_CORREO =
            Pattern.compile("^(prof|insp|ti|admin)\\.[a-z0-9]+@escuelamarcelapaz\\.cl$");

    private static final int LARGO_MINIMO_PASSWORD = 6;

    private final UsuarioRepository usuarioRepo;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepo.findAllByOrderById();
    }

    @Override
    public Usuario crear(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (usuario.getDepartamento() == null || usuario.getDepartamento().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El departamento es obligatorio");
        }
        if (usuario.getPassword() == null || usuario.getPassword().length() < LARGO_MINIMO_PASSWORD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres");
        }
        String email = usuario.getEmail() == null ? "" : usuario.getEmail().trim().toLowerCase();
        if (!FORMATO_CORREO.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Correo inválido. Usa prefijo.nombre@escuelamarcelapaz.cl con prefijo prof, insp, ti o admin");
        }
        if (usuarioRepo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }
        usuario.setId(null);
        usuario.setEmail(email);
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setDepartamento(usuario.getDepartamento().trim());
        // El cargo no lo elige quien crea el usuario: sale del prefijo del correo
        usuario.setCargo(CARGO_POR_PREFIJO.get(email.substring(0, email.indexOf('.'))));
        return usuarioRepo.save(usuario);
    }

    @Override
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (CARGO_SUPER_ADMIN.equals(usuario.getCargo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar a un Super Admin");
        }
        usuarioRepo.delete(usuario);
    }

    @Override
    public Usuario login(String email, String password) {
        String correo = email == null ? "" : email.trim().toLowerCase();
        return usuarioRepo.findByEmail(correo)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "La contraseña o correo son incorrectos"));
    }
}

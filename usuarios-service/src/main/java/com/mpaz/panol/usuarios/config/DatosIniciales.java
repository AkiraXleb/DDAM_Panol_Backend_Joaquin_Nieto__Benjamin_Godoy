package com.mpaz.panol.usuarios.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mpaz.panol.usuarios.entities.Usuario;
import com.mpaz.panol.usuarios.repository.UsuarioRepository;

// Carga los usuarios de prueba solo si la tabla está vacía (el orden fija los ids 1 a 4)
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;

    public DatosIniciales(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepo.count() > 0) {
            return;
        }
        usuarioRepo.save(new Usuario("Patricia González", "prof.patricia@escuelamarcelapaz.cl", "docente123",
                "Docente", "Departamento de Ciencias"));
        usuarioRepo.save(new Usuario("Marcelo Rojas", "insp.marcelo@escuelamarcelapaz.cl", "inspector123",
                "Inspector", "Inspectoría General"));
        usuarioRepo.save(new Usuario("Roberto Díaz", "ti.roberto@escuelamarcelapaz.cl", "admin123",
                "Encargado TI", "Pañol Central"));
        usuarioRepo.save(new Usuario("Administrador del Sistema", "admin.panol@escuelamarcelapaz.cl", "superadmin123",
                "Super Admin", "Dirección"));
    }
}

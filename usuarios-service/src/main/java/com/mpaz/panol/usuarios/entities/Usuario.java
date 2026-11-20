package com.mpaz.panol.usuarios.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    // Único: el correo identifica al usuario y su prefijo define el cargo
    @Column(unique = true)
    private String email;
    private String password;
    // Se calcula con el prefijo del correo: prof., insp., ti. o admin.
    private String cargo;
    private String departamento;

    public Usuario() {
    }

    public Usuario(String nombre, String email, String password, String cargo, String departamento) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.cargo = cargo;
        this.departamento = departamento;
    }
}

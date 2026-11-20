package com.mpaz.panol.equipos.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String nombre;
    private String categoria;
    private String marca;
    private String modelo;
    private String descripcion;
    private String ubicacion;
    private Integer stockTotal;
    private Integer stockDisponible;
    // Unidades que están en mantenimiento (no se pueden prestar hasta que el Encargado TI las devuelva)
    @Column(columnDefinition = "integer default 0")
    private Integer stockMantenimiento = 0;
    // Disponible, Reservado, Prestado o Mantenimiento
    private String estado;

    public Equipo() {
    }

    // Constructor completo, igual a los datos que muestra la app
    public Equipo(String codigo, String nombre, String marca, String modelo, String categoria, String descripcion,
            Integer stockTotal, Integer stockDisponible, String estado, String ubicacion) {
        this(codigo, nombre, categoria, descripcion, stockTotal, stockDisponible, estado);
        this.marca = marca;
        this.modelo = modelo;
        this.ubicacion = ubicacion;
    }

    public Equipo(String codigo, String nombre, String categoria, String descripcion, Integer stockTotal,
            Integer stockDisponible, String estado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.stockTotal = stockTotal;
        this.stockDisponible = stockDisponible;
        this.estado = estado;
    }
}

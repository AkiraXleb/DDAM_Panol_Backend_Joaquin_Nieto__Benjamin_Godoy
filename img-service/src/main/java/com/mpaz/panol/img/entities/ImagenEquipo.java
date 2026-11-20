package com.mpaz.panol.img.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Una imagen por equipo: el id es el mismo id del equipo (equipos-service)
@Entity
@Table(name = "imagenes_equipo")
public class ImagenEquipo {

    @Id
    private Long equipoId;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private byte[] datos;

    public ImagenEquipo() {
    }

    public ImagenEquipo(Long equipoId, String tipo, byte[] datos) {
        this.equipoId = equipoId;
        this.tipo = tipo;
        this.datos = datos;
    }

    public Long getEquipoId() { return equipoId; }
    public void setEquipoId(Long equipoId) { this.equipoId = equipoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public byte[] getDatos() { return datos; }
    public void setDatos(byte[] datos) { this.datos = datos; }
}

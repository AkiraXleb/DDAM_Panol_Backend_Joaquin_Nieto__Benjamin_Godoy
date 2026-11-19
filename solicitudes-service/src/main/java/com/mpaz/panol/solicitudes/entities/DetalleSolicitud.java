package com.mpaz.panol.solicitudes.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

// Un equipo pedido dentro de una solicitud, con su cantidad
@Data
@Entity
@Table(name = "detalle_solicitud")
public class DetalleSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long equipoId;
    private String equipoNombre;
    private String categoria;
    private Integer cantidad;

    public DetalleSolicitud() {
    }

    public DetalleSolicitud(Long equipoId, String equipoNombre, Integer cantidad) {
        this.equipoId = equipoId;
        this.equipoNombre = equipoNombre;
        this.cantidad = cantidad;
    }

    public DetalleSolicitud(Long equipoId, String equipoNombre, String categoria, Integer cantidad) {
        this(equipoId, equipoNombre, cantidad);
        this.categoria = categoria;
    }
}

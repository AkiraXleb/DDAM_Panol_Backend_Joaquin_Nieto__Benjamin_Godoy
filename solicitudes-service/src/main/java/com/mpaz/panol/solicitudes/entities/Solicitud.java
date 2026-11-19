package com.mpaz.panol.solicitudes.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String sala;
    // Fechas en milisegundos desde 1970 (igual que en la app)
    private Long fechaInicio;
    private Long fechaDevolucion;
    private String asignatura;
    private String observaciones;
    private String estado;
    // Comentario del encargado (obligatorio al rechazar, opcional en los demás cambios)
    private String comentarioGestion;
    private Long fechaCreacion;
    private Long fechaEntrega;
    private Long fechaRecepcion;
    // false hasta que la app avisa al docente del resultado (vibración y linterna)
    private Boolean notificada;

    // Una solicitud tiene varios detalles (uno por equipo)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitud_id")
    private List<DetalleSolicitud> detalles = new ArrayList<>();

    public Solicitud() {
    }
}

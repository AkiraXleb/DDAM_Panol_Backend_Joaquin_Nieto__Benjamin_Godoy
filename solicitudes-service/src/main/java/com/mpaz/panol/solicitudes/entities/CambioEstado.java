package com.mpaz.panol.solicitudes.entities;

import lombok.Data;

// Cuerpo de la petición PATCH /api/solicitudes/{id}/estado
@Data
public class CambioEstado {

    private String estado;
    // Obligatorio solo cuando el estado nuevo es Rechazada
    private String motivo;

    public CambioEstado() {
    }

    public CambioEstado(String estado, String motivo) {
        this.estado = estado;
        this.motivo = motivo;
    }
}

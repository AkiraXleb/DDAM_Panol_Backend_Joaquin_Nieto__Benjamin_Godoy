package com.mpaz.panol.solicitudes.entities;

// Estados posibles de una solicitud (mismos textos que usa la app Android)
public final class EstadoSolicitud {

    public static final String PENDIENTE = "Pendiente";
    public static final String ACEPTADA = "Aceptada";
    public static final String RECHAZADA = "Rechazada";
    public static final String CANCELADA = "Cancelada";
    public static final String ENTREGADA = "Entregada";
    public static final String DEVUELTA = "Devuelta";

    private EstadoSolicitud() {
    }
}

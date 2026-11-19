package com.mpaz.panol.solicitudes.services;

import java.util.List;
import java.util.Optional;

import com.mpaz.panol.solicitudes.entities.Solicitud;

public interface SolicitudService {

    Solicitud crear(Solicitud solicitud);

    List<Solicitud> obtener(Long usuarioId, String estado);

    Optional<Solicitud> obtenerPorId(Long id);

    Solicitud cambiarEstado(Long id, String nuevoEstado, String motivo);

    Solicitud marcarNotificada(Long id);

}

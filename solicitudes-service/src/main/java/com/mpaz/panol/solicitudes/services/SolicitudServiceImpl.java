package com.mpaz.panol.solicitudes.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mpaz.panol.solicitudes.client.EquipoClient;
import com.mpaz.panol.solicitudes.entities.DetalleSolicitud;
import com.mpaz.panol.solicitudes.entities.EstadoSolicitud;
import com.mpaz.panol.solicitudes.entities.Solicitud;
import com.mpaz.panol.solicitudes.repository.SolicitudRepository;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    // Regla del caso: la solicitud debe hacerse con al menos 3 horas de anticipación
    static final int HORAS_ANTICIPACION = 3;

    private final SolicitudRepository solicitudRepo;
    private final EquipoClient equipoClient;
    private final Clock clock;

    public SolicitudServiceImpl(SolicitudRepository solicitudRepo, EquipoClient equipoClient, Clock clock) {
        this.solicitudRepo = solicitudRepo;
        this.equipoClient = equipoClient;
        this.clock = clock;
    }

    @Override
    public Solicitud crear(Solicitud solicitud) {
        validar(solicitud);
        long ahora = clock.millis();
        solicitud.setId(null);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaCreacion(ahora);
        solicitud.setNotificada(false);
        solicitud.setComentarioGestion(null);
        return solicitudRepo.save(solicitud);
    }

    @Override
    public List<Solicitud> obtener(Long usuarioId, String estado) {
        boolean hayEstado = estado != null && !estado.isBlank();
        if (usuarioId != null) {
            List<Solicitud> delUsuario = solicitudRepo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
            if (!hayEstado) {
                return delUsuario;
            }
            return delUsuario.stream().filter(s -> estado.equals(s.getEstado())).toList();
        }
        if (hayEstado) {
            return solicitudRepo.findByEstadoOrderByFechaCreacionDesc(estado);
        }
        return solicitudRepo.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    public Optional<Solicitud> obtenerPorId(Long id) {
        return solicitudRepo.findById(id);
    }

    @Override
    public Solicitud cambiarEstado(Long id, String nuevoEstado, String motivo) {
        Solicitud solicitud = solicitudRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        String actual = solicitud.getEstado();

        if (!transicionValida(actual, nuevoEstado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede pasar de " + actual + " a " + nuevoEstado);
        }
        if (EstadoSolicitud.RECHAZADA.equals(nuevoEstado) && (motivo == null || motivo.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo del rechazo es obligatorio");
        }

        // Al aceptar se descuenta el stock; al devolver se repone (llamada al microservicio de equipos)
        if (EstadoSolicitud.ACEPTADA.equals(nuevoEstado)) {
            for (DetalleSolicitud d : solicitud.getDetalles()) {
                equipoClient.ajustarStock(d.getEquipoId(), -d.getCantidad());
            }
        }
        if (EstadoSolicitud.DEVUELTA.equals(nuevoEstado)) {
            for (DetalleSolicitud d : solicitud.getDetalles()) {
                equipoClient.ajustarStock(d.getEquipoId(), d.getCantidad());
            }
        }

        solicitud.setEstado(nuevoEstado);
        if (motivo != null && !motivo.isBlank()) {
            solicitud.setComentarioGestion(motivo.trim());
        }
        if (EstadoSolicitud.ENTREGADA.equals(nuevoEstado)) {
            solicitud.setFechaEntrega(clock.millis());
        }
        if (EstadoSolicitud.DEVUELTA.equals(nuevoEstado)) {
            solicitud.setFechaRecepcion(clock.millis());
        }
        // Una nueva respuesta del encargado vuelve a quedar sin avisar al docente
        if (EstadoSolicitud.ACEPTADA.equals(nuevoEstado) || EstadoSolicitud.RECHAZADA.equals(nuevoEstado)) {
            solicitud.setNotificada(false);
        }
        return solicitudRepo.save(solicitud);
    }

    @Override
    public Solicitud marcarNotificada(Long id) {
        Solicitud solicitud = solicitudRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        solicitud.setNotificada(true);
        return solicitudRepo.save(solicitud);
    }

    // Flujo permitido: Pendiente -> Aceptada/Rechazada/Cancelada, Aceptada -> Entregada, Entregada -> Devuelta
    private boolean transicionValida(String actual, String nuevo) {
        if (EstadoSolicitud.PENDIENTE.equals(actual)) {
            return EstadoSolicitud.ACEPTADA.equals(nuevo) || EstadoSolicitud.RECHAZADA.equals(nuevo)
                    || EstadoSolicitud.CANCELADA.equals(nuevo);
        }
        if (EstadoSolicitud.ACEPTADA.equals(actual)) {
            return EstadoSolicitud.ENTREGADA.equals(nuevo);
        }
        if (EstadoSolicitud.ENTREGADA.equals(actual)) {
            return EstadoSolicitud.DEVUELTA.equals(nuevo);
        }
        return false;
    }

    private void validar(Solicitud s) {
        if (s.getUsuarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario es obligatorio");
        }
        if (s.getSala() == null || s.getSala().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sala es obligatoria");
        }
        if (s.getFechaInicio() == null || s.getFechaDevolucion() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las fechas de inicio y devolución son obligatorias");
        }
        if (s.getFechaDevolucion() <= s.getFechaInicio()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La devolución debe ser posterior al inicio");
        }
        if (s.getObservaciones() != null && s.getObservaciones().length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las observaciones admiten máximo 200 caracteres");
        }
        if (s.getDetalles() == null || s.getDetalles().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud debe tener al menos un equipo");
        }
        for (DetalleSolicitud d : s.getDetalles()) {
            if (d.getEquipoId() == null || d.getCantidad() == null || d.getCantidad() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada equipo necesita una cantidad de al menos 1");
            }
        }
        // REGLA CRÍTICA DEL CASO: inicio >= ahora + 3 horas
        long minimo = clock.millis() + HORAS_ANTICIPACION * 60L * 60L * 1000L;
        if (s.getFechaInicio() < minimo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La solicitud debe hacerse con al menos " + HORAS_ANTICIPACION + " horas de anticipación");
        }
    }
}

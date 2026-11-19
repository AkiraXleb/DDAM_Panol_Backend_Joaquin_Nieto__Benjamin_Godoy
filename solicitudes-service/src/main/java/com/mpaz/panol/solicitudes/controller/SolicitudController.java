package com.mpaz.panol.solicitudes.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mpaz.panol.solicitudes.entities.CambioEstado;
import com.mpaz.panol.solicitudes.entities.Solicitud;
import com.mpaz.panol.solicitudes.services.SolicitudService;

@RestController
@RequestMapping("api/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    // Docente: ?usuarioId=1   Encargado TI: sin filtro o ?estado=Pendiente
    @GetMapping
    public List<Solicitud> listarSolicitudes(@RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String estado) {
        return solicitudService.obtener(usuarioId, estado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtenerPorId(@PathVariable Long id) {
        return solicitudService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Solicitud guardarSolicitud(@RequestBody Solicitud solicitud) {
        return solicitudService.crear(solicitud);
    }

    @PatchMapping("/{id}/estado")
    public Solicitud cambiarEstado(@PathVariable Long id, @RequestBody CambioEstado cambio) {
        return solicitudService.cambiarEstado(id, cambio.getEstado(), cambio.getMotivo());
    }

    @PatchMapping("/{id}/notificada")
    public Solicitud marcarNotificada(@PathVariable Long id) {
        return solicitudService.marcarNotificada(id);
    }

}

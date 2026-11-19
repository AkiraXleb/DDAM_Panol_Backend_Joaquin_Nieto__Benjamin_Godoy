package com.mpaz.panol.solicitudes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mpaz.panol.solicitudes.entities.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findAllByOrderByFechaCreacionDesc();

    List<Solicitud> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Solicitud> findByEstadoOrderByFechaCreacionDesc(String estado);

}

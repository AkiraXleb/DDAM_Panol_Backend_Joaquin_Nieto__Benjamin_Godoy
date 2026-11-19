package com.mpaz.panol.equipos.services;

import java.util.List;
import java.util.Optional;

import com.mpaz.panol.equipos.entities.Equipo;

public interface EquipoService {

    List<Equipo> obtenerTodos(String categoria);

    Optional<Equipo> obtenerPorId(Long id);

    Equipo crear(Equipo equipo);

    Equipo actualizar(Long id, Equipo datos);

    Equipo ajustarStock(Long id, int cantidad);

}

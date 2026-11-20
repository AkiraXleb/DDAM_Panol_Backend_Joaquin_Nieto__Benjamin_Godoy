package com.mpaz.panol.equipos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mpaz.panol.equipos.entities.Equipo;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByCategoriaOrderById(String categoria);

    List<Equipo> findAllByOrderById();

}

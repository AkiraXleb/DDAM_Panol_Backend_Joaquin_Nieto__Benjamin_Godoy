package com.mpaz.panol.equipos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mpaz.panol.equipos.entities.AjusteStock;
import com.mpaz.panol.equipos.entities.Equipo;
import com.mpaz.panol.equipos.services.EquipoService;

@RestController
@RequestMapping("api/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping
    public List<Equipo> listarEquipos(@RequestParam(required = false) String categoria) {
        return equipoService.obtenerTodos(categoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipo> obtenerPorId(@PathVariable Long id) {
        return equipoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipo guardarEquipo(@RequestBody Equipo equipo) {
        return equipoService.crear(equipo);
    }

    @PutMapping("/{id}")
    public Equipo actualizarEquipo(@PathVariable Long id, @RequestBody Equipo equipo) {
        return equipoService.actualizar(id, equipo);
    }

    @PatchMapping("/{id}/stock")
    public Equipo ajustarStock(@PathVariable Long id, @RequestBody AjusteStock ajuste) {
        return equipoService.ajustarStock(id, ajuste.getCantidad());
    }

}

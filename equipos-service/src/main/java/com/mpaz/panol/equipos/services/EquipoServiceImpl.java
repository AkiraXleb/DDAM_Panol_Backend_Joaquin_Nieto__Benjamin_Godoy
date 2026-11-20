package com.mpaz.panol.equipos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mpaz.panol.equipos.entities.Equipo;
import com.mpaz.panol.equipos.repository.EquipoRepository;

@Service
public class EquipoServiceImpl implements EquipoService {

    private final EquipoRepository equipoRepo;

    public EquipoServiceImpl(EquipoRepository equipoRepo) {
        this.equipoRepo = equipoRepo;
    }

    @Override
    public List<Equipo> obtenerTodos(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return equipoRepo.findAllByOrderById();
        }
        return equipoRepo.findByCategoriaOrderById(categoria);
    }

    @Override
    public Optional<Equipo> obtenerPorId(Long id) {
        return equipoRepo.findById(id);
    }

    @Override
    public Equipo crear(Equipo equipo) {
        validar(equipo);
        equipo.setId(null);
        // Un equipo nuevo parte con todo su stock disponible
        if (equipo.getStockDisponible() == null) {
            equipo.setStockDisponible(equipo.getStockTotal());
        }
        if (equipo.getEstado() == null || equipo.getEstado().isBlank()) {
            equipo.setEstado("Disponible");
        }
        return equipoRepo.save(equipo);
    }

    @Override
    public Equipo actualizar(Long id, Equipo datos) {
        Equipo existente = equipoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
        validar(datos);
        existente.setCodigo(datos.getCodigo());
        existente.setNombre(datos.getNombre());
        existente.setCategoria(datos.getCategoria());
        existente.setMarca(datos.getMarca());
        existente.setModelo(datos.getModelo());
        existente.setDescripcion(datos.getDescripcion());
        existente.setUbicacion(datos.getUbicacion());
        existente.setStockTotal(datos.getStockTotal());
        if (datos.getEstado() != null && !datos.getEstado().isBlank()) {
            existente.setEstado(datos.getEstado());
        }
        return equipoRepo.save(existente);
    }

    @Override
    public Equipo ajustarStock(Long id, int cantidad) {
        Equipo equipo = equipoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
        int nuevoStock = equipo.getStockDisponible() + cantidad;
        if (nuevoStock < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para " + equipo.getNombre());
        }
        if (nuevoStock > equipo.getStockTotal() - enMantenimiento(equipo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El stock disponible no puede superar el total");
        }
        equipo.setStockDisponible(nuevoStock);
        return equipoRepo.save(equipo);
    }

    @Override
    public Equipo ajustarMantenimiento(Long id, int cantidad) {
        Equipo equipo = equipoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
        if (cantidad == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad no puede ser 0");
        }
        int mantenimiento = enMantenimiento(equipo);
        if (cantidad > 0 && cantidad > equipo.getStockDisponible()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo hay " + equipo.getStockDisponible() + " unidades disponibles de " + equipo.getNombre());
        }
        if (cantidad < 0 && -cantidad > mantenimiento) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo hay " + mantenimiento + " unidades en mantenimiento de " + equipo.getNombre());
        }
        equipo.setStockDisponible(equipo.getStockDisponible() - cantidad);
        equipo.setStockMantenimiento(mantenimiento + cantidad);
        // Si no queda nada disponible y hay unidades en mantenimiento, el equipo figura En Mantenimiento
        if (equipo.getStockMantenimiento() > 0 && equipo.getStockDisponible() == 0) {
            equipo.setEstado("En Mantenimiento");
        } else {
            equipo.setEstado("Disponible");
        }
        return equipoRepo.save(equipo);
    }

    // Los equipos creados antes de agregar esta columna tienen null: se toma como 0
    private int enMantenimiento(Equipo equipo) {
        return equipo.getStockMantenimiento() == null ? 0 : equipo.getStockMantenimiento();
    }

    @Override
    public void eliminar(Long id) {
        Equipo equipo = equipoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
        // Si hay unidades prestadas o reservadas todavía no se puede quitar del inventario
        if (equipo.getStockDisponible() < equipo.getStockTotal()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar " + equipo.getNombre() + ": tiene unidades prestadas, reservadas o en mantenimiento");
        }
        equipoRepo.delete(equipo);
    }

    private void validar(Equipo equipo) {
        if (equipo.getNombre() == null || equipo.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (equipo.getCodigo() == null || equipo.getCodigo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código es obligatorio");
        }
        if (equipo.getStockTotal() == null || equipo.getStockTotal() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock total no puede ser negativo");
        }
    }
}

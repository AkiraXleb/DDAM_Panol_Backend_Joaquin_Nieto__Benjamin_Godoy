package com.mpaz.panol.equipos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.mpaz.panol.equipos.entities.Equipo;
import com.mpaz.panol.equipos.repository.EquipoRepository;
import com.mpaz.panol.equipos.services.EquipoServiceImpl;

// Pruebas unitarias del servicio de equipos (sin base de datos: el repositorio es un mock)
class EquipoServiceImplTest {

    private EquipoRepository repo;
    private EquipoServiceImpl service;

    @BeforeEach
    void preparar() {
        repo = mock(EquipoRepository.class);
        service = new EquipoServiceImpl(repo);
        when(repo.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Equipo equipo(int total, int disponible) {
        return new Equipo("NB-001", "Notebook", "Computación", "desc", total, disponible, "Disponible");
    }

    @Test
    void crearEquipoDejaTodoElStockDisponible() {
        Equipo nuevo = new Equipo("NB-009", "Notebook", "Computación", "desc", 5, null, null);
        Equipo creado = service.crear(nuevo);
        assertEquals(5, creado.getStockDisponible());
        assertEquals("Disponible", creado.getEstado());
    }

    @Test
    void crearSinNombreLanzaBadRequest() {
        Equipo sinNombre = new Equipo("NB-009", " ", "Computación", "desc", 5, 5, "Disponible");
        assertThrows(ResponseStatusException.class, () -> service.crear(sinNombre));
        verify(repo, never()).save(any(Equipo.class));
    }

    @Test
    void crearConStockNegativoLanzaBadRequest() {
        Equipo negativo = new Equipo("NB-009", "Notebook", "Computación", "desc", -1, 0, "Disponible");
        assertThrows(ResponseStatusException.class, () -> service.crear(negativo));
    }

    @Test
    void descontarStockReduceElDisponible() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipo(5, 5)));
        Equipo resultado = service.ajustarStock(1L, -2);
        assertEquals(3, resultado.getStockDisponible());
    }

    @Test
    void descontarMasQueElStockLanzaConflicto() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipo(5, 1)));
        assertThrows(ResponseStatusException.class, () -> service.ajustarStock(1L, -2));
    }

    @Test
    void reponerNoPuedeSuperarElStockTotal() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipo(5, 5)));
        assertThrows(ResponseStatusException.class, () -> service.ajustarStock(1L, 1));
    }

    @Test
    void ajustarStockDeEquipoInexistenteLanzaNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.ajustarStock(99L, -1));
    }

    @Test
    void eliminarEquipoSinUnidadesPrestadasLoBorra() {
        Equipo existente = equipo(5, 5);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        service.eliminar(1L);
        verify(repo).delete(existente);
    }

    @Test
    void eliminarEquipoConUnidadesPrestadasLanzaConflicto() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipo(5, 3)));
        assertThrows(ResponseStatusException.class, () -> service.eliminar(1L));
        verify(repo, never()).delete(any(Equipo.class));
    }

    @Test
    void eliminarEquipoInexistenteLanzaNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.eliminar(99L));
    }

    @Test
    void enviarAMantenimientoDescuentaDelDisponible() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipo(5, 5)));
        Equipo resultado = service.ajustarMantenimiento(1L, 2);
        assertEquals(3, resultado.getStockDisponible());
        assertEquals(2, resultado.getStockMantenimiento());
        assertEquals("Disponible", resultado.getEstado());
    }

    @Test
    void siTodoQuedaEnMantenimientoElEstadoCambia() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipo(2, 2)));
        Equipo resultado = service.ajustarMantenimiento(1L, 2);
        assertEquals(0, resultado.getStockDisponible());
        assertEquals("En Mantenimiento", resultado.getEstado());
    }

    @Test
    void devolverDeMantenimientoRepoeElStock() {
        Equipo existente = equipo(5, 3);
        existente.setStockMantenimiento(2);
        existente.setEstado("En Mantenimiento");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        Equipo resultado = service.ajustarMantenimiento(1L, -2);
        assertEquals(5, resultado.getStockDisponible());
        assertEquals(0, resultado.getStockMantenimiento());
        assertEquals("Disponible", resultado.getEstado());
    }

    @Test
    void enviarMasDeLoDisponibleLanzaConflicto() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipo(5, 1)));
        assertThrows(ResponseStatusException.class, () -> service.ajustarMantenimiento(1L, 2));
        verify(repo, never()).save(any(Equipo.class));
    }

    @Test
    void devolverMasDeLoQueHayEnMantenimientoLanzaConflicto() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipo(5, 5)));
        assertThrows(ResponseStatusException.class, () -> service.ajustarMantenimiento(1L, -1));
    }

    @Test
    void mantenimientoConCantidadCeroLanzaBadRequest() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipo(5, 5)));
        assertThrows(ResponseStatusException.class, () -> service.ajustarMantenimiento(1L, 0));
    }

    @Test
    void noSePuedeEliminarUnEquipoConUnidadesEnMantenimiento() {
        Equipo existente = equipo(5, 3);
        existente.setStockMantenimiento(2);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        assertThrows(ResponseStatusException.class, () -> service.eliminar(1L));
    }
}

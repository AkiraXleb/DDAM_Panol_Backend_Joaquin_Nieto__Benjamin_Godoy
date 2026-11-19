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
}

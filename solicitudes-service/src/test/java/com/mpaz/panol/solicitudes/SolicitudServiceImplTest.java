package com.mpaz.panol.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.mpaz.panol.solicitudes.client.EquipoClient;
import com.mpaz.panol.solicitudes.entities.DetalleSolicitud;
import com.mpaz.panol.solicitudes.entities.EstadoSolicitud;
import com.mpaz.panol.solicitudes.entities.Solicitud;
import com.mpaz.panol.solicitudes.repository.SolicitudRepository;
import com.mpaz.panol.solicitudes.services.SolicitudServiceImpl;

// Pruebas unitarias del servicio de solicitudes. El repositorio y el cliente de equipos son mocks
// y el reloj está fijo, así la regla de las 3 horas se prueba siempre igual.
class SolicitudServiceImplTest {

    private static final long HORA = 60L * 60L * 1000L;
    private static final long AHORA = 1_800_000_000_000L;

    private SolicitudRepository repo;
    private EquipoClient equipoClient;
    private SolicitudServiceImpl service;

    @BeforeEach
    void preparar() {
        repo = mock(SolicitudRepository.class);
        equipoClient = mock(EquipoClient.class);
        Clock reloj = Clock.fixed(Instant.ofEpochMilli(AHORA), ZoneOffset.UTC);
        service = new SolicitudServiceImpl(repo, equipoClient, reloj);
        when(repo.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Solicitud solicitudValida(long inicio) {
        Solicitud s = new Solicitud();
        s.setUsuarioId(1L);
        s.setUsuarioNombre("Patricia");
        s.setSala("Sala 4");
        s.setFechaInicio(inicio);
        s.setFechaDevolucion(inicio + HORA);
        s.setDetalles(new ArrayList<>(List.of(new DetalleSolicitud(10L, "Proyector", 2))));
        return s;
    }

    private Solicitud conEstado(String estado) {
        Solicitud s = solicitudValida(AHORA + 5 * HORA);
        s.setId(7L);
        s.setEstado(estado);
        return s;
    }

    // ---------- creación y regla de 3 horas ----------

    @Test
    void crearConMasDeTresHorasQuedaPendiente() {
        Solicitud creada = service.crear(solicitudValida(AHORA + 4 * HORA));
        assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
        assertEquals(AHORA, creada.getFechaCreacion());
        assertFalse(creada.getNotificada());
    }

    @Test
    void crearConExactamenteTresHorasEsValido() {
        Solicitud creada = service.crear(solicitudValida(AHORA + 3 * HORA));
        assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
    }

    @Test
    void crearConMenosDeTresHorasLanzaError() {
        Solicitud muyCercana = solicitudValida(AHORA + 2 * HORA);
        assertThrows(ResponseStatusException.class, () -> service.crear(muyCercana));
        verify(repo, never()).save(any(Solicitud.class));
    }

    @Test
    void crearSinSalaLanzaError() {
        Solicitud s = solicitudValida(AHORA + 4 * HORA);
        s.setSala("  ");
        assertThrows(ResponseStatusException.class, () -> service.crear(s));
    }

    @Test
    void crearConDevolucionAnteriorAlInicioLanzaError() {
        Solicitud s = solicitudValida(AHORA + 4 * HORA);
        s.setFechaDevolucion(s.getFechaInicio() - HORA);
        assertThrows(ResponseStatusException.class, () -> service.crear(s));
    }

    @Test
    void crearSinEquiposLanzaError() {
        Solicitud s = solicitudValida(AHORA + 4 * HORA);
        s.setDetalles(new ArrayList<>());
        assertThrows(ResponseStatusException.class, () -> service.crear(s));
    }

    @Test
    void crearConObservacionesDeMasDe200CaracteresLanzaError() {
        Solicitud s = solicitudValida(AHORA + 4 * HORA);
        s.setObservaciones("x".repeat(201));
        assertThrows(ResponseStatusException.class, () -> service.crear(s));
    }

    // ---------- cambios de estado ----------

    @Test
    void aceptarDescuentaElStockDeCadaEquipo() {
        when(repo.findById(7L)).thenReturn(Optional.of(conEstado(EstadoSolicitud.PENDIENTE)));
        Solicitud resultado = service.cambiarEstado(7L, EstadoSolicitud.ACEPTADA, null);
        assertEquals(EstadoSolicitud.ACEPTADA, resultado.getEstado());
        assertFalse(resultado.getNotificada());
        verify(equipoClient).ajustarStock(10L, -2);
    }

    @Test
    void rechazarSinMotivoLanzaError() {
        when(repo.findById(7L)).thenReturn(Optional.of(conEstado(EstadoSolicitud.PENDIENTE)));
        assertThrows(ResponseStatusException.class,
                () -> service.cambiarEstado(7L, EstadoSolicitud.RECHAZADA, " "));
    }

    @Test
    void rechazarConMotivoGuardaElMotivoYNoTocaElStock() {
        when(repo.findById(7L)).thenReturn(Optional.of(conEstado(EstadoSolicitud.PENDIENTE)));
        Solicitud resultado = service.cambiarEstado(7L, EstadoSolicitud.RECHAZADA, "Sin stock esa semana");
        assertEquals("Sin stock esa semana", resultado.getComentarioGestion());
        verify(equipoClient, never()).ajustarStock(any(Long.class), any(Integer.class));
    }

    @Test
    void devolverReponeElStock() {
        when(repo.findById(7L)).thenReturn(Optional.of(conEstado(EstadoSolicitud.ENTREGADA)));
        Solicitud resultado = service.cambiarEstado(7L, EstadoSolicitud.DEVUELTA, null);
        assertEquals(EstadoSolicitud.DEVUELTA, resultado.getEstado());
        verify(equipoClient).ajustarStock(10L, 2);
    }

    @Test
    void entregarRegistraLaFechaDeEntrega() {
        when(repo.findById(7L)).thenReturn(Optional.of(conEstado(EstadoSolicitud.ACEPTADA)));
        Solicitud resultado = service.cambiarEstado(7L, EstadoSolicitud.ENTREGADA, null);
        assertEquals(AHORA, resultado.getFechaEntrega());
    }

    @Test
    void soloSePuedeCancelarUnaSolicitudPendiente() {
        when(repo.findById(7L)).thenReturn(Optional.of(conEstado(EstadoSolicitud.ACEPTADA)));
        assertThrows(ResponseStatusException.class,
                () -> service.cambiarEstado(7L, EstadoSolicitud.CANCELADA, null));
    }

    @Test
    void noSePuedeEntregarUnaSolicitudPendiente() {
        when(repo.findById(7L)).thenReturn(Optional.of(conEstado(EstadoSolicitud.PENDIENTE)));
        assertThrows(ResponseStatusException.class,
                () -> service.cambiarEstado(7L, EstadoSolicitud.ENTREGADA, null));
    }

    @Test
    void cambiarEstadoDeSolicitudInexistenteLanzaNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
                () -> service.cambiarEstado(99L, EstadoSolicitud.ACEPTADA, null));
    }

    @Test
    void marcarNotificadaDejaElCampoEnTrue() {
        Solicitud s = conEstado(EstadoSolicitud.ACEPTADA);
        s.setNotificada(false);
        when(repo.findById(7L)).thenReturn(Optional.of(s));
        assertTrue(service.marcarNotificada(7L).getNotificada());
    }

    // ---------- listados ----------

    @Test
    void listarPorUsuarioYEstadoFiltraLosResultados() {
        Solicitud pendiente = conEstado(EstadoSolicitud.PENDIENTE);
        Solicitud aceptada = conEstado(EstadoSolicitud.ACEPTADA);
        when(repo.findByUsuarioIdOrderByFechaCreacionDesc(1L)).thenReturn(List.of(pendiente, aceptada));
        List<Solicitud> resultado = service.obtener(1L, EstadoSolicitud.ACEPTADA);
        assertEquals(1, resultado.size());
        assertEquals(EstadoSolicitud.ACEPTADA, resultado.get(0).getEstado());
    }
}

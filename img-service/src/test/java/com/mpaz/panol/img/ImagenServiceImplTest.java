package com.mpaz.panol.img;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mpaz.panol.img.entities.ImagenEquipo;
import com.mpaz.panol.img.repository.ImagenRepository;
import com.mpaz.panol.img.services.ImagenServiceImpl;

// Pruebas unitarias del servicio de imágenes (el repositorio es un mock)
class ImagenServiceImplTest {

    private ImagenRepository repo;
    private ImagenServiceImpl service;

    @BeforeEach
    void preparar() {
        repo = mock(ImagenRepository.class);
        service = new ImagenServiceImpl(repo);
        when(repo.save(any(ImagenEquipo.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void guardaUnaImagenValida() {
        ImagenEquipo img = service.guardar(1L, "image/jpeg", new byte[] {1, 2, 3});
        assertEquals(1L, img.getEquipoId());
        assertEquals("image/jpeg", img.getTipo());
        verify(repo).save(any(ImagenEquipo.class));
    }

    @Test
    void rechazaImagenVacia() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.guardar(1L, "image/jpeg", new byte[0]));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verify(repo, never()).save(any());
    }

    @Test
    void rechazaArchivoQueNoEsImagen() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.guardar(1L, "application/pdf", new byte[] {1}));
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getStatusCode());
    }

    @Test
    void rechazaImagenMuyGrande() {
        byte[] grande = new byte[3 * 1024 * 1024 + 1];
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.guardar(1L, "image/png", grande));
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, e.getStatusCode());
    }

    @Test
    void eliminarUnaImagenInexistenteNoFalla() {
        when(repo.existsById(9L)).thenReturn(false);
        service.eliminar(9L);
        verify(repo, never()).deleteById(any());
    }

    @Test
    void eliminarBorraLaImagenExistente() {
        when(repo.existsById(2L)).thenReturn(true);
        service.eliminar(2L);
        verify(repo).deleteById(2L);
    }
}

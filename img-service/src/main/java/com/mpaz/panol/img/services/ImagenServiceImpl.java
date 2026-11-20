package com.mpaz.panol.img.services;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mpaz.panol.img.entities.ImagenEquipo;
import com.mpaz.panol.img.repository.ImagenRepository;

@Service
public class ImagenServiceImpl implements ImagenService {

    static final long MAXIMO_BYTES = 3L * 1024 * 1024;

    private final ImagenRepository repo;

    public ImagenServiceImpl(ImagenRepository repo) {
        this.repo = repo;
    }

    @Override
    public ImagenEquipo guardar(Long equipoId, String tipo, byte[] datos) {
        if (datos == null || datos.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen está vacía");
        }
        if (datos.length > MAXIMO_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La imagen supera los 3 MB");
        }
        if (tipo == null || !tipo.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El archivo debe ser una imagen");
        }
        // Si ya existía, save() la reemplaza (mismo id)
        return repo.save(new ImagenEquipo(equipoId, tipo, datos));
    }

    @Override
    public Optional<ImagenEquipo> obtener(Long equipoId) {
        return repo.findById(equipoId);
    }

    @Override
    public void eliminar(Long equipoId) {
        // Borrar una imagen que no existe no es un error
        if (repo.existsById(equipoId)) {
            repo.deleteById(equipoId);
        }
    }
}

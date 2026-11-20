package com.mpaz.panol.img.services;

import java.util.Optional;

import com.mpaz.panol.img.entities.ImagenEquipo;

public interface ImagenService {

    ImagenEquipo guardar(Long equipoId, String tipo, byte[] datos);

    Optional<ImagenEquipo> obtener(Long equipoId);

    void eliminar(Long equipoId);
}

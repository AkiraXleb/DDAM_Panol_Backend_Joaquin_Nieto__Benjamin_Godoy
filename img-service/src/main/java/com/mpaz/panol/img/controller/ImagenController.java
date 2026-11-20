package com.mpaz.panol.img.controller;

import java.io.IOException;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mpaz.panol.img.services.ImagenService;

@RestController
@RequestMapping("api/imagenes/equipos")
public class ImagenController {

    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    // Sube o reemplaza la imagen del equipo (multipart, campo "archivo")
    @PutMapping(value = "/{equipoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subir(@PathVariable Long equipoId, @RequestParam("archivo") MultipartFile archivo) {
        try {
            imagenService.guardar(equipoId, archivo.getContentType(), archivo.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen");
        }
    }

    @GetMapping("/{equipoId}")
    public ResponseEntity<byte[]> obtener(@PathVariable Long equipoId) {
        return imagenService.obtener(equipoId)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getTipo()))
                        .cacheControl(CacheControl.noCache())
                        .body(img.getDatos()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{equipoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long equipoId) {
        imagenService.eliminar(equipoId);
    }
}

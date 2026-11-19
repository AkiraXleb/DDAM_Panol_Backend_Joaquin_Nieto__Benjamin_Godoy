package com.mpaz.panol.solicitudes.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

// Llama al microservicio de equipos para descontar o reponer stock
@Component
public class EquipoClient {

    private final RestClient restClient;

    public EquipoClient(@Value("${equipos.url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    // cantidad negativa descuenta, positiva repone
    public void ajustarStock(Long equipoId, int cantidad) {
        try {
            restClient.patch()
                    .uri("/api/equipos/{id}/stock", equipoId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("cantidad", cantidad))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            // El microservicio de equipos respondió con error
            if (e.getStatusCode().value() == 409) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No hay stock suficiente de un equipo de la solicitud");
            }
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Un equipo de la solicitud ya no existe en el inventario");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error en el servicio de equipos");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo contactar al servicio de equipos");
        }
    }
}

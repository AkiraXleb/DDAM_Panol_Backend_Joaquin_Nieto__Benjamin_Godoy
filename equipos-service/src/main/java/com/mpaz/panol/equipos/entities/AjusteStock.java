package com.mpaz.panol.equipos.entities;

import lombok.Data;

// Cuerpo de la petición PATCH /api/equipos/{id}/stock
// cantidad positiva = repone stock, negativa = descuenta stock
@Data
public class AjusteStock {

    private Integer cantidad;

    public AjusteStock() {
    }

    public AjusteStock(Integer cantidad) {
        this.cantidad = cantidad;
    }
}

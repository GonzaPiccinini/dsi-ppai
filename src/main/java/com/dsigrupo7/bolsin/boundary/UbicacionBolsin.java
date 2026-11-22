package com.dsigrupo7.bolsin.boundary;

import java.time.LocalDateTime;

public record UbicacionBolsin(
        Integer numeroBolsin,
        Double latitud,
        Double longitud,
        LocalDateTime fechaHoraUltimaActualizacion) {
}

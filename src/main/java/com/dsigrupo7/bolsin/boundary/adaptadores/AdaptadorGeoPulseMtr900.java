package com.dsigrupo7.bolsin.boundary.adaptadores;

import java.time.LocalDateTime;

import org.springframework.web.client.RestClientException;

import com.dsigrupo7.bolsin.boundary.UbicacionBolsin;
import com.dsigrupo7.bolsin.boundary.servicios.ServicioGeoPulseMtr900;

import tools.jackson.core.JacksonException;

public class AdaptadorGeoPulseMtr900 implements AdaptadorGPSTracker {

    private static final String API_KEY = "sk_geo_f2d84a1c9e7345b8a6d1c3f0972e";

    private static final int CAMPOS_ESPERADOS = 4;

    private final ServicioGeoPulseMtr900 servicio;

    public AdaptadorGeoPulseMtr900(ServicioGeoPulseMtr900 servicio) {
        this.servicio = servicio;
    }

    @Override
    public UbicacionBolsin obtenerUbicacionBolsin(Integer numeroBolsin, String codigoCMOrigen, String codigoCMDestino) {
        try {
            Object[][] respuesta = servicio.fetchCargoPositions(API_KEY, numeroBolsin);
            return adaptar(respuesta);
        } catch (RestClientException | JacksonException | ClassCastException | IllegalArgumentException e) {
            // Bolsin unknown to the tracker, tracker unavailable or invalid matrix: no location (A3).
            return null;
        }
    }

    // Converts the vendor matrix [[numeroBolsin, latitud, longitud, fechaHora]] into the common type.
    private UbicacionBolsin adaptar(Object[][] respuesta) {
        if (respuesta == null || respuesta.length == 0 || respuesta[0].length != CAMPOS_ESPERADOS) {
            return null;
        }
        Object[] fila = respuesta[0];
        return new UbicacionBolsin(((Number) fila[0]).intValue(),
                ((Number) fila[1]).doubleValue(),
                ((Number) fila[2]).doubleValue(),
                LocalDateTime.parse((String) fila[3]));
    }
}

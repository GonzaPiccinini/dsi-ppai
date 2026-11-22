package com.dsigrupo7.bolsin.boundary.adaptadores;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.web.client.RestClientException;

import com.dsigrupo7.bolsin.boundary.UbicacionBolsin;
import com.dsigrupo7.bolsin.boundary.servicios.ServicioNavTrackQX7A;

public class AdaptadorNavTrackQX7A implements AdaptadorGPSTracker {

    private static final String API_KEY = "sk_nav_7b61d9e4c3a2485fae10c77d5b2f";

    private static final int CAMPOS_ESPERADOS = 4;

    private final ServicioNavTrackQX7A servicio;

    public AdaptadorNavTrackQX7A(ServicioNavTrackQX7A servicio) {
        this.servicio = servicio;
    }

    @Override
    public UbicacionBolsin obtenerUbicacionBolsin(Integer numeroBolsin, String codigoCMOrigen, String codigoCMDestino) {
        try {
            String respuesta = servicio.retrieveTrackingData(API_KEY, numeroBolsin, codigoCMDestino);
            return adaptar(respuesta);
        } catch (RestClientException | NumberFormatException | DateTimeParseException e) {
            // Bolsin unknown to the tracker, tracker unavailable or invalid text: no location (A3).
            return null;
        }
    }

    // Converts the vendor text "numeroBolsin,latitud,longitud,fechaHora" into the common type.
    private UbicacionBolsin adaptar(String respuesta) {
        if (respuesta == null || respuesta.isBlank()) {
            return null;
        }
        String[] campos = respuesta.strip().split("\\R")[0].split(",");
        if (campos.length != CAMPOS_ESPERADOS) {
            return null;
        }
        return new UbicacionBolsin(Integer.valueOf(campos[0].strip()),
                Double.valueOf(campos[1].strip()),
                Double.valueOf(campos[2].strip()),
                LocalDateTime.parse(campos[3].strip()));
    }
}

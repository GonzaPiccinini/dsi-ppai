package com.dsigrupo7.bolsin.boundary.adaptadores;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.client.RestClientException;

import com.dsigrupo7.bolsin.boundary.UbicacionBolsin;
import com.dsigrupo7.bolsin.boundary.servicios.ServicioXtr4500l;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class AdaptadorXtr4500l implements IAdaptadorGPSTracker {

    private static final String API_KEY = "sk_xtr_9F3a7C21bE6d4A10f2c8e5b7d913";

    private final ServicioXtr4500l servicio;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public AdaptadorXtr4500l(ServicioXtr4500l servicio) {
        this.servicio = servicio;
    }

    @Override
    public UbicacionBolsin obtenerUbicacionBolsin(Integer numeroBolsin, String codigoCMOrigen, String codigoCMDestino) {
        try {
            String respuesta = servicio.getBolsinLocation(API_KEY, numeroBolsin, codigoCMOrigen);
            return adaptar(respuesta);
        } catch (RestClientException | JacksonException e) {
            // Bolsin unknown to the tracker, tracker unavailable or invalid JSON: no location (A3).
            return null;
        }
    }

    // Converts the vendor JSON {"items":[{...}]} into the common type.
    private UbicacionBolsin adaptar(String respuesta) {
        if (respuesta == null || respuesta.isBlank()) {
            return null;
        }
        Respuesta json = jsonMapper.readValue(respuesta, Respuesta.class);
        if (json.items() == null || json.items().isEmpty()) {
            return null;
        }
        Item item = json.items().get(0);
        return new UbicacionBolsin(item.numeroBolsin(), item.latitud(), item.longitud(),
                item.fechaHoraUltimaActualizacion());
    }

    public record Respuesta(List<Item> items) {
    }

    public record Item(Integer numeroBolsin, Double latitud, Double longitud,
                       LocalDateTime fechaHoraUltimaActualizacion) {
    }
}

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
            // A3: bolsin desconocido para el GPS Tracker, servicio no disponible o JSON invalido:
            // no hay ubicacion para informar.
            return null;
        }
    }

    /**
     * Adapta la respuesta de ServicioXtr4500l.getBolsinLocation() al tipo comun UbicacionBolsin.
     *
     * El modelo XTR-4500L devuelve un JSON en formato cadena de caracteres con un item por bolsin:
     * {"items":[{"numeroBolsin":101,"latitud":-31.42,"longitud":-64.18,
     *            "fechaHoraUltimaActualizacion":"2026-03-07T12:30"}]}
     *
     * 1. Si la cadena esta vacia no hay ubicacion (A3).
     * 2. Deserializa el JSON en los records Respuesta/Item (Jackson convierte la fecha ISO a LocalDateTime).
     * 3. Si no hay items no hay ubicacion (A3).
     * 4. Toma el primer item (se consulta de a un bolsin) y copia sus campos a UbicacionBolsin.
     */
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

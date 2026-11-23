package com.dsigrupo7.bolsin.boundary.adaptadores;

import java.time.LocalDateTime;

import org.springframework.web.client.RestClientException;

import com.dsigrupo7.bolsin.boundary.servicios.ServicioGeoPulseMtr900;

import tools.jackson.core.JacksonException;

public class AdaptadorGeoPulseMtr900 implements IAdaptadorGPSTracker {

    private static final String API_KEY = "sk_geo_f2d84a1c9e7345b8a6d1c3f0972e";

    private static final int CAMPOS_ESPERADOS = 4;

    private final ServicioGeoPulseMtr900 servicio;

    public AdaptadorGeoPulseMtr900(ServicioGeoPulseMtr900 servicio) {
        this.servicio = servicio;
    }

    @Override
    public Object[] obtenerUbicacionBolsin(Integer numeroBolsin, String codigoCMOrigen, String codigoCMDestino) {
        try {
            Object[][] respuesta = servicio.fetchCargoPositions(API_KEY, numeroBolsin);
            return adaptar(respuesta);
        } catch (RestClientException | JacksonException | ClassCastException | IllegalArgumentException e) {
            // A3: bolsin desconocido para el GPS Tracker, servicio no disponible o matriz invalida:
            // no hay ubicacion para informar.
            return null;
        }
    }

    /**
     * Adapta la respuesta de ServicioGeoPulseMtr900.fetchCargoPositions() al tipo comun Object[]
     * con indices fijos: [0] numero de bolsin (Integer), [1] latitud (Double), [2] longitud (Double)
     * y [3] fecha y hora de la ultima actualizacion (LocalDateTime).
     *
     * El modelo GeoPulse MTR-900 devuelve una matriz donde cada fila es un bolsin y cada columna un dato:
     * [[101, -31.4201, -64.1888, "2026-03-07T12:30"]]
     * (numero de bolsin, latitud, longitud y fecha y hora de la ultima actualizacion de posicion).
     *
     * 1. Si la matriz esta vacia o la fila no tiene 4 columnas no hay ubicacion (A3).
     * 2. Toma la primera fila (se consulta de a un bolsin).
     * 3. Las celdas llegan como Object: los numeros se convierten desde Number (intValue/doubleValue)
     *    y la fecha se parsea desde String a LocalDateTime. Un tipo inesperado lanza una excepcion
     *    que obtenerUbicacionBolsin() trata como A3.
     */
    private Object[] adaptar(Object[][] respuesta) {
        if (respuesta == null || respuesta.length == 0 || respuesta[0].length != CAMPOS_ESPERADOS) {
            return null;
        }
        Object[] fila = respuesta[0];
        return new Object[]{((Number) fila[0]).intValue(),
                ((Number) fila[1]).doubleValue(),
                ((Number) fila[2]).doubleValue(),
                LocalDateTime.parse((String) fila[3])};
    }
}

package com.dsigrupo7.bolsin.boundary.adaptadores;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.web.client.RestClientException;

import com.dsigrupo7.bolsin.boundary.servicios.ServicioNavTrackQX7A;

public class AdaptadorNavTrackQX7A implements IAdaptadorGPSTracker {

    private static final String API_KEY = "sk_nav_7b61d9e4c3a2485fae10c77d5b2f";

    private final ServicioNavTrackQX7A servicio;

    public AdaptadorNavTrackQX7A(ServicioNavTrackQX7A servicio) {
        this.servicio = servicio;
    }

    @Override
    public Object[] obtenerUbicacionBolsin(Integer numeroBolsin, String codigoCMOrigen, String codigoCMDestino) {
        try {
            String respuesta = servicio.retrieveTrackingData(API_KEY, numeroBolsin, codigoCMDestino);
            return adaptar(respuesta);
        } catch (RestClientException | NumberFormatException | DateTimeParseException e) {
            // A3: bolsin desconocido para el GPS Tracker, servicio no disponible o texto invalido:
            // no hay ubicacion para informar.
            return null;
        }
    }

    /**
     * Adapta la respuesta de ServicioNavTrackQX7A.retrieveTrackingData() al tipo comun Object[]
     * con indices fijos: [0] numero de bolsin (Integer), [1] latitud (Double), [2] longitud (Double)
     * y [3] fecha y hora de la ultima actualizacion (LocalDateTime).
     *
     * El modelo NavTrack QX-7A devuelve un unico String con sus items separados por comas:
     * "101,-31.4201,-64.1888,2026-03-07T12:30" (numero de bolsin, latitud, longitud y fecha y hora
     * del ultimo reporte).
     *
     * 1. Si el texto esta vacio no hay ubicacion (A3).
     * 2. Toma la primera linea y la separa por comas.
     * 3. Si no tiene exactamente 4 campos el formato es invalido: no hay ubicacion (A3).
     * 4. Convierte cada campo de texto a su tipo: Integer, Double, Double y LocalDateTime.
     *    Si un campo no se puede convertir se lanza una excepcion que obtenerUbicacionBolsin() trata como A3.
     */
    private Object[] adaptar(String respuesta) {
        if (respuesta == null || respuesta.isBlank()) {
            return null;
        }
        String[] campos = respuesta.strip().split("\\R")[0].split(",");
        if (campos.length != 4) {
            return null;
        }
        return new Object[]{Integer.valueOf(campos[0].strip()),
                Double.valueOf(campos[1].strip()),
                Double.valueOf(campos[2].strip()),
                LocalDateTime.parse(campos[3].strip())};
    }
}

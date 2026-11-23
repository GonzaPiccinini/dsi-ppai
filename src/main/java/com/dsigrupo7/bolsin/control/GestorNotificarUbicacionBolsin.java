package com.dsigrupo7.bolsin.control;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// Caso de Uso 31: Notificar ubicacion de bolsin.
@Service
public class GestorNotificarUbicacionBolsin {

    private static final Logger log = LoggerFactory.getLogger(GestorNotificarUbicacionBolsin.class);

    public boolean notificarUbicacionBolsin(String email, LocalDateTime fechaHora, Integer numeroBolsin,
            LocalDateTime fechaHoraUltimaActualizacion, Double latitud, Double longitud) {
        if (email == null || email.isBlank() || numeroBolsin == null) {
            return false;
        }

        String asunto = "Ubicacion del bolsin " + numeroBolsin;
        String cuerpo = "Fecha y hora de notificacion: " + fechaHora + "\n"
                + "Numero de bolsin: " + numeroBolsin + "\n"
                + "Fecha y hora de ultima actualizacion: " + fechaHoraUltimaActualizacion + "\n"
                + "Coordenadas: " + latitud + ", " + longitud;

        // TODO: reemplazar por el envio real del correo electronico.
        log.info("Enviando correo a {}\nAsunto: {}\n{}", email, asunto, cuerpo);
        return true;
    }
}

package com.dsigrupo7.bolsin.boundary.adaptadores;

public interface IAdaptadorGPSTracker {

    /**
     * Returns the current location of the bolsin, or {@code null} when the GPS Tracker
     * could not report it (CU 36, alternative flow A3).
     *
     * La ubicacion es un Object[] con indices fijos: [0] numero de bolsin (Integer), [1] latitud (Double),
     * [2] longitud (Double) y [3] fecha y hora de la ultima actualizacion (LocalDateTime).
     */
    Object[] obtenerUbicacionBolsin(Integer numeroBolsin, String codigoCMOrigen, String codigoCMDestino);
}

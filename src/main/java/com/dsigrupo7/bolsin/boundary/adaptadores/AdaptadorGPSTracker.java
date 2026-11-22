package com.dsigrupo7.bolsin.boundary.adaptadores;

import com.dsigrupo7.bolsin.boundary.UbicacionBolsin;

public interface AdaptadorGPSTracker {

    /**
     * Returns the current location of the bolsin, or {@code null} when the GPS Tracker
     * could not report it (CU 36, alternative flow A3).
     */
    UbicacionBolsin obtenerUbicacionBolsin(Integer numeroBolsin, String codigoCMOrigen, String codigoCMDestino);
}

package com.dsigrupo7.bolsin.boundary.servicios;

import org.springframework.web.client.RestClient;

/**
 * Client for the NavTrack QX-7A GPS Tracker endpoint. Returns the vendor comma-separated
 * text untouched; converting it is the adapter's job.
 */
public class ServicioNavTrackQX7A {

    private final RestClient restClient;

    public ServicioNavTrackQX7A(String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public String retrieveTrackingData(String apiKey, Integer numeroBolsin, String codigoCMDestino) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/navtrack-qx-7a/retrieveTrackingData")
                        .queryParam("numeroBolsin", numeroBolsin)
                        .queryParam("codigoComisionMedicaDestino", codigoCMDestino)
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .body(String.class);
    }
}

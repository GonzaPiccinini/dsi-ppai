package com.dsigrupo7.bolsin.boundary.servicios;

import org.springframework.web.client.RestClient;

/**
 * Client for the XTR-4500L GPS Tracker endpoint. Returns the vendor JSON untouched;
 * converting it is the adapter's job.
 */
public class ServicioXtr4500l {

    private final RestClient restClient;

    public ServicioXtr4500l(String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public String getBolsinLocation(String apiKey, Integer numeroBolsin, String codigoCMOrigen) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/xtr-4500l/getBolsinLocation")
                        .queryParam("numeroBolsin", numeroBolsin)
                        .queryParam("codigoComisionMedicaOrigen", codigoCMOrigen)
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .body(String.class);
    }
}

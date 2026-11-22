package com.dsigrupo7.bolsin.boundary.servicios;

import org.springframework.web.client.RestClient;

import tools.jackson.databind.json.JsonMapper;

/**
 * Client for the GeoPulse MTR-900 GPS Tracker endpoint. Returns the vendor matrix
 * as {@code Object[][]}; converting it is the adapter's job.
 */
public class ServicioGeoPulseMtr900 {

    private final RestClient restClient;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public ServicioGeoPulseMtr900(String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Object[][] fetchCargoPositions(String apiKey, Integer numeroBolsin) {
        String json = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/geopulse-mtr-900/fetchCargoPositions")
                        .queryParam("numeroBolsin", numeroBolsin)
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .body(String.class);
        if (json == null || json.isBlank()) {
            return new Object[0][];
        }
        return jsonMapper.readValue(json, Object[][].class);
    }
}

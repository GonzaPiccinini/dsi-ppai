package com.dsigrupo7.bolsin.boundary.adaptadores;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dsigrupo7.bolsin.boundary.servicios.ServicioXtr4500l;

@Configuration
public class AdaptadorGPSTrackerConfig {

    // Resolves from the GPS_TRACKER_URL environment variable via relaxed binding.
    // To use another GPS Tracker model, return its adapter built with its service, e.g.
    // new AdaptadorNavTrackQX7A(new ServicioNavTrackQX7A(baseUrl)).
    @Bean
    public AdaptadorGPSTracker adaptadorGPSTracker(@Value("${gps.tracker.url:http://localhost:8081}") String baseUrl) {
        return new AdaptadorXtr4500l(new ServicioXtr4500l(baseUrl));
    }
}

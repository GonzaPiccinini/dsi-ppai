package com.dsigrupo7.bolsin.boundary.adaptadores;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dsigrupo7.bolsin.boundary.servicios.ServicioGeoPulseMtr900;
import com.dsigrupo7.bolsin.boundary.servicios.ServicioNavTrackQX7A;
import com.dsigrupo7.bolsin.boundary.servicios.ServicioXtr4500l;
import com.sun.net.httpserver.HttpServer;

/**
 * Exercises each adapter against a local HTTP server that answers with the same payloads
 * as the dsi-api-bolsines mock, so adaptar() is tested through the public port.
 */
class AdaptadoresGPSTrackerTest {

    // Object[] con indices fijos: [0] numero de bolsin, [1] latitud, [2] longitud, [3] fecha y hora.
    private static final Object[] ESPERADA =
            {101, -31.4201, -64.1888, LocalDateTime.of(2026, 3, 7, 12, 30)};

    private HttpServer server;

    private String baseUrl;

    private final Map<String, String> respuestas = new HashMap<>();

    private final Map<String, String> ultimaQuery = new HashMap<>();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            ultimaQuery.put(path, exchange.getRequestURI().getQuery());
            String body = respuestas.get(path);
            int status = body == null ? 404 : 200;
            byte[] bytes = (body == null ? "{\"error\":\"BOLSIN_NO_ENCONTRADO\"}" : body).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void xtr4500lAdaptaElJsonDeItemsYEnviaLaCMOrigen() {
        respuestas.put("/api/xtr-4500l/getBolsinLocation",
                "{\"items\":[{\"numeroBolsin\":101,\"latitud\":-31.4201,\"longitud\":-64.1888,"
                        + "\"fechaHoraUltimaActualizacion\":\"2026-03-07T12:30:00\"}]}");

        Object[] ubicacion = new AdaptadorXtr4500l(new ServicioXtr4500l(baseUrl)).obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02");

        assertThat(ubicacion).containsExactly(ESPERADA);
        assertThat(ultimaQuery.get("/api/xtr-4500l/getBolsinLocation")).contains("codigoComisionMedicaOrigen=CM-CBA-01");
    }

    @Test
    void navTrackQX7AAdaptaElTextoSeparadoPorComasYEnviaLaCMDestino() {
        respuestas.put("/api/navtrack-qx-7a/retrieveTrackingData", "101,-31.4201,-64.1888,2026-03-07T12:30");

        Object[] ubicacion = new AdaptadorNavTrackQX7A(new ServicioNavTrackQX7A(baseUrl)).obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02");

        assertThat(ubicacion).containsExactly(ESPERADA);
        assertThat(ultimaQuery.get("/api/navtrack-qx-7a/retrieveTrackingData")).contains("codigoComisionMedicaDestino=CM-ROS-02");
    }

    @Test
    void geoPulseMtr900AdaptaLaMatriz() {
        respuestas.put("/api/geopulse-mtr-900/fetchCargoPositions", "[[101,-31.4201,-64.1888,\"2026-03-07T12:30:00\"]]");

        Object[] ubicacion = new AdaptadorGeoPulseMtr900(new ServicioGeoPulseMtr900(baseUrl)).obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02");

        assertThat(ubicacion).containsExactly(ESPERADA);
    }

    @Test
    void bolsinDesconocidoParaElTrackerDevuelveNull() {
        assertThat(new AdaptadorXtr4500l(new ServicioXtr4500l(baseUrl)).obtenerUbicacionBolsin(999, "CM-CBA-01", "CM-ROS-02")).isNull();
        assertThat(new AdaptadorNavTrackQX7A(new ServicioNavTrackQX7A(baseUrl)).obtenerUbicacionBolsin(999, "CM-CBA-01", "CM-ROS-02")).isNull();
        assertThat(new AdaptadorGeoPulseMtr900(new ServicioGeoPulseMtr900(baseUrl)).obtenerUbicacionBolsin(999, "CM-CBA-01", "CM-ROS-02")).isNull();
    }

    @Test
    void respuestaConFormatoInvalidoDevuelveNull() {
        respuestas.put("/api/navtrack-qx-7a/retrieveTrackingData", "101,no-es-un-numero,-64.1888,2026-03-07T12:30");
        respuestas.put("/api/geopulse-mtr-900/fetchCargoPositions", "[[101,-31.4201]]");

        assertThat(new AdaptadorNavTrackQX7A(new ServicioNavTrackQX7A(baseUrl)).obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02")).isNull();
        assertThat(new AdaptadorGeoPulseMtr900(new ServicioGeoPulseMtr900(baseUrl)).obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02")).isNull();
    }
}

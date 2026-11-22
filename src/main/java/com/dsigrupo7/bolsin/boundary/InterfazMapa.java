package com.dsigrupo7.bolsin.boundary;

import java.util.List;
import java.util.stream.Collectors;

public class InterfazMapa {

    public String crearMapa(List<UbicacionBolsin> ubicaciones) {
        if (ubicaciones == null || ubicaciones.isEmpty()) {
            return "";
        }

        String marcadores = ubicaciones.stream()
                .map(u -> "[" + u.numeroBolsin() + "," + u.latitud() + "," + u.longitud()
                        + ",\"" + u.fechaHoraUltimaActualizacion() + "\"]")
                .collect(Collectors.joining(","));

        return """
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
                <div id="mapa" style="height: 420px; max-width: 960px; margin-top: 1.5rem; border: 1px solid #dfe3e8; border-radius: 10px;"></div>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <script>
                    (function () {
                        var mapa = L.map('mapa');
                        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                        }).addTo(mapa);

                        var bolsines = [%s];
                        var puntos = [];
                        bolsines.forEach(function (b) {
                            var punto = [b[1], b[2]];
                            puntos.push(punto);

                            var popup = document.createElement('div');
                            var titulo = document.createElement('strong');
                            titulo.textContent = 'Bolsin ' + b[0];
                            popup.appendChild(titulo);
                            popup.appendChild(document.createElement('br'));
                            popup.appendChild(document.createTextNode('Ultima actualizacion: ' + b[3]));

                            L.marker(punto).addTo(mapa).bindPopup(popup).on('click', function () {
                                window.location.href = '/seleccion-bolsin?numeroBolsin=' + b[0];
                            });
                        });

                        if (puntos.length === 1) {
                            mapa.setView(puntos[0], 12);
                        } else {
                            mapa.fitBounds(puntos, { padding: [40, 40] });
                        }
                    })();
                </script>
                """.formatted(marcadores);
    }
}

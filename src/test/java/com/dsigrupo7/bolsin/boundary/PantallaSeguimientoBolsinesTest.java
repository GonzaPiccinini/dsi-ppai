package com.dsigrupo7.bolsin.boundary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.dsigrupo7.bolsin.control.GestorSeguimientoBolsines;

@ExtendWith(MockitoExtension.class)
class PantallaSeguimientoBolsinesTest {

    @Mock
    private GestorSeguimientoBolsines gestorSeguimientoBolsines;

    @InjectMocks
    private PantallaSeguimientoBolsines pantalla;

    @Test
    void seleccOpcUbicarBolsinesDelegaEnElGestorYMuestraLaPantalla() {
        ExtendedModelMap model = new ExtendedModelMap();

        String vista = pantalla.seleccOpcUbicarBolsines(model);

        verify(gestorSeguimientoBolsines).consultarUbicacionDeBolsines();
        assertThat(vista).isEqualTo("seguimiento-bolsines");
    }

    @Test
    void mostrarPosicionDeBolsinesEnMapaHabilitaElMapaEnLaPantalla() {
        pantalla.mostrarPosicionDeBolsinesEnMapa("<div id=\"mapa\"></div>");
        ExtendedModelMap model = new ExtendedModelMap();

        pantalla.tomarFiltroBolsin(null, null, model);

        assertThat(model.getAttribute("mapa")).isEqualTo("<div id=\"mapa\"></div>");
    }

    @Test
    void tomarConfirmacionEnvioEmailDelegaLaConfirmacionEnElGestor() {
        pantalla.solicitarEnvioCorreoElectronico();

        pantalla.tomarConfirmacionEnvioEmail(true, new ExtendedModelMap());

        verify(gestorSeguimientoBolsines).confirmarEnvioEmail(true);
    }

    @Test
    void tomarCancelacionCancelaElCUYBloqueaLasAccionesPosteriores() {
        ExtendedModelMap model = new ExtendedModelMap();

        pantalla.tomarCancelacion(model);

        verify(gestorSeguimientoBolsines).cancelarCU();
        assertThat(model.getAttribute("cuCancelado")).isEqualTo(true);
        assertThat(pantalla.tomarSeleccionBolsin(101, new ExtendedModelMap())).isEqualTo("redirect:/");
    }

    @Test
    void precintoNoEncontradoInformadoPorElGestorSeMuestraEnLaPantalla() {
        doAnswer(invocacion -> {
            pantalla.informarPrecintoNoEncontrado("P-999");
            return null;
        }).when(gestorSeguimientoBolsines).tomarFiltroBolsin(null, "P-999");
        ExtendedModelMap model = new ExtendedModelMap();

        pantalla.tomarFiltroBolsin(null, "P-999", model);

        assertThat(model.getAttribute("precintoNoEncontrado")).isEqualTo("P-999");
    }
}

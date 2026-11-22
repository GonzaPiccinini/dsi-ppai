package com.dsigrupo7.bolsin.boundary;

import java.util.ArrayList;
import java.util.List;

import com.dsigrupo7.bolsin.control.GestorSeguimientoBolsines;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PantallaSeguimientoBolsines {

    @Autowired
    private GestorSeguimientoBolsines gestorSeguimientoBolsines;

    private String mapa = "";
    private boolean mapaHabilitado = false;
    private boolean seleccionBolsinHabilitada = false;
    private boolean envioCorreoSolicitado = false;
    private boolean ejecucionExitosaCU31 = false;
    private boolean noHayBolsinesEnviados = false;
    private String precintoNoEncontrado;
    private List<Integer> numerosBolsinSinUbicacion = new ArrayList<>();
    private boolean cuCancelado = false;

    @GetMapping("/")
    public String seleccOpcUbicarBolsines(Model model) {
        this.habilitarPantalla();
        this.mostrarPantalla(model);
        return "seguimiento-bolsines";
    }

    private void habilitarPantalla() {
        mapa = "";
        mapaHabilitado = false;
        seleccionBolsinHabilitada = false;
        envioCorreoSolicitado = false;
        ejecucionExitosaCU31 = false;
        noHayBolsinesEnviados = false;
        precintoNoEncontrado = null;
        numerosBolsinSinUbicacion = new ArrayList<>();
        cuCancelado = false;
        gestorSeguimientoBolsines.consultarUbicacionDeBolsines();
    }

    public void informarNoHayBolsinesEnviados() {
        noHayBolsinesEnviados = true;
    }

    public void informarBolsinesSinUbicacion(List<Integer> numerosBolsin) {
        for (Integer numeroBolsin : numerosBolsin) {
            if (!numerosBolsinSinUbicacion.contains(numeroBolsin)) {
                numerosBolsinSinUbicacion.add(numeroBolsin);
            }
        }
    }

    public void informarPrecintoNoEncontrado(String numeroPrecinto) {
        precintoNoEncontrado = numeroPrecinto;
    }

    public void mostrarPosicionDeBolsinesEnMapa(String mapa) {
        this.mapa = mapa != null ? mapa : "";
        this.habilitarMapa();
    }

    private void habilitarMapa() {
        mapaHabilitado = !mapa.isEmpty();
    }

    public void pedirSeleccionBolsin() {
        seleccionBolsinHabilitada = true;
    }

    @GetMapping("/seleccion-bolsin")
    public String tomarSeleccionBolsin(
            @RequestParam(name = "numeroBolsin", required = false) Integer numeroBolsin,
            Model model) {
        if (numeroBolsin == null || cuCancelado) {
            return "redirect:/";
        }
        precintoNoEncontrado = null;
        envioCorreoSolicitado = false;
        ejecucionExitosaCU31 = false;
        gestorSeguimientoBolsines.tomarSeleccionBolsin(numeroBolsin);
        this.mostrarPantalla(model);
        return "seguimiento-bolsines";
    }

    @GetMapping("/filtro-bolsin")
    public String tomarFiltroBolsin(
            @RequestParam(name = "cmDestino", required = false) String cmDestino,
            @RequestParam(name = "precinto", required = false) String precinto,
            Model model) {
        if (cuCancelado) {
            return "redirect:/";
        }
        precintoNoEncontrado = null;
        envioCorreoSolicitado = false;
        ejecucionExitosaCU31 = false;
        gestorSeguimientoBolsines.tomarFiltroBolsin(cmDestino, precinto);
        this.mostrarPantalla(model);
        return "seguimiento-bolsines";
    }

    public void solicitarEnvioCorreoElectronico() {
        envioCorreoSolicitado = true;
    }

    @GetMapping("/confirmacion-envio-email")
    public String tomarConfirmacionEnvioEmail(
            @RequestParam(name = "confirmacion", required = false) Boolean confirmacion,
            Model model) {
        if (confirmacion == null || !envioCorreoSolicitado || cuCancelado) {
            return "redirect:/";
        }
        envioCorreoSolicitado = false;
        ejecucionExitosaCU31 = false;
        gestorSeguimientoBolsines.confirmarEnvioEmail(confirmacion);
        this.mostrarPantalla(model);
        return "seguimiento-bolsines";
    }

    public void mostrarEjecucionExitosaCU31() {
        ejecucionExitosaCU31 = true;
    }

    @GetMapping("/cancelar")
    public String tomarCancelacion(Model model) {
        cuCancelado = true;
        mapaHabilitado = false;
        seleccionBolsinHabilitada = false;
        envioCorreoSolicitado = false;
        ejecucionExitosaCU31 = false;
        precintoNoEncontrado = null;
        gestorSeguimientoBolsines.cancelarCU();
        this.mostrarPantalla(model);
        return "seguimiento-bolsines";
    }

    private void mostrarComisionesMedicasDestino(Model model) {
        model.addAttribute("comisionesMedicasDestino", gestorSeguimientoBolsines.getComisionesMedicasDestino());
    }

    private void mostrarNumerosPrecinto(Model model) {
        model.addAttribute("numerosPrecinto", gestorSeguimientoBolsines.getNumerosPrecinto());
    }

    private void mostrarPantalla(Model model) {
        model.addAttribute("empleado", gestorSeguimientoBolsines.getEmpleadoLogueado());
        model.addAttribute("mapa", mapaHabilitado ? mapa : "");
        model.addAttribute("seleccionBolsinHabilitada", seleccionBolsinHabilitada);
        model.addAttribute("bolsinSeleccionado", gestorSeguimientoBolsines.getBolsinSeleccionado());
        model.addAttribute("cmDestinoSeleccionada", gestorSeguimientoBolsines.getCmDestinoSeleccionada());
        model.addAttribute("numeroPrecintoSeleccionado", gestorSeguimientoBolsines.getNumeroPrecintoSeleccionado());
        model.addAttribute("envioCorreoSolicitado", envioCorreoSolicitado);
        model.addAttribute("deseaEnviarEmail", gestorSeguimientoBolsines.getDeseaEnviarEmail());
        model.addAttribute("envioEmailConfirmado", gestorSeguimientoBolsines.isEnvioEmailConfirmado());
        model.addAttribute("emailGCMDestino", gestorSeguimientoBolsines.getEmailGCM());
        model.addAttribute("ejecucionExitosaCU31", ejecucionExitosaCU31);
        model.addAttribute("ubicacionNotificada", gestorSeguimientoBolsines.getUbicacionNotificada());
        model.addAttribute("noHayBolsinesEnviados", noHayBolsinesEnviados);
        model.addAttribute("precintoNoEncontrado", precintoNoEncontrado);
        model.addAttribute("numerosBolsinSinUbicacion", numerosBolsinSinUbicacion);
        model.addAttribute("cuCancelado", cuCancelado);
        this.mostrarComisionesMedicasDestino(model);
        this.mostrarNumerosPrecinto(model);
    }
}

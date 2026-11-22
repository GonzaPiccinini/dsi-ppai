package com.dsigrupo7.bolsin.control;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.dsigrupo7.bolsin.boundary.InterfazMapa;
import com.dsigrupo7.bolsin.boundary.PantallaSeguimientoBolsines;
import com.dsigrupo7.bolsin.boundary.UbicacionBolsin;
import com.dsigrupo7.bolsin.boundary.adaptadores.IAdaptadorGPSTracker;
import com.dsigrupo7.bolsin.entity.Bolsin;
import com.dsigrupo7.bolsin.entity.ComisionMedica;
import com.dsigrupo7.bolsin.entity.Empleado;
import com.dsigrupo7.bolsin.entity.Sesion;
import com.dsigrupo7.bolsin.entity.Usuario;
import com.dsigrupo7.bolsin.repository.BolsinRepository;
import com.dsigrupo7.bolsin.repository.EmpleadoRepository;
import com.dsigrupo7.bolsin.repository.UsuarioRepository;

@Service
public class GestorSeguimientoBolsines {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BolsinRepository bolsinRepository;

    @Autowired
    @Lazy
    private PantallaSeguimientoBolsines pantallaSeguimientoBolsines;

    @Autowired
    private GestorNotificarUbicacionBolsin gestorNotificarUbicacionBolsin;

    @Autowired
    private IAdaptadorGPSTracker adaptadorGPSTracker;

    private Empleado empleadoLogueado;
    private ComisionMedica CMUsuarioLogueado;
    private List<Bolsin> bolsinesEnviados = new ArrayList<>();
    private List<Integer> numeroBolsinesEnviados = new ArrayList<>();
    private String codigoCMUsuarioLogueado;
    private List<UbicacionBolsin> posicionBolsin = new ArrayList<>();
    private List<Integer> numeroBolsinesSinUbicacion = new ArrayList<>();
    private Bolsin bolsinSeleccionado;
    private ComisionMedica CMDestino;
    private String emailGCM;
    private Integer numeroBolsinSeleccionado;
    private List<UbicacionBolsin> ubicacionesAMostrar = new ArrayList<>();
    private List<String> comisionesMedicasDestino = new ArrayList<>();
    private List<String> numerosPrecinto = new ArrayList<>();
    private String mapa = "";
    private String cmDestinoSeleccionada;
    private String numeroPrecintoSeleccionado;
    private Boolean deseaEnviarEmail;
    private boolean envioEmailConfirmado;
    private UbicacionBolsin ubicacionNotificada;

    public void consultarUbicacionDeBolsines() {
        bolsinSeleccionado = null;
        numeroBolsinSeleccionado = null;
        CMDestino = null;
        cmDestinoSeleccionada = null;
        numeroPrecintoSeleccionado = null;
        deseaEnviarEmail = null;
        envioEmailConfirmado = false;
        emailGCM = null;
        ubicacionNotificada = null;
        posicionBolsin = new ArrayList<>();
        ubicacionesAMostrar = posicionBolsin;
        numeroBolsinesSinUbicacion = new ArrayList<>();
        comisionesMedicasDestino = new ArrayList<>();
        numerosPrecinto = new ArrayList<>();
        mapa = "";

        CMUsuarioLogueado = obtenerCMUsuario();
        codigoCMUsuarioLogueado = CMUsuarioLogueado != null ? CMUsuarioLogueado.getCodigo() : null;

        bolsinesEnviados = obtenerBolsinesEnviadosDeCM(CMUsuarioLogueado);
        if (bolsinesEnviados.isEmpty()) {
            // A1: no bolsines in Enviado state for the logged user's CM.
            pantallaSeguimientoBolsines.informarNoHayBolsinesEnviados();
            finCU();
            return;
        }

        posicionBolsin = obtenerDatosLocalizacionBolsines(bolsinesEnviados);
        ubicacionesAMostrar = posicionBolsin;
        if (!numeroBolsinesSinUbicacion.isEmpty()) {
            // A3: the GPS Tracker could not report the location of some bolsines.
            pantallaSeguimientoBolsines.informarBolsinesSinUbicacion(numeroBolsinesSinUbicacion);
        }

        comisionesMedicasDestino = buscarComisionesMedicasDestino();
        numerosPrecinto = buscarNumerosPrecinto();
        visualizarBolsinesEnMapa();

        pantallaSeguimientoBolsines.pedirSeleccionBolsin();
    }

    public void tomarSeleccionBolsin(Integer numeroBolsin) {
        numeroBolsinSeleccionado = numeroBolsin;
        bolsinSeleccionado = null;
        CMDestino = null;
        deseaEnviarEmail = null;
        envioEmailConfirmado = false;
        emailGCM = null;
        ubicacionNotificada = null;
        for (Bolsin bolsin : bolsinesEnviados) {
            if (bolsin.getNumeroBolsin() != null && bolsin.getNumeroBolsin().equals(numeroBolsinSeleccionado)) {
                bolsinSeleccionado = bolsin;
            }
        }

        if (bolsinSeleccionado == null) {
            return;
        }
        if (numeroBolsinesSinUbicacion.contains(numeroBolsinSeleccionado)) {
            // A3: without a reported location there is nothing to notify.
            pantallaSeguimientoBolsines.informarBolsinesSinUbicacion(List.of(numeroBolsinSeleccionado));
            return;
        }
        pantallaSeguimientoBolsines.solicitarEnvioCorreoElectronico();
    }

    public void confirmarEnvioEmail(Boolean confirmacion) {
        deseaEnviarEmail = confirmacion;
        envioEmailConfirmado = false;
        emailGCM = null;
        ubicacionNotificada = null;

        if (Boolean.TRUE.equals(confirmacion) && bolsinSeleccionado != null) {
            envioEmailConfirmado = true;
            enviarEmailAGCMDestino();
        } else {
            // A5: the EB does not choose to send the email.
            finCU();
        }
    }

    public void tomarFiltroBolsin(String cmDestino, String numeroPrecinto) {
        cmDestinoSeleccionada = cmDestino == null || cmDestino.isBlank() ? null : cmDestino;
        numeroPrecintoSeleccionado = numeroPrecinto == null || numeroPrecinto.isBlank() ? null : numeroPrecinto;
        bolsinSeleccionado = null;
        numeroBolsinSeleccionado = null;
        CMDestino = null;
        deseaEnviarEmail = null;
        envioEmailConfirmado = false;
        emailGCM = null;
        ubicacionNotificada = null;

        List<Integer> numerosBolsinAMostrar = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            ComisionMedica cmDestinoBolsin = bolsin.obtenerCMDestino();
            boolean coincideCMDestino = cmDestinoSeleccionada == null
                    || (cmDestinoBolsin != null && cmDestinoSeleccionada.equals(cmDestinoBolsin.getNombre()));
            boolean coincidePrecinto = numeroPrecintoSeleccionado == null
                    || numeroPrecintoSeleccionado.equals(bolsin.getNumeroPrecinto());

            if (coincideCMDestino && coincidePrecinto) {
                numerosBolsinAMostrar.add(bolsin.getNumeroBolsin());
            }
        }

        ubicacionesAMostrar = new ArrayList<>();
        for (UbicacionBolsin ubicacion : posicionBolsin) {
            if (numerosBolsinAMostrar.contains(ubicacion.numeroBolsin())) {
                ubicacionesAMostrar.add(ubicacion);
            }
        }

        if (numeroPrecintoSeleccionado != null && !existeBolsinConPrecinto(numeroPrecintoSeleccionado)) {
            // A2: no sent bolsin has the entered seal number.
            pantallaSeguimientoBolsines.informarPrecintoNoEncontrado(numeroPrecintoSeleccionado);
        }

        visualizarBolsinesEnMapa();
    }

    public void cancelarCU() {
        // A4: the EB cancels the use case.
        bolsinSeleccionado = null;
        deseaEnviarEmail = null;
        envioEmailConfirmado = false;
        emailGCM = null;
        ubicacionNotificada = null;
        ubicacionesAMostrar = new ArrayList<>();
        finCU();
    }

    private boolean existeBolsinConPrecinto(String numeroPrecinto) {
        for (Bolsin bolsin : bolsinesEnviados) {
            if (numeroPrecinto.equals(bolsin.getNumeroPrecinto())) {
                return true;
            }
        }
        return false;
    }

    private ComisionMedica obtenerCMUsuario() {
        Sesion sesion = obtenerSesionMock();
        Usuario usuario = sesion.getUsuario();

        empleadoLogueado = null;
        List<Empleado> empleados = empleadoRepository.findAll();
        for (Empleado empleado : empleados) {
            if (empleado.esTuUsuario(usuario)) {
                empleadoLogueado = empleado;
            }
        }

        return empleadoLogueado != null ? empleadoLogueado.getCM() : null;
    }

    private List<Bolsin> obtenerBolsinesEnviadosDeCM(ComisionMedica comisionMedica) {
        List<Bolsin> bolsinesEnviadosDeCM = new ArrayList<>();

        List<Bolsin> bolsines = bolsinRepository.findAll();
        for (Bolsin bolsin : bolsines) {
            if (bolsin.sosEnviado() && bolsin.esTuCMOrigen(comisionMedica)) {
                bolsinesEnviadosDeCM.add(bolsin);
            }
        }

        return bolsinesEnviadosDeCM;
    }

    private List<UbicacionBolsin> obtenerDatosLocalizacionBolsines(List<Bolsin> bolsines) {
        numeroBolsinesEnviados = new ArrayList<>();
        List<UbicacionBolsin> ubicacionesBolsines = new ArrayList<>();
        for (Bolsin bolsin : bolsines) {
            Integer numeroBolsin = bolsin.getNumeroBolsin();
            numeroBolsinesEnviados.add(numeroBolsin);

            ComisionMedica cmDestino = bolsin.obtenerCMDestino();
            String codigoCMDestino = cmDestino != null ? cmDestino.getCodigo() : null;
            UbicacionBolsin ubicacionBolsin = adaptadorGPSTracker.obtenerUbicacionBolsin(numeroBolsin,
                    codigoCMUsuarioLogueado, codigoCMDestino);
            if (ubicacionBolsin == null) {
                numeroBolsinesSinUbicacion.add(numeroBolsin);
            } else {
                ubicacionesBolsines.add(ubicacionBolsin);
            }
        }
        return ubicacionesBolsines;
    }

    private List<String> buscarComisionesMedicasDestino() {
        List<String> nombres = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            ComisionMedica cmDestino = bolsin.obtenerCMDestino();
            String nombre = cmDestino != null ? cmDestino.getNombre() : null;
            if (nombre != null && !nombres.contains(nombre)) {
                nombres.add(nombre);
            }
        }
        return nombres;
    }

    private List<String> buscarNumerosPrecinto() {
        List<String> precintos = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            String numeroPrecinto = bolsin.getNumeroPrecinto();
            if (numeroPrecinto != null && !precintos.contains(numeroPrecinto)) {
                precintos.add(numeroPrecinto);
            }
        }
        return precintos;
    }

    private void visualizarBolsinesEnMapa() {
        InterfazMapa interfazMapa = new InterfazMapa();
        mapa = interfazMapa.crearMapa(ubicacionesAMostrar);
        pantallaSeguimientoBolsines.mostrarPosicionDeBolsinesEnMapa(mapa);
    }

    private void enviarEmailAGCMDestino() {
        CMDestino = bolsinSeleccionado.obtenerCMDestino();
        String codigoCMDestino = CMDestino != null ? CMDestino.getCodigo() : null;

        emailGCM = consultarEmailGCMDestino(codigoCMDestino);
        if (emailGCM == null) {
            return;
        }

        UbicacionBolsin ubicacion = buscarUbicacionBolsinSeleccionado();
        if (ubicacion == null) {
            return;
        }

        boolean notificacionExitosa = gestorNotificarUbicacionBolsin.notificarUbicacionBolsin(
                emailGCM,
                ubicacion.numeroBolsin(),
                ubicacion.fechaHoraUltimaActualizacion(),
                ubicacion.latitud(),
                ubicacion.longitud());

        if (notificacionExitosa) {
            ubicacionNotificada = ubicacion;
            pantallaSeguimientoBolsines.mostrarEjecucionExitosaCU31();
            finCU();
        }
    }

    private String consultarEmailGCMDestino(String codigoCMDestino) {
        String email = null;
        List<Empleado> empleados = empleadoRepository.findAll();
        for (Empleado empleado : empleados) {
            if (empleado.esTuCM(codigoCMDestino) && empleado.sosGCM()) {
                email = empleado.getEmail();
            }
        }

        return email;
    }

    private UbicacionBolsin buscarUbicacionBolsinSeleccionado() {
        UbicacionBolsin ubicacionBolsin = null;
        for (UbicacionBolsin ubicacion : posicionBolsin) {
            if (ubicacion.numeroBolsin() != null && ubicacion.numeroBolsin().equals(bolsinSeleccionado.getNumeroBolsin())) {
                ubicacionBolsin = ubicacion;
            }
        }
        return ubicacionBolsin;
    }

    private void finCU() {
        // The selection has been consumed: a new bolsin must be selected to run the flow again.
        numeroBolsinSeleccionado = null;
    }

    private Sesion obtenerSesionMock() {
        // TODO: reemplazar por la sesion real una vez que exista el login.
        Usuario usuario = usuarioRepository.findAll().stream().findFirst().orElse(null);

        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        return sesion;
    }

    public Empleado getEmpleadoLogueado() {
        return empleadoLogueado;
    }

    public List<Integer> getNumeroBolsinesSinUbicacion() {
        return numeroBolsinesSinUbicacion;
    }

    public String getMapa() {
        return mapa;
    }

    public List<String> getComisionesMedicasDestino() {
        return comisionesMedicasDestino;
    }

    public List<String> getNumerosPrecinto() {
        return numerosPrecinto;
    }

    public Bolsin getBolsinSeleccionado() {
        return bolsinSeleccionado;
    }

    public String getCmDestinoSeleccionada() {
        return cmDestinoSeleccionada;
    }

    public String getNumeroPrecintoSeleccionado() {
        return numeroPrecintoSeleccionado;
    }

    public Boolean getDeseaEnviarEmail() {
        return deseaEnviarEmail;
    }

    public boolean isEnvioEmailConfirmado() {
        return envioEmailConfirmado;
    }

    public String getEmailGCM() {
        return emailGCM;
    }

    public UbicacionBolsin getUbicacionNotificada() {
        return ubicacionNotificada;
    }
}

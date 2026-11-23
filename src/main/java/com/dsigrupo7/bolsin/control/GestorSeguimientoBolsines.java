package com.dsigrupo7.bolsin.control;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.dsigrupo7.bolsin.boundary.InterfazMapa;
import com.dsigrupo7.bolsin.boundary.PantallaSeguimientoBolsines;
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
    // Cada ubicacion es un Object[] con indices fijos: [0] numero de bolsin (Integer), [1] latitud (Double),
    // [2] longitud (Double) y [3] fecha y hora de la ultima actualizacion (LocalDateTime).
    private List<Object[]> posicionBolsin = new ArrayList<>();
    private List<Integer> numeroBolsinesSinUbicacion = new ArrayList<>();
    private Bolsin bolsinSeleccionado;
    private ComisionMedica CMDestino;
    private String emailGCM;
    private Integer numeroBolsinSeleccionado;
    private LocalDateTime fechaHora;
    private List<Object[]> ubicacionesAMostrar = new ArrayList<>();
    private List<String> comisionesMedicasDestino = new ArrayList<>();
    private List<String> numerosPrecinto = new ArrayList<>();
    private String mapa = "";
    private String cmDestinoSeleccionada;
    private String numeroPrecintoSeleccionado;
    private Boolean deseaEnviarEmail;
    private boolean envioEmailConfirmado;
    private Object[] ubicacionNotificada;

    // CU 36 - Consultar seguimiento de bolsines.
    // Se ejecuta despues del paso 1 (el EB selecciona la opcion para consultar la ubicacion de los bolsines).
    public void consultarUbicacionDeBolsines() {
        // Se descarta el estado de una consulta anterior para iniciar el CU desde cero.
        bolsinSeleccionado = null;
        numeroBolsinSeleccionado = null;
        CMDestino = null;
        cmDestinoSeleccionada = null;
        numeroPrecintoSeleccionado = null;
        deseaEnviarEmail = null;
        envioEmailConfirmado = false;
        emailGCM = null;
        fechaHora = null;
        ubicacionNotificada = null;
        posicionBolsin = new ArrayList<>();
        ubicacionesAMostrar = posicionBolsin;
        numeroBolsinesSinUbicacion = new ArrayList<>();
        comisionesMedicasDestino = new ArrayList<>();
        numerosPrecinto = new ArrayList<>();
        mapa = "";

        // Paso 2: el sistema busca la CM del usuario logueado (la pantalla la muestra al renderizarse).
        CMUsuarioLogueado = obtenerCMUsuario();
        codigoCMUsuarioLogueado = CMUsuarioLogueado != null ? CMUsuarioLogueado.getCodigo() : null;

        // Paso 3: el sistema busca los bolsines en estado Enviado cuya CM origen es la CM del usuario logueado.
        bolsinesEnviados = obtenerBolsinesEnviadosDeCM(CMUsuarioLogueado);
        if (bolsinesEnviados.isEmpty()) {
            // A1: no se encuentran bolsines en estado Enviado. Se informa al EB y finaliza el CU.
            pantallaSeguimientoBolsines.informarNoHayBolsinesEnviados();
            finCU();
            return;
        }

        // Pasos 4 y 5: el sistema solicita al GPS Tracker los datos de localizacion de los bolsines
        // y los recibe (a traves del adaptador del modelo configurado).
        posicionBolsin = obtenerDatosLocalizacionBolsines(bolsinesEnviados);
        ubicacionesAMostrar = posicionBolsin;
        if (!numeroBolsinesSinUbicacion.isEmpty()) {
            // A3: el GPS Tracker no pudo informar la ubicacion de uno o mas bolsines. Se informa al EB
            // y el CU continua con los bolsines que si tienen ubicacion.
            pantallaSeguimientoBolsines.informarBolsinesSinUbicacion(numeroBolsinesSinUbicacion);
        }

        // Paso 6: el sistema muestra sobre un mapa la posicion de los bolsines, habilitando la opcion
        // de filtrar por numero de precinto o CM destino y de seleccionar un bolsin del mapa.
        comisionesMedicasDestino = buscarComisionesMedicasDestino();
        numerosPrecinto = buscarNumerosPrecinto();
        visualizarBolsinesEnMapa();

        pantallaSeguimientoBolsines.pedirSeleccionBolsin();
    }

    // Paso 7: el EB selecciona un bolsin del mapa.
    public void tomarSeleccionBolsin(Integer numeroBolsin) {
        numeroBolsinSeleccionado = numeroBolsin;
        bolsinSeleccionado = null;
        CMDestino = null;
        deseaEnviarEmail = null;
        envioEmailConfirmado = false;
        emailGCM = null;
        fechaHora = null;
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
            // A3: el GPS Tracker no informo la ubicacion del bolsin seleccionado, por lo que no hay
            // ubicacion para notificar y no se ofrece el envio del correo electronico.
            pantallaSeguimientoBolsines.informarBolsinesSinUbicacion(List.of(numeroBolsinSeleccionado));
            return;
        }
        // Paso 8: el sistema consulta si se desea enviar un correo electronico al GCM de la CM destino
        // para informar la ubicacion del bolsin.
        pantallaSeguimientoBolsines.solicitarEnvioCorreoElectronico();
    }

    // Paso 9: el EB confirma (o no) el envio del correo electronico.
    public void confirmarEnvioEmail(Boolean confirmacion) {
        deseaEnviarEmail = confirmacion;
        envioEmailConfirmado = false;
        emailGCM = null;
        fechaHora = null;
        ubicacionNotificada = null;

        if (Boolean.TRUE.equals(confirmacion) && bolsinSeleccionado != null) {
            envioEmailConfirmado = true;
            enviarEmailAGCMDestino();
        } else {
            // A5: el EB no selecciona la opcion para enviar el correo electronico. Fin del CU.
            finCU();
        }
    }

    // Paso 6 (opcion de filtrado): el EB filtra los bolsines del mapa por CM destino y/o numero de precinto.
    // Se filtra sobre las ubicaciones ya obtenidas, sin volver a consultar al GPS Tracker.
    public void tomarFiltroBolsin(String cmDestino, String numeroPrecinto) {
        cmDestinoSeleccionada = cmDestino == null || cmDestino.isBlank() ? null : cmDestino;
        numeroPrecintoSeleccionado = numeroPrecinto == null || numeroPrecinto.isBlank() ? null : numeroPrecinto;
        bolsinSeleccionado = null;
        numeroBolsinSeleccionado = null;
        CMDestino = null;
        deseaEnviarEmail = null;
        envioEmailConfirmado = false;
        emailGCM = null;
        fechaHora = null;
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
        for (Object[] ubicacion : posicionBolsin) {
            if (numerosBolsinAMostrar.contains((Integer) ubicacion[0])) {
                ubicacionesAMostrar.add(ubicacion);
            }
        }

        if (numeroPrecintoSeleccionado != null && !existeBolsinConPrecinto(numeroPrecintoSeleccionado)) {
            // A2: no se encuentra un bolsin con el numero de precinto ingresado. Se informa al EB.
            pantallaSeguimientoBolsines.informarPrecintoNoEncontrado(numeroPrecintoSeleccionado);
        }

        visualizarBolsinesEnMapa();
    }

    // A4: el EB cancela la ejecucion del caso de uso (puede ocurrir en cualquier paso).
    public void cancelarCU() {
        bolsinSeleccionado = null;
        deseaEnviarEmail = null;
        envioEmailConfirmado = false;
        emailGCM = null;
        fechaHora = null;
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

    // Paso 2: obtiene el empleado de la sesion y su CM.
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

    // Paso 3: filtra los bolsines en estado Enviado cuya CM origen es la CM del usuario logueado.
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

    // Pasos 4 y 5: por cada bolsin enviado se pide la ubicacion al GPS Tracker. El Gestor solo conoce
    // a IAdaptadorGPSTracker (patron Adapter): el adaptador concreto traduce la llamada al endpoint del
    // modelo y convierte su respuesta a un Object[] (ver indices en posicionBolsin).
    private List<Object[]> obtenerDatosLocalizacionBolsines(List<Bolsin> bolsines) {
        numeroBolsinesEnviados = new ArrayList<>();
        List<Object[]> ubicacionesBolsines = new ArrayList<>();
        for (Bolsin bolsin : bolsines) {
            Integer numeroBolsin = bolsin.getNumeroBolsin();
            numeroBolsinesEnviados.add(numeroBolsin);

            ComisionMedica cmDestino = bolsin.obtenerCMDestino();
            String codigoCMDestino = cmDestino != null ? cmDestino.getCodigo() : null;
            // Paso 4: solicita al GPS Tracker los datos de localizacion (se envian la CM origen y la CM
            // destino porque cada modelo usa una u otra).
            Object[] ubicacionBolsin = adaptadorGPSTracker.obtenerUbicacionBolsin(numeroBolsin,
                    codigoCMUsuarioLogueado, codigoCMDestino);
            // Paso 5: recibe los datos de localizacion del bolsin.
            if (ubicacionBolsin == null) {
                // A3: el GPS Tracker no pudo informar la ubicacion de este bolsin.
                numeroBolsinesSinUbicacion.add(numeroBolsin);
            } else {
                ubicacionesBolsines.add(ubicacionBolsin);
            }
        }
        return ubicacionesBolsines;
    }

    // Paso 6: CM destino disponibles para el filtro del mapa.
    private List<String> buscarComisionesMedicasDestino() {
        List<String> nombres = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            String nombre = bolsin.obtenerCMDestino() != null ? bolsin.obtenerCMDestino().getNombre() : null;
            if (nombre != null && !nombres.contains(nombre)) {
                nombres.add(nombre);
            }
        }
        return nombres;
    }

    // Paso 6: numeros de precinto disponibles para el filtro del mapa.
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

    // Paso 6: genera el mapa (Servidor Google Maps / Leaflet) con la posicion de los bolsines y lo muestra.
    private void visualizarBolsinesEnMapa() {
        InterfazMapa interfazMapa = new InterfazMapa();
        mapa = interfazMapa.crearMapa(ubicacionesAMostrar);
        pantallaSeguimientoBolsines.mostrarPosicionDeBolsinesEnMapa(mapa);
    }

    private void enviarEmailAGCMDestino() {
        // Paso 10: el sistema busca la direccion de correo electronico del GCM de la CM destino.
        CMDestino = bolsinSeleccionado.obtenerCMDestino();
        String codigoCMDestino = CMDestino != null ? CMDestino.getCodigo() : null;

        emailGCM = consultarEmailGCMDestino(codigoCMDestino);
        if (emailGCM == null) {
            return;
        }

        Object[] ubicacion = buscarUbicacionBolsinSeleccionado();
        if (ubicacion == null) {
            return;
        }

        // Paso 11: se incluye el CU 31 "Notificar ubicacion de bolsin" para informar numero de bolsin,
        // fecha y hora de ultima actualizacion y coordenadas del bolsin.
        fechaHora = getFechaHora();
        boolean notificacionExitosa = gestorNotificarUbicacionBolsin.notificarUbicacionBolsin(
                emailGCM,
                fechaHora,
                (Integer) ubicacion[0],
                (LocalDateTime) ubicacion[3],
                (Double) ubicacion[1],
                (Double) ubicacion[2]);

        if (notificacionExitosa) {
            ubicacionNotificada = ubicacion;
            // Paso 11: informa la ejecucion exitosa del CU 31. Fin del caso de uso.
            pantallaSeguimientoBolsines.mostrarEjecucionExitosaCU31();
            finCU();
        }
    }

    // Paso 10: busca el empleado con rol GCM de la CM destino y devuelve su email.
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

    private Object[] buscarUbicacionBolsinSeleccionado() {
        Object[] ubicacionBolsin = null;
        for (Object[] ubicacion : posicionBolsin) {
            if (ubicacion[0] != null && ubicacion[0].equals(bolsinSeleccionado.getNumeroBolsin())) {
                ubicacionBolsin = ubicacion;
            }
        }
        return ubicacionBolsin;
    }

    private LocalDateTime getFechaHora() {
        return LocalDateTime.now();
    }

    // Fin del caso de uso: se descarta la seleccion; para repetir el flujo se debe seleccionar otro bolsin.
    private void finCU() {
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

    public LocalDateTime getFechaHoraNotificacion() {
        return fechaHora;
    }

    public Object[] getUbicacionNotificada() {
        return ubicacionNotificada;
    }
}

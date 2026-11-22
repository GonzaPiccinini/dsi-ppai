package com.dsigrupo7.bolsin.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dsigrupo7.bolsin.boundary.PantallaSeguimientoBolsines;
import com.dsigrupo7.bolsin.boundary.UbicacionBolsin;
import com.dsigrupo7.bolsin.boundary.adaptadores.IAdaptadorGPSTracker;
import com.dsigrupo7.bolsin.entity.Bolsin;
import com.dsigrupo7.bolsin.entity.CambioEstadoBolsin;
import com.dsigrupo7.bolsin.entity.ComisionMedica;
import com.dsigrupo7.bolsin.entity.Empleado;
import com.dsigrupo7.bolsin.entity.Estado;
import com.dsigrupo7.bolsin.entity.Rol;
import com.dsigrupo7.bolsin.entity.Usuario;
import com.dsigrupo7.bolsin.repository.BolsinRepository;
import com.dsigrupo7.bolsin.repository.EmpleadoRepository;
import com.dsigrupo7.bolsin.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class GestorSeguimientoBolsinesTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BolsinRepository bolsinRepository;

    @Mock
    private PantallaSeguimientoBolsines pantallaSeguimientoBolsines;

    @Mock
    private GestorNotificarUbicacionBolsin gestorNotificarUbicacionBolsin;

    @Mock
    private IAdaptadorGPSTracker adaptadorGPSTracker;

    @InjectMocks
    private GestorSeguimientoBolsines gestor;

    private ComisionMedica cmCordoba;

    private ComisionMedica cmRosario;

    private Empleado empleadoLogueado;

    @BeforeEach
    void setUp() {
        cmCordoba = comisionMedica("CM-CBA-01", "Cordoba");
        cmRosario = comisionMedica("CM-ROS-02", "Rosario");

        Usuario usuario = new Usuario();
        empleadoLogueado = new Empleado();
        empleadoLogueado.setUsuario(usuario);
        empleadoLogueado.setComisionMedica(cmCordoba);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(empleadoRepository.findAll()).thenReturn(List.of(empleadoLogueado));
    }

    @Test
    void consultarUbicacionDeBolsinesPideAlTrackerSoloLosEnviadosConCMOrigenYDestino() {
        Bolsin enviado = bolsin(101, cmCordoba, cmRosario, "ENVIADO");
        Bolsin noEnviado = bolsin(102, cmCordoba, cmRosario, "CERRADO");
        Bolsin deOtraCM = bolsin(103, cmRosario, cmCordoba, "ENVIADO");
        when(bolsinRepository.findAll()).thenReturn(List.of(enviado, noEnviado, deOtraCM));

        UbicacionBolsin ubicacion = new UbicacionBolsin(101, -31.4201, -64.1888, LocalDateTime.of(2026, 3, 7, 12, 30));
        when(adaptadorGPSTracker.obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02")).thenReturn(ubicacion);

        gestor.consultarUbicacionDeBolsines();

        verify(adaptadorGPSTracker).obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02");
        verifyNoMoreInteractions(adaptadorGPSTracker);
        assertThat(gestor.getMapa()).contains("[101,-31.4201,-64.1888,");
        assertThat(gestor.getComisionesMedicasDestino()).containsExactly("Rosario");
        verify(pantallaSeguimientoBolsines).mostrarPosicionDeBolsinesEnMapa(gestor.getMapa());
        verify(pantallaSeguimientoBolsines).pedirSeleccionBolsin();
    }

    @Test
    void consultarUbicacionDeBolsinesSinUbicacionDelTrackerDejaElMapaVacio() {
        when(bolsinRepository.findAll()).thenReturn(List.of(bolsin(101, cmCordoba, cmRosario, "ENVIADO")));
        when(adaptadorGPSTracker.obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02")).thenReturn(null);

        gestor.consultarUbicacionDeBolsines();

        assertThat(gestor.getMapa()).isEmpty();
        verify(pantallaSeguimientoBolsines).informarBolsinesSinUbicacion(List.of(101));
    }

    @Test
    void a1SinBolsinesEnviadosInformaALaPantallaYNoConsultaAlTracker() {
        when(bolsinRepository.findAll()).thenReturn(List.of(bolsin(102, cmCordoba, cmRosario, "CERRADO")));

        gestor.consultarUbicacionDeBolsines();

        verify(pantallaSeguimientoBolsines).informarNoHayBolsinesEnviados();
        verify(pantallaSeguimientoBolsines, never()).pedirSeleccionBolsin();
        verifyNoInteractions(adaptadorGPSTracker);
    }

    @Test
    void a2PrecintoIngresadoInexistenteInformaALaPantalla() {
        when(bolsinRepository.findAll()).thenReturn(List.of(bolsin(101, cmCordoba, cmRosario, "ENVIADO")));
        when(adaptadorGPSTracker.obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02")).thenReturn(ubicacion101());
        gestor.consultarUbicacionDeBolsines();

        gestor.tomarFiltroBolsin(null, "P-999");

        verify(pantallaSeguimientoBolsines).informarPrecintoNoEncontrado("P-999");
        assertThat(gestor.getMapa()).isEmpty();
    }

    @Test
    void a2PrecintoExistenteMuestraSoloEseBolsin() {
        Bolsin conPrecinto = bolsin(101, cmCordoba, cmRosario, "ENVIADO");
        conPrecinto.setNumeroPrecinto("P-001");
        when(bolsinRepository.findAll()).thenReturn(List.of(conPrecinto));
        when(adaptadorGPSTracker.obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02")).thenReturn(ubicacion101());
        gestor.consultarUbicacionDeBolsines();

        gestor.tomarFiltroBolsin(null, "P-001");

        verify(pantallaSeguimientoBolsines, never()).informarPrecintoNoEncontrado("P-001");
        assertThat(gestor.getMapa()).contains("[101,");
    }

    @Test
    void a3SeleccionarBolsinSinUbicacionNoOfreceElEnvioDeEmail() {
        when(bolsinRepository.findAll()).thenReturn(List.of(
                bolsin(101, cmCordoba, cmRosario, "ENVIADO"), bolsin(111, cmCordoba, cmRosario, "ENVIADO")));
        when(adaptadorGPSTracker.obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02")).thenReturn(ubicacion101());
        when(adaptadorGPSTracker.obtenerUbicacionBolsin(111, "CM-CBA-01", "CM-ROS-02")).thenReturn(null);
        gestor.consultarUbicacionDeBolsines();

        gestor.tomarSeleccionBolsin(111);

        assertThat(gestor.getNumeroBolsinesSinUbicacion()).containsExactly(111);
        verify(pantallaSeguimientoBolsines, never()).solicitarEnvioCorreoElectronico();
    }

    @Test
    void a4CancelarCUDescartaLaSeleccionYElMapa() {
        when(bolsinRepository.findAll()).thenReturn(List.of(bolsin(101, cmCordoba, cmRosario, "ENVIADO")));
        when(adaptadorGPSTracker.obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02")).thenReturn(ubicacion101());
        gestor.consultarUbicacionDeBolsines();
        gestor.tomarSeleccionBolsin(101);

        gestor.cancelarCU();

        assertThat(gestor.getBolsinSeleccionado()).isNull();
        assertThat(gestor.getDeseaEnviarEmail()).isNull();
        verifyNoInteractions(gestorNotificarUbicacionBolsin);
    }

    private static UbicacionBolsin ubicacion101() {
        return new UbicacionBolsin(101, -31.4201, -64.1888, LocalDateTime.of(2026, 3, 7, 12, 30));
    }

    @Test
    void confirmarEnvioEmailNotificaAlGCMDestinoConLaUbicacionDelBolsinSeleccionado() {
        Empleado gcmRosario = new Empleado();
        gcmRosario.setEmail("gcm@rosario.com");
        gcmRosario.setComisionMedica(cmRosario);
        Rol rolGCM = new Rol();
        rolGCM.setNombre("GCM");
        gcmRosario.setRol(rolGCM);
        when(empleadoRepository.findAll()).thenReturn(List.of(empleadoLogueado, gcmRosario));

        when(bolsinRepository.findAll()).thenReturn(List.of(bolsin(101, cmCordoba, cmRosario, "ENVIADO")));
        LocalDateTime fecha = LocalDateTime.of(2026, 3, 7, 12, 30);
        when(adaptadorGPSTracker.obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02"))
                .thenReturn(new UbicacionBolsin(101, -31.4201, -64.1888, fecha));
        when(gestorNotificarUbicacionBolsin.notificarUbicacionBolsin("gcm@rosario.com", 101, fecha, -31.4201, -64.1888))
                .thenReturn(true);

        gestor.consultarUbicacionDeBolsines();
        gestor.tomarSeleccionBolsin(101);
        gestor.confirmarEnvioEmail(true);

        assertThat(gestor.getEmailGCM()).isEqualTo("gcm@rosario.com");
        assertThat(gestor.getUbicacionNotificada().numeroBolsin()).isEqualTo(101);
        verify(pantallaSeguimientoBolsines).mostrarEjecucionExitosaCU31();
    }

    @Test
    void confirmarEnvioEmailRechazadoNoNotifica() {
        when(bolsinRepository.findAll()).thenReturn(List.of(bolsin(101, cmCordoba, cmRosario, "ENVIADO")));
        when(adaptadorGPSTracker.obtenerUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02")).thenReturn(null);

        gestor.consultarUbicacionDeBolsines();
        gestor.tomarSeleccionBolsin(101);
        gestor.confirmarEnvioEmail(false);

        assertThat(gestor.isEnvioEmailConfirmado()).isFalse();
        verifyNoInteractions(gestorNotificarUbicacionBolsin);
    }

    private static ComisionMedica comisionMedica(String codigo, String nombre) {
        ComisionMedica comisionMedica = new ComisionMedica();
        comisionMedica.setCodigo(codigo);
        comisionMedica.setNombre(nombre);
        return comisionMedica;
    }

    private static Bolsin bolsin(Integer numero, ComisionMedica origen, ComisionMedica destino, String nombreEstado) {
        Estado estado = new Estado();
        estado.setNombre(nombreEstado);

        CambioEstadoBolsin cambioActual = new CambioEstadoBolsin();
        cambioActual.setEstado(estado);

        Bolsin bolsin = new Bolsin();
        bolsin.setNumeroBolsin(numero);
        bolsin.setCmOrigen(origen);
        bolsin.setCmDestino(destino);
        bolsin.setCambioEstado(List.of(cambioActual));
        return bolsin;
    }
}

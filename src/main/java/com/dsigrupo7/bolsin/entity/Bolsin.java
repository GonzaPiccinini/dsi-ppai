package com.dsigrupo7.bolsin.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Bolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate fecha;
    private Integer numeroBolsin;
    private String numeroPrecinto;
    private Double peso;
    
    @ManyToOne
    @JoinColumn(name = "cm_origen_id")
    private ComisionMedica cmOrigen;

    @ManyToOne
    @JoinColumn(name = "cm_destino_id")
    private ComisionMedica cmDestino;

    @OneToMany(mappedBy = "bolsin")
    private List<CambioEstadoBolsin> cambioEstado;

    public Bolsin() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getNumeroBolsin() {
        return numeroBolsin;
    }

    public void setNumeroBolsin(Integer numeroBolsin) {
        this.numeroBolsin = numeroBolsin;
    }

    public String getNumeroPrecinto() {
        return numeroPrecinto;
    }

    public void setNumeroPrecinto(String numeroPrecinto) {
        this.numeroPrecinto = numeroPrecinto;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public ComisionMedica getCmOrigen() {
        return cmOrigen;
    }

    public void setCmOrigen(ComisionMedica cmOrigen) {
        this.cmOrigen = cmOrigen;
    }

    public ComisionMedica getCmDestino() {
        return cmDestino;
    }

    public void setCmDestino(ComisionMedica cmDestino) {
        this.cmDestino = cmDestino;
    }

    public List<CambioEstadoBolsin> getCambioEstado() {
        return cambioEstado;
    }

    public void setCambioEstado(List<CambioEstadoBolsin> cambioEstado) {
        this.cambioEstado = cambioEstado;
    }

    public boolean esTuCMOrigen(ComisionMedica comisionMedica) {
        return this.cmOrigen != null && this.cmOrigen.equals(comisionMedica);
    }

    public ComisionMedica obtenerCMDestino() {
        return cmDestino;
    }

    public boolean sosEnviado() {
        CambioEstadoBolsin cambioActual = obtenerCambioEstadoActual();
        return cambioActual != null && cambioActual.sosEnviado();
    }

    private CambioEstadoBolsin obtenerCambioEstadoActual() {
        if (cambioEstado == null) {
            return null;
        }
        for (CambioEstadoBolsin cambio : cambioEstado) {
            if (cambio.sosActual()) {
                return cambio;
            }
        }
        return null;
    }
}

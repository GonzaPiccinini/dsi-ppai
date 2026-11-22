package com.dsigrupo7.bolsin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String apellido;
    private String email;
    private String nombre;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "comision_medica_id")
    private ComisionMedica comisionMedica;

    public Empleado() {
    }

    public Long getId() {
        return id;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setComisionMedica(ComisionMedica comisionMedica) {
        this.comisionMedica = comisionMedica;
    }

    public boolean esTuUsuario(Usuario usuario) {
        return this.usuario != null && this.usuario.equals(usuario);
    }

    public ComisionMedica getCM() {
        return comisionMedica;
    }

    public boolean sosGCM() {
        return rol != null && rol.esGCM();
    }

    public String getEmail() {
        return email;
    }

    public boolean esTuCM(String codigoComisionMedica) {
        return this.comisionMedica != null && this.comisionMedica.getCodigo() != null
                && this.comisionMedica.getCodigo().equals(codigoComisionMedica);
    }
}

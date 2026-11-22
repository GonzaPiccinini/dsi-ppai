package com.dsigrupo7.bolsin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsigrupo7.bolsin.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}

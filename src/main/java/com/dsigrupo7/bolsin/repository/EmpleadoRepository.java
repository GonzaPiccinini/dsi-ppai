package com.dsigrupo7.bolsin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsigrupo7.bolsin.entity.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
}

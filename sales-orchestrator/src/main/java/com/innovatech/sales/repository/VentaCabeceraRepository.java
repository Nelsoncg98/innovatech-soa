package com.innovatech.sales.repository;

import com.innovatech.sales.model.VentaCabecera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaCabeceraRepository extends JpaRepository<VentaCabecera, Integer> {
}

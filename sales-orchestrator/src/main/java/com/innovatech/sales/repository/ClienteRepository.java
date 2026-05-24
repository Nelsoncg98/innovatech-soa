package com.innovatech.sales.repository;

import com.innovatech.sales.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // Buscar un cliente mediante su documento de identidad único
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
}

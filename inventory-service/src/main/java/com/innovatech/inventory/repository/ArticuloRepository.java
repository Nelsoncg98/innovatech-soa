package com.innovatech.inventory.repository;

import com.innovatech.inventory.model.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {

    // Consulta personalizada para buscar un artículo mediante su SKU único
    Optional<Articulo> findBySku(String sku);
}

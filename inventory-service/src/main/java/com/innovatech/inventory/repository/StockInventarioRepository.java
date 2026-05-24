package com.innovatech.inventory.repository;

import com.innovatech.inventory.model.StockInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StockInventarioRepository extends JpaRepository<StockInventario, Integer> {

    // Consulta personalizada para buscar el inventario asociado al SKU comercial de un artículo
    Optional<StockInventario> findByArticuloSku(String sku);
}

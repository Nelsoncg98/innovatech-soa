package com.innovatech.inventory.controller;

import com.innovatech.inventory.model.StockInventario;
import com.innovatech.inventory.repository.StockInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inventario")
public class InventoryController {

    @Autowired
    private StockInventarioRepository stockRepository;

    // ========================================================================
    // 🔍 ENDPOINT 1: Consultar disponibilidad de stock en tiempo real
    // GET /api/v1/inventario/{sku}
    // ========================================================================
    @GetMapping("/{sku}")
    public ResponseEntity<?> consultarStock(@PathVariable String sku) {
        Optional<StockInventario> inventarioOpt = stockRepository.findByArticuloSku(sku);

        if (inventarioOpt.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Artículo no encontrado");
            errorResponse.put("sku", sku);
            return ResponseEntity.status(404).body(errorResponse);
        }

        StockInventario inventario = inventarioOpt.get();
        
        // Mapear un JSON limpio y descriptivo de salida
        Map<String, Object> response = new HashMap<>();
        response.put("sku", inventario.getArticulo().getSku());
        response.put("nombre", inventario.getArticulo().getNombre());
        response.put("precioUnitario", inventario.getArticulo().getPrecioUnitario());
        response.put("stockFisico", inventario.getStockFisico());
        response.put("stockReservado", inventario.getStockReservado());
        response.put("stockDisponible", inventario.getStockDisponible()); // Retorna el campo calculado
        
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // 🔒 ENDPOINT 2: Reserva y bloqueo temporal de stock (Soft-lock)
    // PUT /api/v1/inventario/reserva
    // ========================================================================
    @PutMapping("/reserva")
    @Transactional // Garantiza atomicidad transaccional de la escritura en base de datos
    public ResponseEntity<?> reservarStock(@RequestBody Map<String, Object> requestBody) {
        String sku = (String) requestBody.get("sku");
        Integer cantidad = (Integer) requestBody.get("cantidad");

        if (sku == null || cantidad == null || cantidad <= 0) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Parámetros de petición inválidos o cantidad menor a 1.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Optional<StockInventario> inventarioOpt = stockRepository.findByArticuloSku(sku);

        if (inventarioOpt.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Artículo no encontrado para reserva");
            errorResponse.put("sku", sku);
            return ResponseEntity.status(404).body(errorResponse);
        }

        StockInventario inventario = inventarioOpt.get();

        // VALIDACIÓN TRANSACCIONAL: Verificar si hay suficiente stock disponible neto
        if (inventario.getStockDisponible() < cantidad) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Stock insuficiente para realizar la reserva");
            errorResponse.put("sku", sku);
            errorResponse.put("stockDisponibleActual", inventario.getStockDisponible());
            errorResponse.put("cantidadSolicitada", cantidad);
            return ResponseEntity.status(400).body(errorResponse);
        }

        // Afectar el stock en base de datos: Sumar a la reserva temporal (Soft-lock)
        inventario.setStockReservado(inventario.getStockReservado() + cantidad);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        
        // Guardar cambios persistidos
        stockRepository.save(inventario);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Stock reservado exitosamente (Soft-lock)");
        response.put("sku", inventario.getArticulo().getSku());
        response.put("nombre", inventario.getArticulo().getNombre());
        response.put("cantidadReservada", cantidad);
        response.put("nuevoStockReservadoTotal", inventario.getStockReservado());
        response.put("nuevoStockDisponible", inventario.getStockDisponible());

        return ResponseEntity.ok(response);
    }
}

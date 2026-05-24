package com.innovatech.inventory.controller;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Permite simular llamadas HTTP de forma purista y limpia en consola
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Controla el orden exacto de ejecucion de las pruebas
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========================================================================
    // 🧪 PRUEBA 1: Consultar stock de un articulo existente (TECH-LAP-001)
    // ========================================================================
    @Test
    @Order(1)
    public void deberiaRetornarStockCuandoSkuExiste() throws Exception {
        mockMvc.perform(get("/api/v1/inventario/TECH-LAP-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera codigo 200 OK
                .andExpect(jsonPath("$.sku").value("TECH-LAP-001"))
                .andExpect(jsonPath("$.nombre").value("Laptop Dell Latitude 5430"))
                .andExpect(jsonPath("$.stockDisponible").exists());
    }

    // ========================================================================
    // 🧪 PRUEBA 2: Consultar stock de un SKU que NO existe
    // ========================================================================
    @Test
    @Order(2)
    public void deberiaRetornar404CuandoSkuNoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/inventario/SKU-FALSO-999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Espera codigo 404 Not Found
                .andExpect(jsonPath("$.error").value("Artículo no encontrado"));
    }

    // ========================================================================
    // 🧪 PRUEBA 3: Realizar una reserva de stock exitosa (Soft-lock)
    // ========================================================================
    @Test
    @Order(3)
    public void deberiaReservarStockExitosamente() throws Exception {
        String payloadJson = "{\"sku\": \"TECH-PHN-001\", \"cantidad\": 2}";

        mockMvc.perform(put("/api/v1/inventario/reserva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isOk()) // Espera codigo 200 OK
                .andExpect(jsonPath("$.mensaje").value("Stock reservado exitosamente (Soft-lock)"))
                .andExpect(jsonPath("$.sku").value("TECH-PHN-001"))
                .andExpect(jsonPath("$.cantidadReservada").value(2));
    }

    // ========================================================================
    // 🧪 PRUEBA 4: Intentar reservar stock mayor a la disponibilidad (Debe fallar)
    // ========================================================================
    @Test
    @Order(4)
    public void deberiaFallarReservaCuandoStockEsInsuficiente() throws Exception {
        // TECH-LAP-002 tiene solo 15 unidades de stock en base de datos. Intentaremos reservar 99.
        String payloadJson = "{\"sku\": \"TECH-LAP-002\", \"cantidad\": 99}";

        mockMvc.perform(put("/api/v1/inventario/reserva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isBadRequest()) // Espera codigo 400 Bad Request
                .andExpect(jsonPath("$.error").value("Stock insuficiente para realizar la reserva"))
                .andExpect(jsonPath("$.cantidadSolicitada").value(99));
    }
}

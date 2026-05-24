package com.innovatech.sales.controller;

import com.innovatech.sales.repository.VentaCabeceraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Simula llamadas HTTP REST al endpoint de checkout del orquestador
public class SalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate; // Mokeamos el cliente HTTP para simular la comunicacion de red

    @Autowired
    private VentaCabeceraRepository ventaRepository;

    @BeforeEach
    public void setUp() {
        // Limpiamos los mocks antes de cada prueba
        Mockito.reset(restTemplate);
    }

    // ========================================================================
    // 🧪 PRUEBA 1: Checkout Omnicanal Exitoso (Pago aprobado)
    // ========================================================================
    @Test
    public void deberiaCompletarCheckoutExitosamente() throws Exception {
        // 1. Simular la respuesta de stock en vivo (GET /api/v1/inventario/TECH-LAP-001)
        Map<String, Object> mockInvResponse = new HashMap<>();
        mockInvResponse.put("sku", "TECH-LAP-001");
        mockInvResponse.put("articuloId", 1);
        mockInvResponse.put("nombre", "Laptop Dell Latitude 5430");
        mockInvResponse.put("stockDisponible", 20);

        Mockito.when(restTemplate.getForObject(contains("/TECH-LAP-001"), eq(Map.class)))
                .thenReturn(mockInvResponse);

        // 2. Simular la respuesta de reserva (PUT /api/v1/inventario/reserva)
        Map<String, Object> mockReservaResponse = new HashMap<>();
        mockReservaResponse.put("mensaje", "Stock reservado exitosamente (Soft-lock)");

        Mockito.when(restTemplate.exchange(
                contains("/reserva"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Map.class)))
                .thenReturn(ResponseEntity.ok(mockReservaResponse));

        // 3. Ejecutar peticion de checkout canónica
        String payloadJson = "{"
                + "\"transaccionId\": \"TRX-TEST-001\","
                + "\"canalOrigen\": \"ECOMMERCE_WEB\","
                + "\"metodoPago\": \"TARJETA_CREDITO\","
                + "\"cliente\": {\"numeroDocumento\": \"76543210\"},"
                + "\"lineasArticulo\": [{"
                + "    \"sku\": \"TECH-LAP-001\","
                + "    \"cantidad\": 1,"
                + "    \"precioUnitario\": 3200.00"
                + "}]"
                + "}";

        mockMvc.perform(post("/api/v1/ventas/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.estado").value("PROCESADO"))
                .andExpect(jsonPath("$.mensaje").value("Venta cobrada e integrada en ERP SAP central correctamente."));
    }

    // ========================================================================
    // 🧪 PRUEBA 2: Checkout Fallido por Pago Rechazado (Dispara Rollback de stock)
    // ========================================================================
    @Test
    public void deberiaEjecutarRollbackCuandoPagoEsRechazado() throws Exception {
        // 1. Simular la respuesta de stock disponible en vivo (GET)
        Map<String, Object> mockInvResponse = new HashMap<>();
        mockInvResponse.put("sku", "TECH-LAP-001");
        mockInvResponse.put("nombre", "Laptop Dell Latitude 5430");
        mockInvResponse.put("stockDisponible", 20);

        Mockito.when(restTemplate.getForObject(contains("/TECH-LAP-001"), eq(Map.class)))
                .thenReturn(mockInvResponse);

        // 2. Simular la respuesta de reserva (PUT de Soft-lock)
        Map<String, Object> mockReservaResponse = new HashMap<>();
        mockReservaResponse.put("mensaje", "Stock reservado exitosamente (Soft-lock)");

        Mockito.when(restTemplate.exchange(
                contains("/reserva"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Map.class)))
                .thenReturn(ResponseEntity.ok(mockReservaResponse));

        // 3. Ejecutar peticion de checkout canónica con método de pago rechazado: "TARJETA_RECHAZADA"
        String payloadJson = "{"
                + "\"transaccionId\": \"TRX-TEST-RECHAZADA\","
                + "\"canalOrigen\": \"SUCURSAL_04_AREQUIPA\","
                + "\"metodoPago\": \"TARJETA_RECHAZADA\","
                + "\"cliente\": {\"numeroDocumento\": \"76543210\"},"
                + "\"lineasArticulo\": [{"
                + "    \"sku\": \"TECH-LAP-001\","
                + "    \"cantidad\": 2,"
                + "    \"precioUnitario\": 3200.00"
                + "}]"
                + "}";

        mockMvc.perform(post("/api/v1/ventas/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isPaymentRequired()) // Espera 402 Payment Required
                .andExpect(jsonPath("$.error").value("Transacción rechazada por la pasarela de pagos Visa/Niubiz."))
                .andExpect(jsonPath("$.mensaje").value("Rollback transaccional ejecutado exitosamente en el Kardex de inventario. Stock liberado."));

        // 4. VERIFICAR QUE EL ROLLBACK SE DISPARÓ: RestTemplate debió ser invocado dos veces (una para reservar y otra para rollback)
        Mockito.verify(restTemplate, Mockito.times(2))
                .exchange(contains("/reserva"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Map.class));
    }
}

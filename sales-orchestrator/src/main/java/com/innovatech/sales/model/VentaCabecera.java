package com.innovatech.sales.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "VentasCabecera", schema = "dbo")
public class VentaCabecera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "venta_id")
    private Integer ventaId;

    @Column(name = "transaccion_id", unique = true, nullable = false, length = 50)
    private String transaccionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "canal_origen", nullable = false, length = 50)
    private String canalOrigen;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "ventaCabecera", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VentaDetalle> lineasDetalle = new ArrayList<>();

    // --- CONSTRUCTORES ---
    public VentaCabecera() {
    }

    // --- GETTERS Y SETTERS ---
    public Integer getVentaId() {
        return ventaId;
    }

    public void setVentaId(Integer ventaId) {
        this.ventaId = ventaId;
    }

    public String getTransaccionId() {
        return transaccionId;
    }

    public void setTransaccionId(String transaccionId) {
        this.transaccionId = transaccionId;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getCanalOrigen() {
        return canalOrigen;
    }

    public void setCanalOrigen(String canalOrigen) {
        this.canalOrigen = canalOrigen;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<VentaDetalle> getLineasDetalle() {
        return lineasDetalle;
    }

    public void setLineasDetalle(List<VentaDetalle> lineasDetalle) {
        this.lineasDetalle = lineasDetalle;
    }
}

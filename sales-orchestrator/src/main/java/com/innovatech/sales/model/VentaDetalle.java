package com.innovatech.sales.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "VentasDetalle", schema = "dbo")
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalle_id")
    private Integer detalleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @JsonIgnore // Evita bucle infinito en serializacion JSON
    private VentaCabecera ventaCabecera;

    @Column(name = "articulo_id", nullable = false)
    private Integer articuloId; // Hacemos referencia simple al ID del articulo

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAplicado;

    // --- CONSTRUCTORES ---
    public VentaDetalle() {
    }

    // --- GETTERS Y SETTERS ---
    public Integer getDetalleId() {
        return detalleId;
    }

    public void setDetalleId(Integer detalleId) {
        this.detalleId = detalleId;
    }

    public VentaCabecera getVentaCabecera() {
        return ventaCabecera;
    }

    public void setVentaCabecera(VentaCabecera ventaCabecera) {
        this.ventaCabecera = ventaCabecera;
    }

    public Integer getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(Integer articuloId) {
        this.articuloId = articuloId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioAplicado() {
        return precioAplicado;
    }

    public void setPrecioAplicado(BigDecimal precioAplicado) {
        this.precioAplicado = precioAplicado;
    }
}

package com.innovatech.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "StockInventario", schema = "dbo")
public class StockInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventario_id")
    private Integer inventarioId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "articulo_id", nullable = false, unique = true)
    private Articulo articulo;

    @Column(name = "stock_fisico", nullable = false)
    private Integer stockFisico;

    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado;

    // Columna calculada en la base de datos: no se debe intentar insertar ni actualizar en JPA
    @Column(name = "stock_disponible", insertable = false, updatable = false)
    private Integer stockDisponible;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    // --- CONSTRUCTORES ---
    public StockInventario() {
    }

    public StockInventario(Articulo articulo, Integer stockFisico, Integer stockReservado, LocalDateTime ultimaActualizacion) {
        this.articulo = articulo;
        this.stockFisico = stockFisico;
        this.stockReservado = stockReservado;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getInventarioId() {
        return inventarioId;
    }

    public void setInventarioId(Integer inventarioId) {
        this.inventarioId = inventarioId;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Integer getStockFisico() {
        return stockFisico;
    }

    public void setStockFisico(Integer stockFisico) {
        this.stockFisico = stockFisico;
    }

    public Integer getStockReservado() {
        return stockReservado;
    }

    public void setStockReservado(Integer stockReservado) {
        this.stockReservado = stockReservado;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }
}

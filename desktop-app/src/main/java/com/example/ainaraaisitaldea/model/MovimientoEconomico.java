package com.example.ainaraaisitaldea.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MovimientoEconomico {

    public enum Tipo {
        INGRESO("Ingreso"),
        GASTO("Gasto");

        private final String descripcion;

        Tipo(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    private Integer idMovimiento;
    private Tipo tipo;
    private String concepto;
    private LocalDate fecha;
    private BigDecimal cantidad;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    // Constructor vacío
    public MovimientoEconomico() {
        this.cantidad = BigDecimal.ZERO;
        this.fecha = LocalDate.now();
    }

    // Constructor completo
    public MovimientoEconomico(Integer idMovimiento, Tipo tipo, String concepto, LocalDate fecha,
                               BigDecimal cantidad, String observaciones) {
        this.idMovimiento = idMovimiento;
        this.tipo = tipo;
        this.concepto = concepto;
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    // Métodos auxiliares
    public String getCantidadFormateada() {
        return String.format("%.2f €", cantidad.doubleValue());
    }

    public String getTipoConSigno() {
        return tipo == Tipo.INGRESO ? "+" : "-";
    }

    @Override
    public String toString() {
        return String.format("%s - %s: %s (%.2f €)",
                fecha.toString(),
                tipo.getDescripcion(),
                concepto,
                cantidad.doubleValue());
    }
}
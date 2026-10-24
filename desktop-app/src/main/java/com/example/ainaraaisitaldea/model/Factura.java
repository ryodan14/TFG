package com.example.ainaraaisitaldea.model;

import com.example.ainaraaisitaldea.model.enums.EstadoFactura;
import com.example.ainaraaisitaldea.model.enums.TipoDocumento;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Factura {
    private Integer idFactura;
    private String numeroFactura;
    private String cliente;
    private String nifCliente;
    private String direccionCliente;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String concepto;
    private Double baseImponible;
    private Double ivaPorcentaje;
    private Double ivaCantidad;
    private Double total;
    private EstadoFactura estado;
    private TipoDocumento tipo;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    // Constructores
    public Factura() {
        this.estado = EstadoFactura.PENDIENTE;
        this.tipo = TipoDocumento.FACTURA;
        this.ivaPorcentaje = 21.0;
        this.fechaEmision = LocalDate.now();
    }

    // Getters y Setters
    public Integer getIdFactura() { return idFactura; }
    public void setIdFactura(Integer idFactura) { this.idFactura = idFactura; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getNifCliente() { return nifCliente; }
    public void setNifCliente(String nifCliente) { this.nifCliente = nifCliente; }

    public String getDireccionCliente() { return direccionCliente; }
    public void setDireccionCliente(String direccionCliente) { this.direccionCliente = direccionCliente; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public Double getBaseImponible() { return baseImponible; }
    public void setBaseImponible(Double baseImponible) {
        this.baseImponible = baseImponible;
        calcularTotal();
    }

    public Double getIvaPorcentaje() { return ivaPorcentaje; }
    public void setIvaPorcentaje(Double ivaPorcentaje) {
        this.ivaPorcentaje = ivaPorcentaje;
        calcularTotal();
    }

    public Double getIvaCantidad() { return ivaCantidad; }
    public void setIvaCantidad(Double ivaCantidad) { this.ivaCantidad = ivaCantidad; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public EstadoFactura getEstado() { return estado; }
    public void setEstado(EstadoFactura estado) { this.estado = estado; }

    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    // Método de cálculo automático
    public void calcularTotal() {
        if (baseImponible != null && ivaPorcentaje != null) {
            this.ivaCantidad = baseImponible * (ivaPorcentaje / 100.0);
            this.total = baseImponible + ivaCantidad;
        }
    }
}
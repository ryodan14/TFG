package com.example.ainaraaisitaldea.model.enums;

public enum EstadoFactura {
    PAGADA("Pagada"),
    PENDIENTE("Pendiente"),
    ANULADA("Anulada");

    private final String descripcion;

    EstadoFactura(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

package com.example.ainaraaisitaldea.model.enums;

public enum TipoDocumento {
    FACTURA("Factura"),
    PRESUPUESTO("Presupuesto");

    private final String descripcion;

    TipoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

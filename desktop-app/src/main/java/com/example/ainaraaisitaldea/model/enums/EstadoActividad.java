package com.example.ainaraaisitaldea.model.enums;

public enum EstadoActividad {
    PROGRAMADA("Programada"),
    REALIZADA("Realizada"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoActividad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
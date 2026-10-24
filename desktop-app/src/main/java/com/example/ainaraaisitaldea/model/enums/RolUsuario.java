package com.example.ainaraaisitaldea.model.enums;

public enum RolUsuario {
    ADMINISTRADOR("Administrador"),
    MONITOR("Monitor Básico");

    private final String descripcion;

    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
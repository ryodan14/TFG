package com.example.ainaraaisitaldea.model.enums;

public enum TipoActividad {
    LUDOTEKA("Ludoteka"),
    GAZTE_LOKALA("Gazte Lokala"),
    HINCHABLES("Hinchables"),
    OTRAS("Otras Actividades");

    private final String descripcion;

    TipoActividad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
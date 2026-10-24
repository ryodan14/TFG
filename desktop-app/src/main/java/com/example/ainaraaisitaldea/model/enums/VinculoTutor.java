package com.example.ainaraaisitaldea.model.enums;

public enum VinculoTutor {
    MADRE("Madre"),
    PADRE("Padre"),
    TUTOR_LEGAL("Tutor Legal");

    private final String descripcion;

    VinculoTutor(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
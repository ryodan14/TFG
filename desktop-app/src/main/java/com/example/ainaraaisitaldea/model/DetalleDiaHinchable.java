package com.example.ainaraaisitaldea.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa el detalle de un día específico en una salida de hinchables
 * Permite tener múltiples días con horarios e importes independientes
 */
public class DetalleDiaHinchable {

    private Integer idDetalle;
    private Integer idActividad;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Double importe;
    private String observaciones;

    // Constructor vacío
    public DetalleDiaHinchable() {
        this.importe = 0.0;
    }

    // Constructor con fecha
    public DetalleDiaHinchable(LocalDate fecha) {
        this();
        this.fecha = fecha;
    }

    // Constructor completo
    public DetalleDiaHinchable(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Double importe) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.importe = importe != null ? importe : 0.0;
    }

    // Getters y Setters
    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Integer getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(Integer idActividad) {
        this.idActividad = idActividad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Double getImporte() {
        return importe != null ? importe : 0.0;
    }

    public void setImporte(Double importe) {
        this.importe = importe;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /**
     * Obtiene el horario formateado (HH:MM - HH:MM)
     */
    public String getHorarioFormateado() {
        if (horaInicio == null || horaFin == null) {
            return "Sin horario";
        }
        return String.format("%02d:%02d - %02d:%02d",
                horaInicio.getHour(), horaInicio.getMinute(),
                horaFin.getHour(), horaFin.getMinute());
    }

    /**
     * Verifica si el día tiene datos completos
     */
    public boolean tieneHorario() {
        return horaInicio != null && horaFin != null;
    }

    /**
     * Verifica si el día tiene importe
     */
    public boolean tieneImporte() {
        return importe != null && importe > 0;
    }

    /**
     * Verifica si el día está completamente vacío
     */
    public boolean estaVacio() {
        return !tieneHorario() && !tieneImporte() &&
                (observaciones == null || observaciones.trim().isEmpty());
    }

    @Override
    public String toString() {
        return String.format("Día %s - %s - %.2f€",
                fecha, getHorarioFormateado(), getImporte());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleDiaHinchable that = (DetalleDiaHinchable) o;
        return idDetalle != null && idDetalle.equals(that.idDetalle);
    }

    @Override
    public int hashCode() {
        return idDetalle != null ? idDetalle.hashCode() : 0;
    }
}
package com.example.ainaraaisitaldea.model;

import com.example.ainaraaisitaldea.model.enums.TipoActividad;
import com.example.ainaraaisitaldea.model.enums.EstadoActividad;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Actividad {
    private Integer idActividad;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private TipoActividad tipo;
    private String nombre;
    private String descripcion;
    private String observaciones;
    private Double dineroRecibido;
    private Double dineroPagadoMonitores;
    private EstadoActividad estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private List<Usuario> monitores;

    // Constructores
    public Actividad() {
        this.estado = EstadoActividad.PROGRAMADA;
        this.dineroRecibido = 0.0;
        this.dineroPagadoMonitores = 0.0;
        this.monitores = new ArrayList<>();
    }

    // Getters y Setters
    public Integer getIdActividad() { return idActividad; }
    public void setIdActividad(Integer idActividad) { this.idActividad = idActividad; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public TipoActividad getTipo() { return tipo; }
    public void setTipo(TipoActividad tipo) { this.tipo = tipo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Double getDineroRecibido() { return dineroRecibido; }
    public void setDineroRecibido(Double dineroRecibido) { this.dineroRecibido = dineroRecibido; }

    public Double getDineroPagadoMonitores() { return dineroPagadoMonitores; }
    public void setDineroPagadoMonitores(Double dineroPagadoMonitores) { this.dineroPagadoMonitores = dineroPagadoMonitores; }

    public Double getBeneficioNeto() {
        return dineroRecibido - dineroPagadoMonitores;
    }

    public EstadoActividad getEstado() { return estado; }
    public void setEstado(EstadoActividad estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public List<Usuario> getMonitores() { return monitores; }
    public void setMonitores(List<Usuario> monitores) { this.monitores = monitores; }

    public String getHorarioFormateado() {
        return horaInicio + " - " + horaFin;
    }
}
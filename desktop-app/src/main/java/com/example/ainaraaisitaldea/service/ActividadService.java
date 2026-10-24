package com.example.ainaraaisitaldea.service;

import com.example.ainaraaisitaldea.dao.ActividadDAO;
import com.example.ainaraaisitaldea.model.Actividad;
import com.example.ainaraaisitaldea.util.ValidationUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de actividades
 */
public class ActividadService {

    private final ActividadDAO actividadDAO;

    public ActividadService() {
        this.actividadDAO = new ActividadDAO();
    }

    /**
     * Crear actividad con validaciones
     */
    public Actividad crearActividad(Actividad actividad) throws SQLException {

        // Validaciones
        if (actividad.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }

        if (actividad.getHoraInicio() != null || actividad.getHoraFin() != null) {
            if (actividad.getHoraInicio().isAfter(actividad.getHoraFin())) {
                throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
            }
        }

        if (!ValidationUtil.noVacio(actividad.getNombre())) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (actividad.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de actividad es obligatorio");
        }

        return actividadDAO.crear(actividad);
    }

    /**
     * Actualizar actividad
     */
    public void actualizarActividad(Actividad actividad) throws SQLException {

        if (actividad.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }

        if (actividad.getHoraInicio() != null || actividad.getHoraFin() != null) {
            if (actividad.getHoraInicio().isAfter(actividad.getHoraFin())) {
                throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
            }
        }

        if (!ValidationUtil.noVacio(actividad.getNombre())) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        actividadDAO.actualizar(actividad);
    }

    /**
     * Listar actividades por fecha
     */
    public List<Actividad> listarPorFecha(LocalDate fecha) throws SQLException {
        return actividadDAO.listarPorFecha(fecha);
    }

    /**
     * Listar actividades por mes
     */
    public List<Actividad> listarPorMes(int anio, int mes) throws SQLException {
        return actividadDAO.listarPorMes(anio, mes);
    }

    /**
     * Eliminar actividad
     */
    public void eliminarActividad(Integer idActividad) throws SQLException {
        actividadDAO.eliminar(idActividad);
    }

    /**
     * Lista las próximas N actividades desde hoy en adelante
     * @param limite Número máximo de actividades a retornar
     * @return Lista de actividades futuras ordenadas por fecha
     */
    public List<Actividad> listarProximasActividades(int limite) {
        try {
            LocalDate hoy = LocalDate.now();

            // Obtener todas las actividades y filtrar las futuras
            List<Actividad> todasActividades = listarPorFecha(hoy); // Tu método existente

            return todasActividades.stream()
                    .filter(act -> !act.getFecha().isBefore(hoy)) // Solo futuras o de hoy
                    .sorted((a1, a2) -> a1.getFecha().compareTo(a2.getFecha())) // Ordenar por fecha
                    .limit(limite) // Limitar cantidad
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error al obtener próximas actividades: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Retornar lista vacía en caso de error
        }
    }



}
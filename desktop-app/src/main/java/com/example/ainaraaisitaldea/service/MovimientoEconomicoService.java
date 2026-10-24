package com.example.ainaraaisitaldea.service;

import com.example.ainaraaisitaldea.dao.MovimientoEconomicoDAO;
import com.example.ainaraaisitaldea.model.MovimientoEconomico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MovimientoEconomicoService {

    private final MovimientoEconomicoDAO movimientoDAO;

    public MovimientoEconomicoService() {
        this.movimientoDAO = new MovimientoEconomicoDAO();
    }

    // Guardar nuevo movimiento
    public boolean guardar(MovimientoEconomico movimiento) {
        if (!validarMovimiento(movimiento)) {
            return false;
        }
        return movimientoDAO.crear(movimiento);
    }

    // Actualizar movimiento existente
    public boolean actualizar(MovimientoEconomico movimiento) {
        if (!validarMovimiento(movimiento)) {
            return false;
        }
        return movimientoDAO.actualizar(movimiento);
    }

    // Eliminar movimiento
    public boolean eliminar(int idMovimiento) {
        return movimientoDAO.eliminar(idMovimiento);
    }



    // Listar todos los movimientos
    public List<MovimientoEconomico> listar() {
        return movimientoDAO.listar();
    }

    // Listar movimientos por rango de fechas
    public List<MovimientoEconomico> listarPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return movimientoDAO.listarPorFechas(fechaInicio, fechaFin);
    }

    // Listar últimos N movimientos
    public List<MovimientoEconomico> listarUltimos(int limite) {
        return movimientoDAO.listarUltimos(limite);
    }

    // Obtener totales solo de movimientos económicos
    public Map<String, BigDecimal> calcularTotales() {
        return movimientoDAO.calcularTotales();
    }

    // Obtener totales consolidados (movimientos + hinchables + facturas)
    public Map<String, BigDecimal> calcularTotalesConsolidados() {
        return movimientoDAO.calcularTotalesConsolidados();
    }

    // Validar movimiento antes de guardar
    private boolean validarMovimiento(MovimientoEconomico movimiento) {
        if (movimiento.getTipo() == null) {
            System.err.println("El tipo de movimiento es obligatorio");
            return false;
        }

        if (movimiento.getConcepto() == null || movimiento.getConcepto().trim().isEmpty()) {
            System.err.println("El concepto es obligatorio");
            return false;
        }

        if (movimiento.getFecha() == null) {
            System.err.println("La fecha es obligatoria");
            return false;
        }

        if (movimiento.getCantidad() == null || movimiento.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("La cantidad debe ser mayor a 0");
            return false;
        }

        return true;
    }
}
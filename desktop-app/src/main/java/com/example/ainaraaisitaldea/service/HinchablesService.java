package com.example.ainaraaisitaldea.service;

import com.example.ainaraaisitaldea.database.ConnectionFactory;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para cálculos y resúmenes económicos
 */
public class HinchablesService {

    private final ConnectionFactory connectionFactory;

    public HinchablesService() {
        this.connectionFactory = ConnectionFactory.getInstance();
    }

    /**
     * Obtener resumen económico de hinchables por mes
     */
    public Map<String, Object> obtenerResumenHinchablesMes(int anio, int mes) throws SQLException {
        String sql = "SELECT " +
                "COUNT(*) as total_actividades, " +
                "COALESCE(SUM(dinero_recibido), 0) as total_ingresos, " +
                "COALESCE(SUM(dinero_pagado_monitores), 0) as total_gastos, " +
                "COALESCE(SUM(dinero_recibido - dinero_pagado_monitores), 0) as beneficio_neto " +
                "FROM actividades " +
                "WHERE tipo = 'HINCHABLES' AND YEAR(fecha) = ? AND MONTH(fecha) = ?";

        Map<String, Object> resumen = new HashMap<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, anio);
            stmt.setInt(2, mes);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    resumen.put("totalActividades", rs.getInt("total_actividades"));
                    resumen.put("totalIngresos", rs.getDouble("total_ingresos"));
                    resumen.put("totalGastos", rs.getDouble("total_gastos"));
                    resumen.put("beneficioNeto", rs.getDouble("beneficio_neto"));
                }
            }
        }

        return resumen;
    }

    /**
     * Obtener resumen económico anual
     */
    public Map<String, Object> obtenerResumenHinchablesAnual(int anio) throws SQLException {
        String sql = "SELECT " +
                "COUNT(*) as total_actividades, " +
                "COALESCE(SUM(dinero_recibido), 0) as total_ingresos, " +
                "COALESCE(SUM(dinero_pagado_monitores), 0) as total_gastos, " +
                "COALESCE(SUM(dinero_recibido - dinero_pagado_monitores), 0) as beneficio_neto " +
                "FROM actividades " +
                "WHERE tipo = 'HINCHABLES' AND YEAR(fecha) = ?";

        Map<String, Object> resumen = new HashMap<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, anio);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    resumen.put("totalActividades", rs.getInt("total_actividades"));
                    resumen.put("totalIngresos", rs.getDouble("total_ingresos"));
                    resumen.put("totalGastos", rs.getDouble("total_gastos"));
                    resumen.put("beneficioNeto", rs.getDouble("beneficio_neto"));
                }
            }
        }

        return resumen;
    }
}

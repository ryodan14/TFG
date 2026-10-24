package com.example.ainaraaisitaldea.dao;

import com.example.ainaraaisitaldea.model.MovimientoEconomico;
import com.example.ainaraaisitaldea.database.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovimientoEconomicoDAO {

    // Crear nuevo movimiento
    public boolean crear(MovimientoEconomico movimiento) {
        String sql = "INSERT INTO movimientos_economicos " +
                "(tipo, concepto, fecha, cantidad, observaciones) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, movimiento.getTipo().name());
            stmt.setString(2, movimiento.getConcepto());
            stmt.setDate(3, Date.valueOf(movimiento.getFecha()));
            stmt.setBigDecimal(4, movimiento.getCantidad());
            stmt.setString(5, movimiento.getObservaciones());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        movimiento.setIdMovimiento(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear movimiento: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Listar todos los movimientos
    public List<MovimientoEconomico> listar() {
        List<MovimientoEconomico> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_economicos ORDER BY fecha DESC, id_movimiento DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                movimientos.add(mapearMovimiento(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar movimientos: " + e.getMessage());
            e.printStackTrace();
        }

        return movimientos;
    }

    // Obtener movimientos por rango de fechas
    public List<MovimientoEconomico> listarPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<MovimientoEconomico> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_economicos " +
                "WHERE fecha BETWEEN ? AND ? " +
                "ORDER BY fecha DESC, id_movimiento DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fechaInicio));
            stmt.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearMovimiento(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar movimientos por fechas: " + e.getMessage());
            e.printStackTrace();
        }

        return movimientos;
    }

    // Obtener últimos N movimientos
    public List<MovimientoEconomico> listarUltimos(int limite) {
        List<MovimientoEconomico> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_economicos " +
                "ORDER BY fecha DESC, id_movimiento DESC LIMIT ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearMovimiento(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar últimos movimientos: " + e.getMessage());
            e.printStackTrace();
        }

        return movimientos;
    }

    // Calcular totales (ingresos, gastos, beneficio)
    public Map<String, BigDecimal> calcularTotales() {
        Map<String, BigDecimal> totales = new HashMap<>();
        totales.put("ingresos", BigDecimal.ZERO);
        totales.put("gastos", BigDecimal.ZERO);
        totales.put("beneficio", BigDecimal.ZERO);

        String sql = "SELECT tipo, SUM(cantidad) as total FROM movimientos_economicos GROUP BY tipo";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                BigDecimal total = rs.getBigDecimal("total");

                if ("INGRESO".equals(tipo)) {
                    totales.put("ingresos", total);
                } else if ("GASTO".equals(tipo)) {
                    totales.put("gastos", total);
                }
            }

            BigDecimal ingresos = totales.get("ingresos");
            BigDecimal gastos = totales.get("gastos");
            totales.put("beneficio", ingresos.subtract(gastos));

        } catch (SQLException e) {
            System.err.println("Error al calcular totales: " + e.getMessage());
            e.printStackTrace();
        }

        return totales;
    }

    // Calcular totales consolidados (incluyendo hinchables y facturas)
    public Map<String, BigDecimal> calcularTotalesConsolidados() {
        Map<String, BigDecimal> totales = new HashMap<>();
        totales.put("ingresos", BigDecimal.ZERO);
        totales.put("gastos", BigDecimal.ZERO);
        totales.put("beneficio", BigDecimal.ZERO);

        String sql = "SELECT SUM(ingreso) as total_ingresos, SUM(gasto) as total_gastos " +
                "FROM v_economia_consolidada";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                BigDecimal ingresos = rs.getBigDecimal("total_ingresos");
                BigDecimal gastos = rs.getBigDecimal("total_gastos");

                if (ingresos != null) totales.put("ingresos", ingresos);
                if (gastos != null) totales.put("gastos", gastos);

                BigDecimal beneficio = totales.get("ingresos").subtract(totales.get("gastos"));
                totales.put("beneficio", beneficio);
            }

        } catch (SQLException e) {
            System.err.println("Error al calcular totales consolidados: " + e.getMessage());
            e.printStackTrace();
        }

        return totales;
    }

    // Actualizar movimiento
    public boolean actualizar(MovimientoEconomico movimiento) {
        String sql = "UPDATE movimientos_economicos SET " +
                "tipo = ?, concepto = ?, fecha = ?, cantidad = ?, observaciones = ? " +
                "WHERE id_movimiento = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, movimiento.getTipo().name());
            stmt.setString(2, movimiento.getConcepto());
            stmt.setDate(3, Date.valueOf(movimiento.getFecha()));
            stmt.setBigDecimal(4, movimiento.getCantidad());
            stmt.setString(5, movimiento.getObservaciones());
            stmt.setInt(6, movimiento.getIdMovimiento());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar movimiento: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Eliminar movimiento
    public boolean eliminar(int idMovimiento) {
        String sql = "DELETE FROM movimientos_economicos WHERE id_movimiento = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMovimiento);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar movimiento: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Mapear ResultSet a MovimientoEconomico
    private MovimientoEconomico mapearMovimiento(ResultSet rs) throws SQLException {
        MovimientoEconomico movimiento = new MovimientoEconomico();

        movimiento.setIdMovimiento(rs.getInt("id_movimiento"));
        movimiento.setTipo(MovimientoEconomico.Tipo.valueOf(rs.getString("tipo")));
        movimiento.setConcepto(rs.getString("concepto"));
        movimiento.setFecha(rs.getDate("fecha").toLocalDate());
        movimiento.setCantidad(rs.getBigDecimal("cantidad"));
        movimiento.setObservaciones(rs.getString("observaciones"));

        Timestamp creacion = rs.getTimestamp("fecha_creacion");
        if (creacion != null) {
            movimiento.setFechaCreacion(creacion.toLocalDateTime());
        }

        Timestamp modificacion = rs.getTimestamp("fecha_modificacion");
        if (modificacion != null) {
            movimiento.setFechaModificacion(modificacion.toLocalDateTime());
        }

        return movimiento;
    }
}
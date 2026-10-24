package com.example.ainaraaisitaldea.dao;

import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.model.DetalleDiaHinchable;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar los detalles de días de salidas de hinchables
 */
public class DetalleDiaHinchableDAO {

    /**
     * Obtener todos los detalles de días de una actividad
     */
    public List<DetalleDiaHinchable> obtenerPorActividad(int idActividad) throws SQLException {
        List<DetalleDiaHinchable> detalles = new ArrayList<>();
        String sql = "SELECT * FROM detalles_dias_hinchables WHERE id_actividad = ? ORDER BY fecha";

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idActividad);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapearDetalle(rs));
                }
            }
        }
        return detalles;
    }

    /**
     * Insertar un nuevo detalle de día
     */
    public int insertar(DetalleDiaHinchable detalle) throws SQLException {
        String sql = "INSERT INTO detalles_dias_hinchables " +
                "(id_actividad, fecha, hora_inicio, hora_fin, importe, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, detalle.getIdActividad());
            stmt.setDate(2, Date.valueOf(detalle.getFecha()));

            if (detalle.getHoraInicio() != null) {
                stmt.setTime(3, Time.valueOf(detalle.getHoraInicio()));
            } else {
                stmt.setNull(3, Types.TIME);
            }

            if (detalle.getHoraFin() != null) {
                stmt.setTime(4, Time.valueOf(detalle.getHoraFin()));
            } else {
                stmt.setNull(4, Types.TIME);
            }

            if (detalle.getImporte() != null) {
                stmt.setDouble(5, detalle.getImporte());
            } else {
                stmt.setNull(5, Types.DECIMAL);
            }

            stmt.setString(6, detalle.getObservaciones());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Actualizar un detalle de día existente
     */
    public boolean actualizar(DetalleDiaHinchable detalle) throws SQLException {
        String sql = "UPDATE detalles_dias_hinchables SET " +
                "fecha = ?, hora_inicio = ?, hora_fin = ?, importe = ?, observaciones = ? " +
                "WHERE id_detalle = ?";

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(detalle.getFecha()));

            if (detalle.getHoraInicio() != null) {
                stmt.setTime(2, Time.valueOf(detalle.getHoraInicio()));
            } else {
                stmt.setNull(2, Types.TIME);
            }

            if (detalle.getHoraFin() != null) {
                stmt.setTime(3, Time.valueOf(detalle.getHoraFin()));
            } else {
                stmt.setNull(3, Types.TIME);
            }

            if (detalle.getImporte() != null) {
                stmt.setDouble(4, detalle.getImporte());
            } else {
                stmt.setNull(4, Types.DECIMAL);
            }

            stmt.setString(5, detalle.getObservaciones());
            stmt.setInt(6, detalle.getIdDetalle());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Eliminar un detalle de día
     */
    public boolean eliminar(int idDetalle) throws SQLException {
        String sql = "DELETE FROM detalles_dias_hinchables WHERE id_detalle = ?";

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDetalle);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Eliminar todos los detalles de una actividad
     */
    public boolean eliminarPorActividad(int idActividad) throws SQLException {
        String sql = "DELETE FROM detalles_dias_hinchables WHERE id_actividad = ?";

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idActividad);
            return stmt.executeUpdate() >= 0; // Puede ser 0 si no hay detalles
        }
    }

    /**
     * Insertar múltiples detalles de días en una sola transacción
     */
    public boolean insertarVarios(List<DetalleDiaHinchable> detalles) throws SQLException {
        if (detalles == null || detalles.isEmpty()) {
            return true;
        }

        String sql = "INSERT INTO detalles_dias_hinchables " +
                "(id_actividad, fecha, hora_inicio, hora_fin, importe, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (DetalleDiaHinchable detalle : detalles) {
                    stmt.setInt(1, detalle.getIdActividad());
                    stmt.setDate(2, Date.valueOf(detalle.getFecha()));

                    if (detalle.getHoraInicio() != null) {
                        stmt.setTime(3, Time.valueOf(detalle.getHoraInicio()));
                    } else {
                        stmt.setNull(3, Types.TIME);
                    }

                    if (detalle.getHoraFin() != null) {
                        stmt.setTime(4, Time.valueOf(detalle.getHoraFin()));
                    } else {
                        stmt.setNull(4, Types.TIME);
                    }

                    if (detalle.getImporte() != null) {
                        stmt.setDouble(5, detalle.getImporte());
                    } else {
                        stmt.setNull(5, Types.DECIMAL);
                    }

                    stmt.setString(6, detalle.getObservaciones());
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Calcular el importe total de todos los días de una actividad
     */
    public double calcularImporteTotal(int idActividad) throws SQLException {
        String sql = "SELECT COALESCE(SUM(importe), 0) FROM detalles_dias_hinchables WHERE id_actividad = ?";

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idActividad);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    /**
     * Mapear ResultSet a objeto DetalleDiaHinchable
     */
    private DetalleDiaHinchable mapearDetalle(ResultSet rs) throws SQLException {
        DetalleDiaHinchable detalle = new DetalleDiaHinchable();

        detalle.setIdDetalle(rs.getInt("id_detalle"));
        detalle.setIdActividad(rs.getInt("id_actividad"));

        Date fecha = rs.getDate("fecha");
        if (fecha != null) {
            detalle.setFecha(fecha.toLocalDate());
        }

        Time horaInicio = rs.getTime("hora_inicio");
        if (horaInicio != null) {
            detalle.setHoraInicio(horaInicio.toLocalTime());
        }

        Time horaFin = rs.getTime("hora_fin");
        if (horaFin != null) {
            detalle.setHoraFin(horaFin.toLocalTime());
        }

        Double importe = rs.getDouble("importe");
        if (!rs.wasNull()) {
            detalle.setImporte(importe);
        }

        detalle.setObservaciones(rs.getString("observaciones"));

        return detalle;
    }
}
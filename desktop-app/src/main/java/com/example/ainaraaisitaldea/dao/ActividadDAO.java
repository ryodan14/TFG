package com.example.ainaraaisitaldea.dao;

import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.model.Actividad;
import com.example.ainaraaisitaldea.model.Usuario;
import com.example.ainaraaisitaldea.model.enums.TipoActividad;
import com.example.ainaraaisitaldea.model.enums.EstadoActividad;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActividadDAO {

    private final ConnectionFactory connectionFactory;
    private final UsuarioDAO usuarioDAO;

    public ActividadDAO() {
        this.connectionFactory = ConnectionFactory.getInstance();
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Crear nueva actividad
     */
    public Actividad crear(Actividad actividad) throws SQLException {
        String sql = "INSERT INTO actividades (fecha, hora_inicio, hora_fin, tipo, nombre, " +
                "descripcion, observaciones, dinero_recibido, dinero_pagado_monitores, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setDate(1, Date.valueOf(actividad.getFecha()));
                if (actividad.getHoraInicio() != null) {
                    stmt.setTime(2, Time.valueOf(actividad.getHoraInicio()));
                    stmt.setTime(3, Time.valueOf(actividad.getHoraFin()));
                } else {
                    stmt.setNull(2, Types.TIME);
                    stmt.setNull(3, Types.TIME);
                }
                stmt.setString(4, actividad.getTipo().name());
                stmt.setString(5, actividad.getNombre());
                stmt.setString(6, actividad.getDescripcion());
                stmt.setString(7, actividad.getObservaciones());
                stmt.setDouble(8, actividad.getDineroRecibido());
                stmt.setDouble(9, actividad.getDineroPagadoMonitores());
                stmt.setString(10, actividad.getEstado().name());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        actividad.setIdActividad(rs.getInt(1));
                    }
                }
            }

            // Asignar monitores
            if (actividad.getMonitores() != null && !actividad.getMonitores().isEmpty()) {
                asignarMonitores(conn, actividad.getIdActividad(), actividad.getMonitores());
            }

            conn.commit();
            return actividad;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Actualizar actividad
     */
    public void actualizar(Actividad actividad) throws SQLException {
        String sql = "UPDATE actividades SET fecha = ?, hora_inicio = ?, hora_fin = ?, " +
                "tipo = ?, nombre = ?, descripcion = ?, observaciones = ?, " +
                "dinero_recibido = ?, dinero_pagado_monitores = ?, estado = ? " +
                "WHERE id_actividad = ?";

        Connection conn = null;
        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(actividad.getFecha()));
                if (actividad.getHoraInicio() != null) {
                    stmt.setTime(2, Time.valueOf(actividad.getHoraInicio()));
                    stmt.setTime(3, Time.valueOf(actividad.getHoraFin()));
                } else {
                    stmt.setNull(2, Types.TIME);
                    stmt.setNull(3, Types.TIME);
                }
                stmt.setString(4, actividad.getTipo().name());
                stmt.setString(5, actividad.getNombre());
                stmt.setString(6, actividad.getDescripcion());
                stmt.setString(7, actividad.getObservaciones());
                stmt.setDouble(8, actividad.getDineroRecibido());
                stmt.setDouble(9, actividad.getDineroPagadoMonitores());
                stmt.setString(10, actividad.getEstado().name());
                stmt.setInt(11, actividad.getIdActividad());

                stmt.executeUpdate();
            }

            // Actualizar monitores
            eliminarMonitores(conn, actividad.getIdActividad());
            if (actividad.getMonitores() != null && !actividad.getMonitores().isEmpty()) {
                asignarMonitores(conn, actividad.getIdActividad(), actividad.getMonitores());
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Listar actividades por fecha
     */
    public List<Actividad> listarPorFecha(LocalDate fecha) throws SQLException {
        String sql = "SELECT * FROM actividades WHERE fecha = ? ORDER BY hora_inicio";
        List<Actividad> actividades = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Actividad act = mapearActividad(rs);
                    cargarMonitores(act);
                    actividades.add(act);
                }
            }
        }

        return actividades;
    }

    /**
     * Listar actividades por mes
     */
    public List<Actividad> listarPorMes(int anio, int mes) throws SQLException {
        String sql = "SELECT * FROM actividades WHERE YEAR(fecha) = ? AND MONTH(fecha) = ? " +
                "ORDER BY fecha, hora_inicio";
        List<Actividad> actividades = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, anio);
            stmt.setInt(2, mes);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Actividad act = mapearActividad(rs);
                    cargarMonitores(act);
                    actividades.add(act);
                }
            }
        }

        return actividades;
    }

    /**
     * Eliminar actividad
     */
    public void eliminar(Integer idActividad) throws SQLException {
        String sql = "DELETE FROM actividades WHERE id_actividad = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idActividad);
            stmt.executeUpdate();
        }
    }

    /**
     * Asignar monitores a actividad
     */
    private void asignarMonitores(Connection conn, Integer idActividad, List<Usuario> monitores)
            throws SQLException {
        String sql = "INSERT INTO monitores_actividades (id_actividad, id_usuario) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Usuario monitor : monitores) {
                stmt.setInt(1, idActividad);
                stmt.setInt(2, monitor.getIdUsuario());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Eliminar monitores de actividad
     */
    private void eliminarMonitores(Connection conn, Integer idActividad) throws SQLException {
        String sql = "DELETE FROM monitores_actividades WHERE id_actividad = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idActividad);
            stmt.executeUpdate();
        }
    }

    /**
     * Cargar monitores de una actividad
     */
    private void cargarMonitores(Actividad actividad) throws SQLException {
        String sql = "SELECT u.* FROM usuarios u " +
                "INNER JOIN monitores_actividades ma ON u.id_usuario = ma.id_usuario " +
                "WHERE ma.id_actividad = ?";

        List<Usuario> monitores = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, actividad.getIdActividad());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setNombreCompleto(rs.getString("nombre_completo"));
                    monitores.add(usuario);
                }
            }
        }

        actividad.setMonitores(monitores);
    }

    private Actividad mapearActividad(ResultSet rs) throws SQLException {
        Actividad actividad = new Actividad();
        actividad.setIdActividad(rs.getInt("id_actividad"));
        actividad.setFecha(rs.getDate("fecha").toLocalDate());

        // 👇 Comprobación de null en horas
        Time inicio = rs.getTime("hora_inicio");
        actividad.setHoraInicio(inicio != null ? inicio.toLocalTime() : null);

        Time fin = rs.getTime("hora_fin");
        actividad.setHoraFin(fin != null ? fin.toLocalTime() : null);

        actividad.setTipo(TipoActividad.valueOf(rs.getString("tipo")));
        actividad.setNombre(rs.getString("nombre"));
        actividad.setDescripcion(rs.getString("descripcion"));
        actividad.setObservaciones(rs.getString("observaciones"));
        actividad.setDineroRecibido(rs.getDouble("dinero_recibido"));
        actividad.setDineroPagadoMonitores(rs.getDouble("dinero_pagado_monitores"));
        actividad.setEstado(EstadoActividad.valueOf(rs.getString("estado")));

        // 👇 Comprobaciones de timestamps NULL
        Timestamp tsCreacion = rs.getTimestamp("fecha_creacion");
        actividad.setFechaCreacion(tsCreacion != null ? tsCreacion.toLocalDateTime() : null);

        Timestamp tsModificacion = rs.getTimestamp("fecha_modificacion");
        actividad.setFechaModificacion(tsModificacion != null ? tsModificacion.toLocalDateTime() : null);

        return actividad;
    }

}
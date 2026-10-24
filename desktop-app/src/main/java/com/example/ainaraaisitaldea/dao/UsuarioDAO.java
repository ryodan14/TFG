package com.example.ainaraaisitaldea.dao;

import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.model.Usuario;
import com.example.ainaraaisitaldea.model.enums.RolUsuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private final ConnectionFactory connectionFactory;

    public UsuarioDAO() {
        this.connectionFactory = ConnectionFactory.getInstance();
    }

    /**
     * Busca usuario por username para login
     */
    public Usuario buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND activo = TRUE";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * Crear nuevo usuario
     */
    public Usuario crear(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (username, password, nombre_completo, " + // Cambiado password_hash a password
                "email, telefono, rol, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getPasswordHash()); // Ahora es contraseña plana
            stmt.setString(3, usuario.getNombreCompleto());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getTelefono());
            stmt.setString(6, usuario.getRol().name());
            stmt.setBoolean(7, usuario.isActivo());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1));
                }
            }

            return usuario;
        }
    }

    /**
     * Actualizar usuario
     */
    public void actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre_completo = ?, email = ?, " +
                "telefono = ?, rol = ?, activo = ? WHERE id_usuario = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombreCompleto());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefono());
            stmt.setString(4, usuario.getRol().name());
            stmt.setBoolean(5, usuario.isActivo());
            stmt.setInt(6, usuario.getIdUsuario());

            stmt.executeUpdate();
        }
    }

    /**
     * Actualizar contraseña
     */
    public void actualizarPassword(Integer idUsuario, String nuevaPassword) throws SQLException {
        String sql = "UPDATE usuarios SET password = ? WHERE id_usuario = ?"; // Cambiado password_hash a password

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevaPassword); // Contraseña plana
            stmt.setInt(2, idUsuario);

            stmt.executeUpdate();
        }
    }

    /**
     * Listar todos los usuarios
     */
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY nombre_completo";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }

        return usuarios;
    }

    /**
     * Listar monitores activos
     */
    public List<Usuario> listarMonitores() throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE rol = 'MONITOR' AND activo = TRUE " +
                "ORDER BY nombre_completo";
        List<Usuario> monitores = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                monitores.add(mapearUsuario(rs));
            }
        }

        return monitores;
    }

    /**
     * Eliminar (desactivar) usuario
     */
    public void eliminar(Integer idUsuario) throws SQLException {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE id_usuario = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();
        }
    }

    /**
     * Mapea ResultSet a objeto Usuario
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("password")); // Cambiado de password_hash a password
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setEmail(rs.getString("email"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setRol(RolUsuario.valueOf(rs.getString("rol")));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        usuario.setFechaModificacion(rs.getTimestamp("fecha_modificacion").toLocalDateTime());
        return usuario;
    }
}
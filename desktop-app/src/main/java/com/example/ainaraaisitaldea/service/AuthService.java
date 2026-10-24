package com.example.ainaraaisitaldea.service;

import com.example.ainaraaisitaldea.dao.UsuarioDAO;
import com.example.ainaraaisitaldea.model.Usuario;
import com.example.ainaraaisitaldea.util.PasswordUtil;
import com.example.ainaraaisitaldea.util.SessionManager;
import java.sql.SQLException;

/**
 * Servicio de autenticación (versión sin encriptación)
 */
public class AuthService {

    private final UsuarioDAO usuarioDAO;
    private final SessionManager sessionManager;

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Iniciar sesión
     * @return Usuario si las credenciales son correctas, null en caso contrario
     */
    public Usuario login(String username, String password) throws SQLException {

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return null;
        }

        Usuario usuario = usuarioDAO.buscarPorUsername(username.trim());

        if (usuario == null) {
            return null;
        }

        if (!usuario.isActivo()) {
            throw new SQLException("Usuario desactivado");
        }

        // Verificar contraseña (comparación directa)
        if (PasswordUtil.checkPassword(password, usuario.getPasswordHash())) {
            sessionManager.iniciarSesion(usuario);
            return usuario;
        }

        return null;
    }

    /**
     * Cerrar sesión
     */
    public void logout() {
        sessionManager.cerrarSesion();
    }

    /**
     * Obtener usuario actual
     */
    public Usuario getUsuarioActual() {
        return sessionManager.getUsuarioActual();
    }

    /**
     * Verificar si hay sesión activa
     */
    public boolean haySesionActiva() {
        return sessionManager.haySesionActiva();
    }
}
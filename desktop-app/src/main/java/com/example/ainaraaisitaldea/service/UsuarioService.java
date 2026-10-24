package com.example.ainaraaisitaldea.service;
import com.example.ainaraaisitaldea.dao.UsuarioDAO;
import com.example.ainaraaisitaldea.model.Usuario;
import com.example.ainaraaisitaldea.util.PasswordUtil;
import com.example.ainaraaisitaldea.util.ValidationUtil;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de gestión de usuarios
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Crear nuevo usuario con validaciones
     */
    public Usuario crearUsuario(Usuario usuario, String passwordPlain) throws SQLException {

        // Validaciones
        if (!ValidationUtil.noVacio(usuario.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }

        if (!ValidationUtil.noVacio(usuario.getNombreCompleto())) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }

        if (!PasswordUtil.esPasswordSegura(passwordPlain)) {
            throw new IllegalArgumentException(PasswordUtil.getMensajeRequisitos());
        }

        if (usuario.getEmail() != null && !usuario.getEmail().isEmpty() &&
                !ValidationUtil.validarEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Email inválido");
        }

        // Verificar si username ya existe
        if (usuarioDAO.buscarPorUsername(usuario.getUsername()) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        // Guardar contraseña sin encriptar
        usuario.setPasswordHash(passwordPlain); // Contraseña plana

        // Crear usuario
        return usuarioDAO.crear(usuario);
    }

    /**
     * Actualizar usuario
     */
    public void actualizarUsuario(Usuario usuario) throws SQLException {

        if (!ValidationUtil.noVacio(usuario.getNombreCompleto())) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }

        if (usuario.getEmail() != null && !usuario.getEmail().isEmpty() &&
                !ValidationUtil.validarEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Email inválido");
        }

        usuarioDAO.actualizar(usuario);
    }

    /**
     * Cambiar contraseña
     */
    public void cambiarPassword(Integer idUsuario, String nuevaPassword) throws SQLException {

        if (!PasswordUtil.esPasswordSegura(nuevaPassword)) {
            throw new IllegalArgumentException(PasswordUtil.getMensajeRequisitos());
        }

        // Guardar contraseña sin encriptar
        usuarioDAO.actualizarPassword(idUsuario, nuevaPassword);
    }

    /**
     * Listar todos los usuarios
     */
    public List<Usuario> listarTodos() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    /**
     * Listar monitores
     */
    public List<Usuario> listarMonitores() throws SQLException {
        return usuarioDAO.listarMonitores();
    }

    /**
     * Eliminar usuario
     */
    public void eliminarUsuario(Integer idUsuario) throws SQLException {
        usuarioDAO.eliminar(idUsuario);
    }
}


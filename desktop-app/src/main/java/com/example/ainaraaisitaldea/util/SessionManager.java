package com.example.ainaraaisitaldea.util;

import com.example.ainaraaisitaldea.model.Usuario;
import javafx.stage.Stage;

/**
 * Gestiona la sesión del usuario autenticado en memoria
 */
public class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;
    private static Stage mainStage;


    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void toggleFullScreen() {
        if (mainStage != null) {
            mainStage.setFullScreen(!mainStage.isFullScreen());
        }
    }

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
        System.out.println("Sesión iniciada para: " + usuario.getUsername() +
                " (" + usuario.getRol() + ")");
    }

    public void cerrarSesion() {
        if (usuarioActual != null) {
            System.out.println("Sesión cerrada para: " + usuarioActual.getUsername());
        }
        this.usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public boolean esAdministrador() {
        return usuarioActual != null &&
                "ADMINISTRADOR".equals(usuarioActual.getRol().name());
    }

    public boolean esMonitor() {
        return usuarioActual != null &&
                "MONITOR".equals(usuarioActual.getRol().name());
    }
}
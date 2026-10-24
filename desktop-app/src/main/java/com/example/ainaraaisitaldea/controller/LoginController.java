package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Usuario;
import com.example.ainaraaisitaldea.service.AuthService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        // Configurar listeners si es necesario
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campos vacíos", "Por favor ingrese usuario y contraseña");
            return;
        }

        try {
            Usuario usuario = authService.login(username, password);

            if (usuario != null) {
                // Login exitoso - redirigir a dashboard
                cargarDashboard();
            } else {
                AlertUtil.mostrarError("Error de autenticación",
                        "Usuario o contraseña incorrectos");
                txtPassword.clear();
            }

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
            e.printStackTrace();
        }
    }

    private void cargarDashboard() {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/dashboard.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: No se encontró /fxml/dashboard.fxml");
                AlertUtil.mostrarInfo("Login Exitoso",
                        "¡Bienvenido al sistema!\n\n" +
                                "El archivo dashboard.fxml no está disponible.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) txtUsername.getScene().getWindow();

            // ✅ Crear la Scene SIN tamaño fijo
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Ainara Aisi Taldea");
            stage.setMaximized(true); // ✅ Maximiza después de asignar la escena
            // stage.centerOnScreen(); ← innecesario si está maximizada

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo cargar el dashboard: " + e.getMessage());
        }
    }
}
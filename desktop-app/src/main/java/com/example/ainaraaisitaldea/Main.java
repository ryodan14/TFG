package com.example.ainaraaisitaldea;

/**
 * Clase principal de la aplicación JavaFX
 */

import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Verificar conexión a BD
            if (!verificarConexionBD()) {
                mostrarErrorConexion();
                return;
            }

            // Cargar pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 500, 600);



            // Propiedades de la ventana
            primaryStage.setTitle("Ainara Aisi Taldea");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();

            // ICONO EN TODAS LOS FORMS
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/ainara.png")));

            primaryStage.setTitle("Ainara Aisi Taldea - Inicio de sesión");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
        }
    }

    /**
     * Verifica conexión a base de datos
     */
    private boolean verificarConexionBD() {
        try {
            ConnectionFactory factory = ConnectionFactory.getInstance();
            return factory.testConnection();

        } catch (Exception e) {
            System.err.println("Error al verificar conexión: " + e.getMessage());
            return false;
        }
    }

    /**
     * Error de conexión
     */
    private void mostrarErrorConexion() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Error de Conexión");
        alert.setHeaderText("No se pudo conectar a la base de datos");
        alert.setContentText(
                "Por favor verifique:\n" +
                        "1. MySQL/MariaDB está en ejecución\n" +
                        "2. La base de datos 'gestion_asociacion' existe\n" +
                        "3. Las credenciales en database.properties son correctas\n"
        );
        alert.showAndWait();
    }

    @Override
    public void stop() {
        System.out.println("Aplicacion cerrada");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

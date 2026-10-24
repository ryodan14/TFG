package com.example.ainaraaisitaldea.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidad para gestionar la navegación entre vistas
 */
public class NavigationUtil {

    private static final Logger LOGGER = Logger.getLogger(NavigationUtil.class.getName());
    private static Stage primaryStage;

    /**
     * Establecer el Stage principal de la aplicación
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Obtener el Stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Cargar una vista en el Stage principal
     */
    public static void cargarVista(String rutaFxml, String titulo) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary Stage no ha sido configurado");
        }

        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(rutaFxml));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle(titulo);
        primaryStage.setScene(scene);
        primaryStage.show();

        LOGGER.log(Level.INFO, "Vista cargada: {0}", rutaFxml);
    }

    /**
     * Cargar vista obteniendo el Stage desde un evento
     */
    public static void cargarVista(ActionEvent event, String rutaFxml, String titulo) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(rutaFxml));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle(titulo);
        stage.setScene(scene);
        stage.show();

        LOGGER.log(Level.INFO, "Vista cargada: {0}", rutaFxml);
    }

    /**
     * Cargar vista obteniendo el Stage desde un Node
     */
    public static void cargarVista(Node node, String rutaFxml, String titulo) throws IOException {
        Stage stage = (Stage) node.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(rutaFxml));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle(titulo);
        stage.setScene(scene);
        stage.show();

        LOGGER.log(Level.INFO, "Vista cargada: {0}", rutaFxml);
    }

    /**
     * VOLVER AL DASHBOARD - Este es el método clave que falta
     */
    public static void volverAlDashboard(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.setTitle("Ainara Aisi Taldea");
            stage.setMaximized(true);
            stage.show();

            LOGGER.info("Retornando al Dashboard");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al volver al dashboard", e);
            AlertUtil.mostrarError("Error de Navegación",
                    "No se pudo regresar al panel de control: " + e.getMessage());
        }
    }

    /**
     * VOLVER AL DASHBOARD desde un Node
     */
    public static void volverAlDashboard(Node node) {
        try {
            Stage stage = (Stage) node.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.setTitle("Ainara Aisi Taldea");
            stage.setMaximized(true);
            stage.show();

            LOGGER.info("Retornando al Dashboard");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al volver al dashboard", e);
            AlertUtil.mostrarError("Error de Navegación",
                    "No se pudo regresar al panel de control: " + e.getMessage());
        }
    }

    /**
     * Cargar vista y obtener su controlador
     */
    public static <T> T cargarVistaConControlador(String rutaFxml, String titulo) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary Stage no ha sido configurado");
        }

        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(rutaFxml));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle(titulo);
        primaryStage.setScene(scene);
        primaryStage.show();

        LOGGER.log(Level.INFO, "Vista cargada con controlador: {0}", rutaFxml);
        return loader.getController();
    }

    /**
     * Obtener FXMLLoader para una ruta específica
     */
    public static FXMLLoader getFXMLLoader(String rutaFxml) {
        return new FXMLLoader(NavigationUtil.class.getResource(rutaFxml));
    }
}
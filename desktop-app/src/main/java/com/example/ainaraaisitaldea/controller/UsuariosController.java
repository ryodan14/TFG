package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Usuario;
import com.example.ainaraaisitaldea.service.UsuarioService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import com.example.ainaraaisitaldea.util.NavigationUtil;
import com.example.ainaraaisitaldea.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;

public class UsuariosController {

    @FXML
    private TableView<Usuario> tableUsuarios;

    @FXML
    private TableColumn<Usuario, String> colUsername;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private TableColumn<Usuario, String> colEmail;

    @FXML
    private TableColumn<Usuario, String> colRol;

    @FXML
    private TableColumn<Usuario, String> colEstado;

    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnResetear;

    @FXML
    private Button btnDesactivar;

    private final UsuarioService usuarioService;
    private final SessionManager sessionManager;
    private ObservableList<Usuario> listaObservable;

    private static final String FXML_BASE = "/fxml/";

    public UsuariosController() {
        this.usuarioService = new UsuarioService();
        this.sessionManager = SessionManager.getInstance();
    }

    @FXML
    private void initialize() {
        // CONTROL DE PERMISOS - Solo ADMINISTRADOR puede acceder
        if (!sessionManager.esAdministrador()) {
            mostrarAccesoDenegado();
            return;
        }

        configurarTabla();
        cargarDatos();
    }

    private void mostrarAccesoDenegado() {
        // Deshabilitar todos los controles
        tableUsuarios.setDisable(true);
        btnNuevo.setDisable(true);
        btnEditar.setDisable(true);
        btnResetear.setDisable(true);
        btnDesactivar.setDisable(true);

        // Mostrar mensaje
        AlertUtil.mostrarAdvertencia("Acceso Denegado",
                "Solo los ADMINISTRADORES pueden acceder al módulo de usuarios.\n\n" +
                        "Su rol actual: " + sessionManager.getUsuarioActual().getRol().getDescripcion());

        // Agregar un mensaje visible en la tabla
        Label lblError = new Label("⛔ ACCESO DENEGADO\n\n" +
                "Este módulo es solo para ADMINISTRADORES");
        lblError.setStyle("-fx-font-size: 18px; -fx-text-fill: red; -fx-font-weight: bold;");
        tableUsuarios.setPlaceholder(lblError);
    }

    private void configurarTabla() {
        colUsername.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUsername()));

        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombreCompleto()));

        colEmail.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEmail() != null ?
                        data.getValue().getEmail() : ""));

        colRol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRol().getDescripcion()));

        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isActivo() ? "Activo" : "Inactivo"));

        // Colorear estado
        colEstado.setCellFactory(column -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Activo")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Colorear rol
        colRol.setCellFactory(column -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Administrador")) {
                        setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #757575;");
                    }
                }
            }
        });

        listaObservable = FXCollections.observableArrayList();
        tableUsuarios.setItems(listaObservable);
    }

    private void cargarDatos() {
        try {
            List<Usuario> usuarios = usuarioService.listarTodos();
            listaObservable.clear();
            listaObservable.addAll(usuarios);
        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    @FXML
    private void nuevoUsuario(ActionEvent event) {
        abrirFormulario(null);
    }

    @FXML
    private void editarUsuario(ActionEvent event) {
        Usuario seleccionado = tableUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione un usuario de la tabla");
            return;
        }

        abrirFormulario(seleccionado);
    }

    @FXML
    private void resetearPassword(ActionEvent event) {
        Usuario seleccionado = tableUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione un usuario de la tabla");
            return;
        }

        // Crear diálogo para nueva contraseña
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Resetear Contraseña");
        dialog.setHeaderText("Resetear contraseña para: " + seleccionado.getUsername());
        dialog.setContentText("Nueva contraseña:");

        dialog.showAndWait().ifPresent(nuevaPassword -> {
            if (nuevaPassword.trim().isEmpty()) {
                AlertUtil.mostrarAdvertencia("Validación", "La contraseña no puede estar vacía");
                return;
            }

            try {
                usuarioService.cambiarPassword(seleccionado.getIdUsuario(), nuevaPassword);
                AlertUtil.mostrarExito("Contraseña actualizada correctamente para " +
                        seleccionado.getUsername());
            } catch (IllegalArgumentException e) {
                AlertUtil.mostrarAdvertencia("Validación", e.getMessage());
            } catch (Exception e) {
                AlertUtil.mostrarErrorBD(e);
            }
        });
    }

    @FXML
    private void desactivarUsuario(ActionEvent event) {
        Usuario seleccionado = tableUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione un usuario de la tabla");
            return;
        }

        // No permitir desactivar al usuario actual
        if (seleccionado.getIdUsuario().equals(sessionManager.getUsuarioActual().getIdUsuario())) {
            AlertUtil.mostrarAdvertencia("Operación no permitida",
                    "No puede desactivar su propio usuario");
            return;
        }

        String accion = seleccionado.isActivo() ? "desactivar" : "activar";
        boolean confirmado = AlertUtil.mostrarConfirmacion(
                "Confirmar " + accion,
                "¿Está seguro de " + accion + " el usuario " + seleccionado.getUsername() + "?");

        if (confirmado) {
            try {
                seleccionado.setActivo(!seleccionado.isActivo());
                usuarioService.actualizarUsuario(seleccionado);
                AlertUtil.mostrarExito("Usuario " +
                        (seleccionado.isActivo() ? "activado" : "desactivado") + " correctamente");
                cargarDatos();
            } catch (Exception e) {
                AlertUtil.mostrarErrorBD(e);
            }
        }
    }

    private void abrirFormulario(Usuario usuario) {
        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "usuarios_form.fxml");

            if (fxmlUrl == null) {
                AlertUtil.mostrarAdvertencia("Formulario no disponible",
                        "El formulario de usuario no se encontró");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            UsuariosFormController controller = loader.getController();
            controller.setUsuario(usuario);
            controller.setControllerPadre(this);

            Stage stage = new Stage();
            stage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void volverDashboard(ActionEvent event) {
        NavigationUtil.volverAlDashboard(event);
    }

    public void refrescarDatos() {
        cargarDatos();
    }
}
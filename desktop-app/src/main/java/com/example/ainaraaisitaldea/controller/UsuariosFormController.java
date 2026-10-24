package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Usuario;
import com.example.ainaraaisitaldea.model.enums.RolUsuario;
import com.example.ainaraaisitaldea.service.UsuarioService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import com.example.ainaraaisitaldea.util.PasswordUtil;
import com.example.ainaraaisitaldea.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UsuariosFormController {

    @FXML
    private Label lblTitulo;

    @FXML
    private TextField txtUsername;

    @FXML
    private Label lblPassword;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblPasswordHint;

    @FXML
    private TextField txtNombreCompleto;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelefono;

    @FXML
    private ComboBox<RolUsuario> cmbRol;

    @FXML
    private CheckBox chkActivo;

    private final UsuarioService usuarioService;
    private Usuario usuarioActual;
    private UsuariosController controllerPadre;

    public UsuariosFormController() {
        this.usuarioService = new UsuarioService();
    }

    @FXML
    private void initialize() {
        // Cargar roles
        cmbRol.getItems().addAll(RolUsuario.values());
        cmbRol.setValue(RolUsuario.MONITOR);

        // Configurar conversión a String
        cmbRol.setConverter(new javafx.util.StringConverter<RolUsuario>() {
            @Override
            public String toString(RolUsuario rol) {
                return rol == null ? "" : rol.getDescripcion();
            }

            @Override
            public RolUsuario fromString(String string) {
                return null;
            }
        });
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;

        if (usuario != null) {
            lblTitulo.setText("Editar Usuario");

            txtUsername.setText(usuario.getUsername());
            txtUsername.setEditable(false); // No permitir cambiar username
            txtNombreCompleto.setText(usuario.getNombreCompleto());
            txtEmail.setText(usuario.getEmail());
            txtTelefono.setText(usuario.getTelefono());
            cmbRol.setValue(usuario.getRol());
            chkActivo.setSelected(usuario.isActivo());

            // Ocultar campo contraseña al editar
            lblPassword.setVisible(false);
            lblPassword.setManaged(false);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            lblPasswordHint.setVisible(false);
            lblPasswordHint.setManaged(false);
        }
    }

    public void setControllerPadre(UsuariosController controller) {
        this.controllerPadre = controller;
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            // Validaciones
            if (txtUsername.getText().trim().isEmpty()) {
                AlertUtil.mostrarAdvertencia("Validación", "El nombre de usuario es obligatorio");
                return;
            }

            if (txtNombreCompleto.getText().trim().isEmpty()) {
                AlertUtil.mostrarAdvertencia("Validación", "El nombre completo es obligatorio");
                return;
            }

            if (cmbRol.getValue() == null) {
                AlertUtil.mostrarAdvertencia("Validación", "El rol es obligatorio");
                return;
            }

            // Validar contraseña solo al crear
            if (usuarioActual == null) {
                if (txtPassword.getText().trim().isEmpty()) {
                    AlertUtil.mostrarAdvertencia("Validación", "La contraseña es obligatoria");
                    return;
                }

                if (!PasswordUtil.esPasswordSegura(txtPassword.getText())) {
                    AlertUtil.mostrarAdvertencia("Validación", PasswordUtil.getMensajeRequisitos());
                    return;
                }
            }

            // Validar email si se proporciona
            if (!txtEmail.getText().trim().isEmpty() &&
                    !ValidationUtil.validarEmail(txtEmail.getText().trim())) {
                AlertUtil.mostrarAdvertencia("Validación", "El email no es válido");
                return;
            }

            // Validar teléfono si se proporciona
            if (!txtTelefono.getText().trim().isEmpty() &&
                    !ValidationUtil.validarTelefono(txtTelefono.getText().trim())) {
                AlertUtil.mostrarAdvertencia("Validación", "El teléfono no es válido");
                return;
            }

            Usuario usuario = usuarioActual != null ? usuarioActual : new Usuario();

            usuario.setUsername(txtUsername.getText().trim());
            usuario.setNombreCompleto(txtNombreCompleto.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setTelefono(ValidationUtil.normalizarTelefono(txtTelefono.getText().trim()));
            usuario.setRol(cmbRol.getValue());
            usuario.setActivo(chkActivo.isSelected());

            if (usuarioActual == null) {
                // Crear usuario nuevo
                String password = txtPassword.getText().trim();
                usuarioService.crearUsuario(usuario, password);
                AlertUtil.mostrarExito("Usuario creado correctamente\n" +
                        "Username: " + usuario.getUsername() + "\n" +
                        "Rol: " + usuario.getRol().getDescripcion());
            } else {
                // Actualizar usuario existente
                usuarioService.actualizarUsuario(usuario);
                AlertUtil.mostrarExito("Usuario actualizado correctamente");
            }

            if (controllerPadre != null) {
                controllerPadre.refrescarDatos();
            }

            cerrar(event);

        } catch (IllegalArgumentException e) {
            AlertUtil.mostrarAdvertencia("Validación", e.getMessage());
        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrar(ActionEvent event) {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }
}
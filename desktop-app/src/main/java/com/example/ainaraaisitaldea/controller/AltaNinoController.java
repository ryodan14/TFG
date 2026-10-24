package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Nino;
import com.example.ainaraaisitaldea.model.Tutor;
import com.example.ainaraaisitaldea.service.NinoService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AltaNinoController {

    @FXML private Label lblNombreCompleto;
    @FXML private Label lblEdadCurso;

    // Campos del Niño
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrimerApellido;
    @FXML private TextField txtSegundoApellido;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtColegio;
    @FXML private ComboBox<String> cbCurso;
    @FXML private CheckBox chkAlergias;
    @FXML private CheckBox chkPuedeSalirSolo;
    @FXML private CheckBox chkAutorizacionFotos;
    @FXML private TextArea txtObservacionesNino;

    // Campos del Tutor
    @FXML private TextField txtNombreTutor;
    @FXML private TextField txtPrimerApellidoTutor;
    @FXML private TextField txtSegundoApellidoTutor;
    @FXML private TextField txtDniTutor;
    @FXML private TextField txtCorreoTutor;
    @FXML private TextField txtTelefonoTutor;
    @FXML private TextField txtDireccionTutor;
    @FXML private TextField txtMunicipioTutor;
    @FXML private TextField txtCodigoPostalTutor;
    @FXML private ComboBox<String> cbVinculo;
    @FXML private TextArea txtObservacionesTutor;

    private final NinoService ninoService;

    // Edicion
    private Nino ninoEnEdicion;

    public AltaNinoController() {
        this.ninoService = new NinoService();
    }

    @FXML
    private void initialize() {
        configurarCombos();
        configurarValidaciones();
    }

    private void configurarCombos() {
        // Configurar combo de cursos
        cbCurso.getItems().addAll(
                "1º Primaria", "2º Primaria", "3º Primaria", "4º Primaria",
                "5º Primaria", "6º Primaria",
                "1º ESO", "2º ESO", "3º ESO", "4º ESO"
        );

        // Configurar combo de vínculos
        cbVinculo.getItems().addAll("Madre", "Padre", "Tutor Legal");
    }

    private void configurarValidaciones() {
        if (lblEdadCurso.getText() == null || lblEdadCurso.getText().isEmpty()) {
            lblEdadCurso.setDisable(true);
        }

        // Validar solo números en teléfono
        txtTelefonoTutor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtTelefonoTutor.setText(oldValue);
            }
        });

        // Validar solo números en código postal
        txtCodigoPostalTutor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtCodigoPostalTutor.setText(oldValue);
            }
        });

        // Limitar longitud de DNI
        txtDniTutor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 9) {
                txtDniTutor.setText(oldValue);
            }
        });
    }

    @FXML
    private void onCursoSeleccionado(ActionEvent event) {
        String cursoSeleccionado = cbCurso.getValue();

        // Si el curso NO permite salir solo → deshabilitar y desmarcar
        if (cursoSeleccionado != null && !Nino.cursoPermiteSalirSolo(cursoSeleccionado)) {
            chkPuedeSalirSolo.setSelected(false);
            chkPuedeSalirSolo.setDisable(true);
        } else {
            chkPuedeSalirSolo.setDisable(false);

            // Si es 5º Primaria → NO puede estar marcado
            if (cursoSeleccionado.equals("5º Primaria")) {
                chkPuedeSalirSolo.setSelected(false);
            }
            // Si es un curso superior (ej: 6º) → marcar automáticamente
            else if (Nino.cursoPermiteSalirSolo(cursoSeleccionado)) {
                chkPuedeSalirSolo.setSelected(true);
            }
        }
    }

    private boolean validarCampos() {
        // Validar campos del niño
        if (txtNombre.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El nombre del niño/a es obligatorio");
            txtNombre.requestFocus();
            return false;
        }

        if (txtPrimerApellido.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El primer apellido es obligatorio");
            txtPrimerApellido.requestFocus();
            return false;
        }

        if (txtSegundoApellido.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El segundo apellido es obligatorio");
            txtSegundoApellido.requestFocus();
            return false;
        }

        if (dpFechaNacimiento.getValue() == null) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "La fecha de nacimiento es obligatoria");
            dpFechaNacimiento.requestFocus();
            return false;
        }

        if (dpFechaNacimiento.getValue().isAfter(LocalDate.now())) {
            AlertUtil.mostrarAdvertencia("Fecha inválida", "La fecha de nacimiento no puede ser futura");
            dpFechaNacimiento.requestFocus();
            return false;
        }

        if (txtColegio.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El colegio/instituto es obligatorio");
            txtColegio.requestFocus();
            return false;
        }

        if (cbCurso.getValue() == null) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "Debe seleccionar un curso");
            cbCurso.requestFocus();
            return false;
        }



        // Validar campos del tutor
        if (txtNombreTutor.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El nombre del tutor/a es obligatorio");
            txtNombreTutor.requestFocus();
            return false;
        }

        if (txtPrimerApellidoTutor.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El primer apellido del tutor/a es obligatorio");
            txtPrimerApellidoTutor.requestFocus();
            return false;
        }

        if (txtSegundoApellidoTutor.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El segundo apellido del tutor/a es obligatorio");
            txtSegundoApellidoTutor.requestFocus();
            return false;
        }

        if (txtDniTutor.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El DNI del tutor/a es obligatorio");
            txtDniTutor.requestFocus();
            return false;
        }

        if (txtCorreoTutor.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El correo electrónico es obligatorio");
            txtCorreoTutor.requestFocus();
            return false;
        }

        if (txtTelefonoTutor.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El teléfono es obligatorio");
            txtTelefonoTutor.requestFocus();
            return false;
        }

        if (txtDireccionTutor.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "La dirección es obligatoria");
            txtDireccionTutor.requestFocus();
            return false;
        }

        if (txtMunicipioTutor.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El municipio es obligatorio");
            txtMunicipioTutor.requestFocus();
            return false;
        }

        if (txtCodigoPostalTutor.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "El código postal es obligatorio");
            txtCodigoPostalTutor.requestFocus();
            return false;
        }

        if (cbVinculo.getValue() == null) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "Debe seleccionar el vínculo con el niño/a");
            cbVinculo.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void cancelar(ActionEvent event) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }


    public void cargarParaEdicion(Nino ninoCompleto) {
        if (ninoCompleto == null) return;

        // Guardar referencia al niño para la edición
        this.ninoEnEdicion = ninoCompleto;

        lblNombreCompleto.setText(ninoCompleto.getNombreCompleto());
        lblEdadCurso.setText(ninoCompleto.getEdad() + " años - " + ninoCompleto.getCurso());

        // Cargar datos del niño
        txtNombre.setText(ninoCompleto.getNombre());
        txtPrimerApellido.setText(ninoCompleto.getPrimerApellido());
        txtSegundoApellido.setText(ninoCompleto.getSegundoApellido());
        dpFechaNacimiento.setValue(ninoCompleto.getFechaNacimiento());
        txtColegio.setText(ninoCompleto.getColegioInstituto());
        cbCurso.setValue(ninoCompleto.getCurso());
        chkAlergias.setSelected(ninoCompleto.isTieneAlergias());
        chkPuedeSalirSolo.setSelected(ninoCompleto.isPuedeSalirSolo());
        chkAutorizacionFotos.setSelected(ninoCompleto.isAutorizacionFotos());
        txtObservacionesNino.setText(ninoCompleto.getObservaciones());
        cbVinculo.setValue(ninoCompleto.getVinculoTutor().name());

        // Cargar datos del tutor
        Tutor tutor = ninoCompleto.getTutor();
        if (tutor != null) {
            txtNombreTutor.setText(tutor.getNombre());
            txtPrimerApellidoTutor.setText(tutor.getPrimerApellido());
            txtSegundoApellidoTutor.setText(tutor.getSegundoApellido());
            txtDniTutor.setText(tutor.getDni());
            txtCorreoTutor.setText(tutor.getCorreo());
            txtTelefonoTutor.setText(tutor.getTelefono());
            txtDireccionTutor.setText(tutor.getDireccion());
            txtMunicipioTutor.setText(tutor.getMunicipio());
            txtCodigoPostalTutor.setText(tutor.getCodigoPostal());
            txtObservacionesTutor.setText(
                    ninoCompleto.getTutor() != null ? ninoCompleto.getTutor().getObservaciones() : ""
            );
        }

        // Verificar restricción de puede_salir_solo
        onCursoSeleccionado(null);
    }

    // Método guardar modificado para soportar edición
    @FXML
    private void guardar(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        try {
            boolean esEdicion = (ninoEnEdicion != null);

            // Crear o actualizar objeto Niño
            Nino nino = esEdicion ? ninoEnEdicion : new Nino();

            nino.setNombre(txtNombre.getText().trim());
            nino.setPrimerApellido(txtPrimerApellido.getText().trim());
            nino.setSegundoApellido(txtSegundoApellido.getText().trim());
            nino.setFechaNacimiento(dpFechaNacimiento.getValue());
            nino.setColegioInstituto(txtColegio.getText().trim());
            nino.setCurso(cbCurso.getValue());
            nino.setTieneAlergias(chkAlergias.isSelected());
            nino.setPuedeSalirSolo(chkPuedeSalirSolo.isSelected());
            nino.setAutorizacionFotos(chkAutorizacionFotos.isSelected());
            nino.setObservaciones(
                    txtObservacionesNino != null && txtObservacionesNino.getText() != null
                            ? txtObservacionesNino.getText().trim()
                            : ""
            );
            nino.setVinculoTutor(Nino.VinculoTutor.valueOf(cbVinculo.getValue()));

            // Verificar restricción de puede_salir_solo
            if (nino.isPuedeSalirSolo() && !Nino.cursoPermiteSalirSolo(nino.getCurso())) {
                AlertUtil.mostrarError("Error de validación",
                        "El niño/a no puede salir solo en el curso " + nino.getCurso() + ".\n" +
                                "Solo pueden salir solos a partir de 5º de Primaria.");
                return;
            }

            boolean guardado;

            if (esEdicion) {
                // EDICIÓN: Actualizar niño y tutor
                Tutor tutor = nino.getTutor();
                if (tutor == null) {
                    tutor = ninoService.obtenerTutorDeNino(nino.getIdTutor());
                }

                if (tutor != null) {
                    tutor.setNombre(txtNombreTutor.getText().trim());
                    tutor.setPrimerApellido(txtPrimerApellidoTutor.getText().trim());
                    tutor.setSegundoApellido(txtSegundoApellidoTutor.getText().trim());
                    tutor.setDni(txtDniTutor.getText().trim().toUpperCase());
                    tutor.setCorreo(txtCorreoTutor.getText().trim());
                    tutor.setTelefono(txtTelefonoTutor.getText().trim());
                    tutor.setDireccion(txtDireccionTutor.getText().trim());
                    tutor.setMunicipio(txtMunicipioTutor.getText().trim());
                    tutor.setCodigoPostal(txtCodigoPostalTutor.getText().trim());
                    tutor.setObservaciones(txtObservacionesTutor.getText());

                    guardado = ninoService.actualizarNinoYTutor(nino, tutor);
                } else {
                    AlertUtil.mostrarError("Error", "No se pudo cargar el tutor");
                    return;
                }

            } else {
                // CREACIÓN: Crear nuevo niño con tutor
                Tutor tutor = new Tutor();
                tutor.setNombre(txtNombreTutor.getText().trim());
                tutor.setPrimerApellido(txtPrimerApellidoTutor.getText().trim());
                tutor.setSegundoApellido(txtSegundoApellidoTutor.getText().trim());
                tutor.setDni(txtDniTutor.getText().trim().toUpperCase());
                tutor.setCorreo(txtCorreoTutor.getText().trim());
                tutor.setTelefono(txtTelefonoTutor.getText().trim());
                tutor.setDireccion(txtDireccionTutor.getText().trim());
                tutor.setMunicipio(txtMunicipioTutor.getText().trim());
                tutor.setCodigoPostal(txtCodigoPostalTutor.getText().trim());
                tutor.setObservaciones(txtObservacionesTutor.getText());

                guardado = ninoService.crearNinoConTutor(nino, tutor);
            }

            if (guardado) {
                String mensaje = esEdicion ? "actualizado" : "registrado";
                AlertUtil.mostrarInfo("Éxito",
                        "El niño/a " + nino.getNombreCompleto() + " ha sido " + mensaje + " correctamente.");
                cerrarVentana();
            } else {
                AlertUtil.mostrarError("Error",
                        "No se pudo guardar el niño/a. Por favor, revise los datos.");
            }

        } catch (Exception e) {
            System.err.println("Error al guardar: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error",
                    "Error al guardar: " + e.getMessage());
        }
    }
}
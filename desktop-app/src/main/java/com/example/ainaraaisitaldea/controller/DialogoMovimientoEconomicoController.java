package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.MovimientoEconomico;
import com.example.ainaraaisitaldea.service.MovimientoEconomicoService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DialogoMovimientoEconomicoController {

    @FXML private Label lblTitulo;
    @FXML private ComboBox<String> cbConcepto;
    @FXML private TextField txtConceptoPersonalizado;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtCantidad;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnGuardar;

    private final MovimientoEconomicoService movimientoService;
    private MovimientoEconomico.Tipo tipoMovimiento;
    private static final String OPCION_OTROS = "Otro (personalizado)";

    public DialogoMovimientoEconomicoController() {
        this.movimientoService = new MovimientoEconomicoService();
    }

    @FXML
    private void initialize() {
        // Establecer fecha actual por defecto
        dpFecha.setValue(LocalDate.now());

        // Cargar conceptos según el tipo (por defecto INGRESO)
        cargarConceptos(MovimientoEconomico.Tipo.INGRESO);

        // Validar que solo se ingresen números en cantidad
        txtCantidad.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.]\\d{0,2})?")) {
                txtCantidad.setText(oldValue);
            }
        });
    }

    /**
     * Método público para establecer el tipo de movimiento desde fuera
     */
    public void setTipoMovimiento(MovimientoEconomico.Tipo tipo) {
        this.tipoMovimiento = tipo;
        cargarConceptos(tipo);

        // Actualizar título y color del botón
        if (tipo == MovimientoEconomico.Tipo.INGRESO) {
            lblTitulo.setText("Registrar Ingreso");
            btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 10 25; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 6;");
        } else {
            lblTitulo.setText("Registrar Gasto");
            btnGuardar.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 10 25; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 6;");
        }
    }

    private void cargarConceptos(MovimientoEconomico.Tipo tipo) {
        List<String> conceptos = new ArrayList<>();

        if (tipo == MovimientoEconomico.Tipo.INGRESO) {
            // Conceptos de INGRESOS
            conceptos.add("Subvención Gobierno Vasco");
            conceptos.add("Subvención Diputación Bizkaia");
            conceptos.add("Subvención Ayuntamiento Barakaldo");
            conceptos.add("Donación");
        } else {
            // Conceptos de GASTOS
            conceptos.add("Furgoneta");
            conceptos.add("Combustible");
            conceptos.add("Hinchables");
            conceptos.add("Material");
            conceptos.add("Seguros");
            conceptos.add("Electricidad / Agua");
            conceptos.add("Internet / Teléfono");
            conceptos.add("Locales");
            conceptos.add("Monitorado");
        }

        // Añadir opción "Otro" al final
        conceptos.add(OPCION_OTROS);

        cbConcepto.getItems().setAll(conceptos);
    }

    @FXML
    private void onConceptoSeleccionado(ActionEvent event) {
        String seleccion = cbConcepto.getValue();

        if (OPCION_OTROS.equals(seleccion)) {
            // Mostrar campo de texto personalizado
            txtConceptoPersonalizado.setVisible(true);
            txtConceptoPersonalizado.setManaged(true);
            txtConceptoPersonalizado.requestFocus();
        } else {
            // Ocultar campo de texto personalizado
            txtConceptoPersonalizado.setVisible(false);
            txtConceptoPersonalizado.setManaged(false);
            txtConceptoPersonalizado.clear();
        }
    }

    @FXML
    private void guardar(ActionEvent event) {
        // Validar campos obligatorios
        if (!validarCampos()) {
            return;
        }

        try {
            // Crear objeto MovimientoEconomico
            MovimientoEconomico movimiento = new MovimientoEconomico();

            // Establecer tipo
            movimiento.setTipo(tipoMovimiento != null ? tipoMovimiento : MovimientoEconomico.Tipo.INGRESO);

            // Establecer concepto (personalizado o predefinido)
            String concepto;
            if (OPCION_OTROS.equals(cbConcepto.getValue())) {
                concepto = txtConceptoPersonalizado.getText().trim();
            } else {
                concepto = cbConcepto.getValue();
            }
            movimiento.setConcepto(concepto);

            // Establecer datos básicos
            movimiento.setFecha(dpFecha.getValue());
            movimiento.setCantidad(new BigDecimal(txtCantidad.getText().trim()));
            movimiento.setObservaciones(txtObservaciones.getText().trim());

            // Guardar en base de datos
            boolean guardado = movimientoService.guardar(movimiento);

            if (guardado) {
                String tipoTexto = movimiento.getTipo() == MovimientoEconomico.Tipo.INGRESO ? "Ingreso" : "Gasto";
                AlertUtil.mostrarInfo("Éxito",
                        tipoTexto + " registrado correctamente\n\n" +
                                "Concepto: " + movimiento.getConcepto() + "\n" +
                                "Cantidad: " + movimiento.getCantidadFormateada());

                cerrarDialogo();
            } else {
                AlertUtil.mostrarError("Error", "No se pudo guardar el movimiento");
            }

        } catch (NumberFormatException e) {
            AlertUtil.mostrarError("Error", "La cantidad debe ser un número válido");
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "Error al guardar el movimiento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validarCampos() {
        String concepto = null;

        if (cbConcepto.getValue() == null) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "Debe seleccionar un concepto");
            cbConcepto.requestFocus();
            return false;
        }

        if (OPCION_OTROS.equals(cbConcepto.getValue())) {
            if (txtConceptoPersonalizado.getText().trim().isEmpty()) {
                AlertUtil.mostrarAdvertencia("Campo requerido", "Debe escribir el concepto personalizado");
                txtConceptoPersonalizado.requestFocus();
                return false;
            }
            concepto = txtConceptoPersonalizado.getText().trim();
        } else {
            concepto = cbConcepto.getValue();
        }

        if (dpFecha.getValue() == null) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "Debe seleccionar una fecha");
            dpFecha.requestFocus();
            return false;
        }

        if (txtCantidad.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo requerido", "Debe ingresar una cantidad");
            txtCantidad.requestFocus();
            return false;
        }

        try {
            double cantidad = Double.parseDouble(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                AlertUtil.mostrarAdvertencia("Valor inválido", "La cantidad debe ser mayor a 0");
                txtCantidad.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtil.mostrarAdvertencia("Valor inválido", "La cantidad debe ser un número válido");
            txtCantidad.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void cancelar(ActionEvent event) {
        cerrarDialogo();
    }

    private void cerrarDialogo() {
        Stage stage = (Stage) cbConcepto.getScene().getWindow();
        stage.close();
    }
}
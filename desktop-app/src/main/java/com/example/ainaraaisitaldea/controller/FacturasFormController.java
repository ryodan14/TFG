package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Factura;
import com.example.ainaraaisitaldea.model.enums.EstadoFactura;
import com.example.ainaraaisitaldea.model.enums.TipoDocumento;
import com.example.ainaraaisitaldea.service.FacturaService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class FacturasFormController {

    @FXML
    private Label lblTitulo;

    @FXML
    private ToggleGroup grupoTipo;

    @FXML
    private RadioButton rbFactura;

    @FXML
    private RadioButton rbPresupuesto;

    @FXML
    private TextField txtNumero;

    @FXML
    private TextField txtCliente;

    @FXML
    private TextField txtNIF;

    @FXML
    private TextArea txtDireccion;

    @FXML
    private DatePicker dpFechaEmision;

    @FXML
    private DatePicker dpFechaVencimiento;

    @FXML
    private TextArea txtConcepto;

    @FXML
    private TextField txtBaseImponible;

    @FXML
    private TextField txtIVA;

    @FXML
    private Label lblIVACantidad;

    @FXML
    private Label lblTotal;

    @FXML
    private ComboBox<EstadoFactura> cmbEstado;

    @FXML
    private TextArea txtObservaciones;

    private final FacturaService facturaService;
    private Factura facturaActual;
    private FacturasController controllerPadre;

    public FacturasFormController() {
        this.facturaService = new FacturaService();
    }

    @FXML
    private void initialize() {
        dpFechaEmision.setValue(LocalDate.now());
        dpFechaVencimiento.setValue(LocalDate.now().plusDays(30));
        txtIVA.setText("21");

        // Cargar estados
        cmbEstado.getItems().addAll(EstadoFactura.values());
        cmbEstado.setValue(EstadoFactura.PENDIENTE);

        // Listeners para cálculo automático
        txtBaseImponible.textProperty().addListener((obs, old, nuevo) -> calcularTotal());
        txtIVA.textProperty().addListener((obs, old, nuevo) -> calcularTotal());

        // Listener para cambio de tipo
        grupoTipo.selectedToggleProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null && facturaActual == null) {
                actualizarTitulo();
            }
        });
    }

    private void actualizarTitulo() {
        if (rbFactura.isSelected()) {
            lblTitulo.setText("Nueva Factura");
        } else {
            lblTitulo.setText("Nuevo Presupuesto");
        }
    }

    public void setFactura(Factura factura) {
        this.facturaActual = factura;

        if (factura != null) {
            lblTitulo.setText("Editar " + factura.getTipo().getDescripcion());

            if (factura.getTipo() == TipoDocumento.FACTURA) {
                rbFactura.setSelected(true);
            } else {
                rbPresupuesto.setSelected(true);
            }

            txtNumero.setText(factura.getNumeroFactura());
            txtCliente.setText(factura.getCliente());
            txtNIF.setText(factura.getNifCliente());
            txtDireccion.setText(factura.getDireccionCliente());
            dpFechaEmision.setValue(factura.getFechaEmision());
            dpFechaVencimiento.setValue(factura.getFechaVencimiento());
            txtConcepto.setText(factura.getConcepto());
            txtBaseImponible.setText(String.valueOf(factura.getBaseImponible()));
            txtIVA.setText(String.valueOf(factura.getIvaPorcentaje()));
            cmbEstado.setValue(factura.getEstado());
            txtObservaciones.setText(factura.getObservaciones());

            calcularTotal();

            // Deshabilitar cambio de tipo al editar
            rbFactura.setDisable(true);
            rbPresupuesto.setDisable(true);
        }
    }

    public void setControllerPadre(FacturasController controller) {
        this.controllerPadre = controller;
    }

    private void calcularTotal() {
        try {
            double base = Double.parseDouble(txtBaseImponible.getText().replace(",", "."));
            double iva = Double.parseDouble(txtIVA.getText().replace(",", "."));

            double ivaCantidad = base * (iva / 100.0);
            double total = base + ivaCantidad;

            lblIVACantidad.setText(String.format("%.2f €", ivaCantidad));
            lblTotal.setText(String.format("%.2f €", total));

        } catch (NumberFormatException e) {
            lblIVACantidad.setText("0.00 €");
            lblTotal.setText("0.00 €");
        }
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            // Validaciones
            if (txtCliente.getText().trim().isEmpty()) {
                AlertUtil.mostrarAdvertencia("Validación", "El cliente es obligatorio");
                return;
            }

            if (dpFechaEmision.getValue() == null) {
                AlertUtil.mostrarAdvertencia("Validación", "La fecha de emisión es obligatoria");
                return;
            }

            if (txtConcepto.getText().trim().isEmpty()) {
                AlertUtil.mostrarAdvertencia("Validación", "El concepto es obligatorio");
                return;
            }

            if (txtBaseImponible.getText().trim().isEmpty()) {
                AlertUtil.mostrarAdvertencia("Validación", "La base imponible es obligatoria");
                return;
            }

            double base;
            try {
                base = Double.parseDouble(txtBaseImponible.getText().replace(",", "."));
                if (base <= 0) {
                    AlertUtil.mostrarAdvertencia("Validación", "La base imponible debe ser mayor que 0");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertUtil.mostrarAdvertencia("Validación", "La base imponible debe ser un número válido");
                return;
            }

            Factura factura = facturaActual != null ? facturaActual : new Factura();

            factura.setTipo(rbFactura.isSelected() ? TipoDocumento.FACTURA : TipoDocumento.PRESUPUESTO);
            factura.setCliente(txtCliente.getText().trim());
            factura.setNifCliente(txtNIF.getText().trim());
            factura.setDireccionCliente(txtDireccion.getText().trim());
            factura.setFechaEmision(dpFechaEmision.getValue());
            factura.setFechaVencimiento(dpFechaVencimiento.getValue());
            factura.setConcepto(txtConcepto.getText().trim());
            factura.setBaseImponible(base);
            factura.setIvaPorcentaje(Double.parseDouble(txtIVA.getText().replace(",", ".")));
            factura.setEstado(cmbEstado.getValue());
            factura.setObservaciones(txtObservaciones.getText().trim());

            factura.calcularTotal();

            if (facturaActual == null) {
                facturaService.crearFactura(factura);
                AlertUtil.mostrarExito(factura.getTipo().getDescripcion() + " creada correctamente\n" +
                        "Número: " + factura.getNumeroFactura());
            } else {
                facturaService.actualizarFactura(factura);
                AlertUtil.mostrarExito(factura.getTipo().getDescripcion() + " actualizada correctamente");
            }

            if (controllerPadre != null) {
                controllerPadre.refrescarDatos();
            }

            cerrar(event);

        } catch (NumberFormatException e) {
            AlertUtil.mostrarAdvertencia("Validación", "Los importes deben ser números válidos");
        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrar(ActionEvent event) {
        Stage stage = (Stage) txtCliente.getScene().getWindow();
        stage.close();
    }
}
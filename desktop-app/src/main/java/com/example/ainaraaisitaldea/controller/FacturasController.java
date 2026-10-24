package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Factura;
import com.example.ainaraaisitaldea.model.enums.EstadoFactura;
import com.example.ainaraaisitaldea.model.enums.TipoDocumento;
import com.example.ainaraaisitaldea.service.FacturaService;
import com.example.ainaraaisitaldea.service.PDFService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import com.example.ainaraaisitaldea.util.NavigationUtil;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class FacturasController {

    @FXML
    private ComboBox<TipoDocumento> cmbTipo;

    @FXML
    private ComboBox<EstadoFactura> cmbEstado;

    @FXML
    private TableView<Factura> tableFacturas;

    @FXML
    private TableColumn<Factura, String> colNumero;

    @FXML
    private TableColumn<Factura, String> colTipo;

    @FXML
    private TableColumn<Factura, String> colCliente;

    @FXML
    private TableColumn<Factura, String> colFecha;

    @FXML
    private TableColumn<Factura, String> colTotal;

    @FXML
    private TableColumn<Factura, String> colEstado;

    private final FacturaService facturaService;
    private final PDFService pdfService;
    private ObservableList<Factura> listaObservable;

    private static final String FXML_BASE = "/fxml/";;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FacturasController() {
        this.facturaService = new FacturaService();
        this.pdfService = new PDFService();
    }

    @FXML
    private void initialize() {
        configurarTabla();
        configurarFiltros();
        cargarDatos();
    }

    private void configurarTabla() {
        colNumero.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNumeroFactura()));

        colTipo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTipo().getDescripcion()));

        colCliente.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCliente()));

        colFecha.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFechaEmision().format(DATE_FORMATTER)));

        colTotal.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f €", data.getValue().getTotal())));

        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEstado().getDescripcion()));

        // Colorear estado
        colEstado.setCellFactory(column -> new TableCell<Factura, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Pagada")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (item.equals("Pendiente")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else if (item.equals("Anulada")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        listaObservable = FXCollections.observableArrayList();
        tableFacturas.setItems(listaObservable);
    }

    private void configurarFiltros() {
        // Agregar opción "Todos"
        cmbTipo.getItems().add(null);
        cmbTipo.getItems().addAll(TipoDocumento.values());
        cmbTipo.setPromptText("Todos los tipos");
        cmbTipo.setValue(null);

        cmbEstado.getItems().add(null);
        cmbEstado.getItems().addAll(EstadoFactura.values());
        cmbEstado.setPromptText("Todos los estados");
        cmbEstado.setValue(null);

        // Listeners
        cmbTipo.setOnAction(e -> aplicarFiltros());
        cmbEstado.setOnAction(e -> aplicarFiltros());

        // Configurar conversión a String
        cmbTipo.setConverter(new javafx.util.StringConverter<TipoDocumento>() {
            @Override
            public String toString(TipoDocumento tipo) {
                return tipo == null ? "Todos" : tipo.getDescripcion();
            }

            @Override
            public TipoDocumento fromString(String string) {
                return null;
            }
        });

        cmbEstado.setConverter(new javafx.util.StringConverter<EstadoFactura>() {
            @Override
            public String toString(EstadoFactura estado) {
                return estado == null ? "Todos" : estado.getDescripcion();
            }

            @Override
            public EstadoFactura fromString(String string) {
                return null;
            }
        });
    }

    private void cargarDatos() {
        try {
            List<Factura> facturas = facturaService.listarTodas();
            listaObservable.clear();
            listaObservable.addAll(facturas);
        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    private void aplicarFiltros() {
        try {
            List<Factura> facturas = facturaService.listarTodas();

            // Filtrar por tipo
            if (cmbTipo.getValue() != null) {
                facturas = facturas.stream()
                        .filter(f -> f.getTipo() == cmbTipo.getValue())
                        .collect(Collectors.toList());
            }

            // Filtrar por estado
            if (cmbEstado.getValue() != null) {
                facturas = facturas.stream()
                        .filter(f -> f.getEstado() == cmbEstado.getValue())
                        .collect(Collectors.toList());
            }

            listaObservable.clear();
            listaObservable.addAll(facturas);

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    @FXML
    private void nuevaFactura(ActionEvent event) {
        abrirFormulario(null, TipoDocumento.FACTURA);
    }

    @FXML
    private void nuevoPresupuesto(ActionEvent event) {
        abrirFormulario(null, TipoDocumento.PRESUPUESTO);
    }

    @FXML
    private void editarFactura(ActionEvent event) {
        Factura seleccionada = tableFacturas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione un documento de la tabla");
            return;
        }

        abrirFormulario(seleccionada, seleccionada.getTipo());
    }

    @FXML
    private void generarPDF(ActionEvent event) {
        Factura seleccionada = tableFacturas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione un documento de la tabla");
            return;
        }

        try {
            String rutaArchivo = pdfService.generarFacturaPDF(seleccionada);
            AlertUtil.mostrarExito("PDF generado correctamente en:\n" + rutaArchivo);
        } catch (Exception e) {
            AlertUtil.mostrarError("Error al generar PDF", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarFactura(ActionEvent event) {
        Factura seleccionada = tableFacturas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione un documento de la tabla");
            return;
        }

        boolean confirmado = AlertUtil.mostrarConfirmacion(
                "Confirmar eliminación",
                "¿Está seguro de eliminar el documento " + seleccionada.getNumeroFactura() + "?");

        if (confirmado) {
            try {
                facturaService.eliminarFactura(seleccionada.getIdFactura());
                AlertUtil.mostrarExito("Documento eliminado correctamente");
                cargarDatos();
            } catch (Exception e) {
                AlertUtil.mostrarErrorBD(e);
            }
        }
    }

    private void abrirFormulario(Factura factura, TipoDocumento tipoInicial) {
        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "facturas_form.fxml");

            if (fxmlUrl == null) {
                AlertUtil.mostrarAdvertencia("Formulario no disponible",
                        "El formulario de factura no se encontró");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            FacturasFormController controller = loader.getController();
            controller.setFactura(factura);
            controller.setControllerPadre(this);

            Stage stage = new Stage();
            String titulo = factura == null ?
                    "Nueva " + tipoInicial.getDescripcion() :
                    "Editar " + factura.getTipo().getDescripcion();
            stage.setTitle(titulo);
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
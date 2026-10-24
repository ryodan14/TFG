package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.dao.DetalleDiaHinchableDAO;
import com.example.ainaraaisitaldea.model.Actividad;
import com.example.ainaraaisitaldea.model.DetalleDiaHinchable;
import com.example.ainaraaisitaldea.model.enums.TipoActividad;
import com.example.ainaraaisitaldea.service.ActividadService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class HinchablesController {

    @FXML private ComboBox<Integer> cmbAnio;
    @FXML private ComboBox<Integer> cmbMes;
    @FXML private Label lblTotalActividades;
    @FXML private Label lblTotalIngresos;
    @FXML private Label lblTotalGastos;
    @FXML private Label lblBeneficioNeto;
    @FXML private Label lblTotalActividadesAnual;
    @FXML private Label lblTotalIngresosAnual;
    @FXML private Label lblTotalGastosAnual;
    @FXML private Label lblBeneficioNetoAnual;
    @FXML private TableView<Actividad> tableHinchables;
    @FXML private TableColumn<Actividad, String> colNombre;
    @FXML private TableColumn<Actividad, String> colIngresos;
    @FXML private TableColumn<Actividad, String> colGastos;
    @FXML private TableColumn<Actividad, String> colBeneficio;
    @FXML private TableColumn<Actividad, String> colFechaDia;
    @FXML private TableColumn<Actividad, String> colHorarioDia;
    @FXML private TableColumn<Actividad, String> colObservaciones;



    private final ActividadService actividadService;
    private final PDFService pdfService;
    private final DetalleDiaHinchableDAO detalleDAO;
    private ObservableList<Actividad> listaObservable;

    private static final String FXML_BASE = "/fxml/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public HinchablesController() {
        this.actividadService = new ActividadService();
        this.pdfService = new PDFService();
        this.detalleDAO = new DetalleDiaHinchableDAO();
    }

    @FXML
    private void initialize() {
        configurarTabla();
        configurarFiltros();
        cargarDatos();
    }

    private void configurarTabla() {

        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombre()));

        colIngresos.setCellValueFactory(data -> {
            double ingresos = data.getValue().getDineroRecibido() != null ?
                    data.getValue().getDineroRecibido() : 0.0;
            return new SimpleStringProperty(String.format("%.2f €", ingresos));
        });

        colGastos.setCellValueFactory(data -> {
            double gastos = data.getValue().getDineroPagadoMonitores() != null ?
                    data.getValue().getDineroPagadoMonitores() : 0.0;
            return new SimpleStringProperty(String.format("%.2f €", gastos));
        });

        colBeneficio.setCellValueFactory(data -> {
            double beneficio = data.getValue().getBeneficioNeto();
            return new SimpleStringProperty(String.format("%.2f €", beneficio));
        });

        // Estilo para beneficio según positivo/negativo
        colBeneficio.setCellFactory(column -> new TableCell<Actividad, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    try {
                        double valor = Double.parseDouble(item.replace(" €", "").replace(",", "."));
                        if (valor >= 0) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        }
                    } catch (NumberFormatException e) {
                        setStyle("");
                    }
                }
            }
        });

        colFechaDia.setCellValueFactory(data -> {
            try {
                List<DetalleDiaHinchable> detalles = detalleDAO.obtenerPorActividad(
                        data.getValue().getIdActividad());

                if (detalles.isEmpty()) {
                    return new SimpleStringProperty("-");
                }

                String dias = detalles.stream()
                        .map(d -> d.getFecha().format(DATE_FORMATTER))
                        .collect(Collectors.joining(", "));

                return new SimpleStringProperty(dias);

            } catch (Exception e) {
                return new SimpleStringProperty("-");
            }
        });
        colHorarioDia.setCellValueFactory(data -> {
            try {
                List<DetalleDiaHinchable> detalles = detalleDAO.obtenerPorActividad(
                        data.getValue().getIdActividad());

                if (detalles.isEmpty()) return new SimpleStringProperty("-");

                String horarios = detalles.stream()
                        .map(d -> {
                            if (d.getHoraInicio() == null || d.getHoraFin() == null) {
                                return d.getFecha().format(DATE_FORMATTER) + ": Sin horario";
                            }
                            return d.getFecha().format(DATE_FORMATTER) + ": " +
                                    d.getHoraInicio() + " - " + d.getHoraFin();
                        })
                        .collect(Collectors.joining("\n"));

                return new SimpleStringProperty(horarios);

            } catch (Exception e) {
                return new SimpleStringProperty("-");
            }
        });
        colObservaciones.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getObservaciones()));

        listaObservable = FXCollections.observableArrayList();
        tableHinchables.setItems(listaObservable);
    }

    private void configurarFiltros() {
        int anioActual = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) {
            cmbAnio.getItems().add(anioActual - i);
        }
        cmbAnio.setValue(anioActual);

        for (int i = 1; i <= 12; i++) {
            cmbMes.getItems().add(i);
        }
        cmbMes.setValue(LocalDate.now().getMonthValue());

        cmbAnio.setOnAction(e -> cargarDatos());
        cmbMes.setOnAction(e -> cargarDatos());
    }

    private void cargarDatos() {
        int anio = cmbAnio.getValue();
        int mes = cmbMes.getValue();

        cargarResumenMensual(anio, mes);
        cargarResumenAnual(anio);
        cargarTablaHinchables(anio, mes);
    }

    private void cargarResumenMensual(int anio, int mes) {
        try {
            List<Actividad> actividades = actividadService.listarPorMes(anio, mes).stream()
                    .filter(a -> a.getTipo() == TipoActividad.HINCHABLES)
                    .collect(Collectors.toList());

            int totalActividades = actividades.size();
            double totalIngresos = actividades.stream()
                    .mapToDouble(a -> a.getDineroRecibido() != null ? a.getDineroRecibido() : 0.0)
                    .sum();
            double totalGastos = actividades.stream()
                    .mapToDouble(a -> a.getDineroPagadoMonitores() != null ? a.getDineroPagadoMonitores() : 0.0)
                    .sum();
            double beneficio = totalIngresos - totalGastos;

            lblTotalActividades.setText(String.valueOf(totalActividades));
            lblTotalIngresos.setText(String.format("%.2f €", totalIngresos));
            lblTotalGastos.setText(String.format("%.2f €", totalGastos));
            lblBeneficioNeto.setText(String.format("%.2f €", beneficio));

            if (beneficio >= 0) {
                lblBeneficioNeto.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                lblBeneficioNeto.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    private void cargarResumenAnual(int anio) {
        try {
            List<Actividad> actividades = FXCollections.observableArrayList();

            for (int mes = 1; mes <= 12; mes++) {
                List<Actividad> actsMes = actividadService.listarPorMes(anio, mes).stream()
                        .filter(a -> a.getTipo() == TipoActividad.HINCHABLES)
                        .collect(Collectors.toList());
                actividades.addAll(actsMes);
            }

            int totalActividades = actividades.size();
            double totalIngresos = actividades.stream()
                    .mapToDouble(a -> a.getDineroRecibido() != null ? a.getDineroRecibido() : 0.0)
                    .sum();
            double totalGastos = actividades.stream()
                    .mapToDouble(a -> a.getDineroPagadoMonitores() != null ? a.getDineroPagadoMonitores() : 0.0)
                    .sum();
            double beneficio = totalIngresos - totalGastos;

            lblTotalActividadesAnual.setText(String.valueOf(totalActividades));
            lblTotalIngresosAnual.setText(String.format("%.2f €", totalIngresos));
            lblTotalGastosAnual.setText(String.format("%.2f €", totalGastos));
            lblBeneficioNetoAnual.setText(String.format("%.2f €", beneficio));

            if (beneficio >= 0) {
                lblBeneficioNetoAnual.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                lblBeneficioNetoAnual.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    private void cargarTablaHinchables(int anio, int mes) {
        try {
            List<Actividad> actividades = actividadService.listarPorMes(anio, mes).stream()
                    .filter(a -> a.getTipo() == TipoActividad.HINCHABLES)
                    .sorted((a1, a2) -> a1.getFecha().compareTo(a2.getFecha()))
                    .collect(Collectors.toList());

            listaObservable.clear();
            listaObservable.addAll(actividades);

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    @FXML
    private void registrarSalida(ActionEvent event) {
        abrirFormularioHinchable(null);
    }

    @FXML
    private void editarSalida(ActionEvent event) {
        Actividad seleccionada = tableHinchables.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione una salida de la tabla");
            return;
        }

        abrirFormularioHinchable(seleccionada);
    }

    @FXML
    private void eliminarSalida(ActionEvent event) {
        Actividad seleccionada = tableHinchables.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione una salida de la tabla");
            return;
        }

        boolean confirmado = AlertUtil.mostrarConfirmacion(
                "Confirmar eliminación",
                "¿Está seguro de eliminar esta salida de hinchables?");

        if (confirmado) {
            try {
                actividadService.eliminarActividad(seleccionada.getIdActividad());
                AlertUtil.mostrarExito("Salida eliminada correctamente");
                cargarDatos();
            } catch (Exception e) {
                AlertUtil.mostrarErrorBD(e);
            }
        }
    }

    private void abrirFormularioHinchable(Actividad actividad) {
        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "hinchables_form.fxml");

            if (fxmlUrl == null) {
                AlertUtil.mostrarAdvertencia("Formulario no disponible",
                        "El formulario de registro aún no está implementado");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            HinchablesFormController controller = loader.getController();
            controller.setActividad(actividad);
            controller.setControllerPadre(this);

            Stage stage = new Stage();
            stage.setTitle(actividad == null ? "Registrar Salida de Hinchables" : "Editar Salida");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NUEVO: Exportar PDF Anual completo
     */
    @FXML
    private void exportarPDFAnual(ActionEvent event) {
        try {
            int anio = cmbAnio.getValue();

            AlertUtil.mostrarInfo("Generando PDF",
                    "Generando reporte anual completo para " + anio + "...\n" +
                            "Esto puede tardar unos momentos.");

            String rutaArchivo = pdfService.generarReporteAnualHinchables(anio);

            AlertUtil.mostrarExito("PDF generado correctamente en:\n" + rutaArchivo);

        } catch (Exception e) {
            AlertUtil.mostrarError("Error al generar PDF", e.getMessage());
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
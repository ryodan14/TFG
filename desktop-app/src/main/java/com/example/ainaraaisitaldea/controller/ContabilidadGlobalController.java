package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.model.MovimientoEconomico;
import com.example.ainaraaisitaldea.model.MovimientoEconomico;
import com.example.ainaraaisitaldea.service.MovimientoEconomicoService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.util.NavigationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContabilidadGlobalController {

    @FXML private Label lblFechaActual;
    @FXML private Label lblIngresosTotal;
    @FXML private Label lblGastosTotal;
    @FXML private Label lblBeneficioNeto;

    // Gráfico
    @FXML private ProgressBar barraIngresos;
    @FXML private ProgressBar barraGastos;
    @FXML private Label lblIngresosGrafico;
    @FXML private Label lblGastosGrafico;

    // Tabla
    @FXML private TableView<Map<String, String>> tablaMovimientos;
    @FXML private TableColumn<Map<String, String>, String> colFecha;
    @FXML private TableColumn<Map<String, String>, String> colTipo;
    @FXML private TableColumn<Map<String, String>, String> colConcepto;
    @FXML private TableColumn<Map<String, String>, String> colCantidad;
    @FXML private TableColumn<Map<String, String>, String> colObservaciones;

    private final MovimientoEconomicoService movimientoService;
    private static final String FXML_BASE = "/fxml/";

    public ContabilidadGlobalController() {
        this.movimientoService = new MovimientoEconomicoService();
    }

    @FXML
    private void initialize() {
        configurarFecha();
        configurarTabla();
        cargarDatos();
    }

    private void configurarFecha() {
        if (lblFechaActual != null) {
            LocalDate hoy = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",
                    new Locale("es", "ES"));
            String fechaTexto = hoy.format(formatter);
            fechaTexto = fechaTexto.substring(0, 1).toUpperCase() + fechaTexto.substring(1);
            lblFechaActual.setText(fechaTexto);
        }
    }

    private void configurarTabla() {
        // Configurar columnas
        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get("fecha")));

        colTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get("tipo")));

        colConcepto.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get("concepto")));

        colCantidad.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get("cantidad")));
        colObservaciones.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get("observaciones")));

        // Estilo personalizado para la columna tipo
        colTipo.setCellFactory(column -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("INGRESO".equals(item)) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else if ("GASTO".equals(item)) {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Estilo para cantidades
        colCantidad.setCellFactory(column -> new TableCell<Map<String, String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Map<String, String> rowData = getTableView().getItems().get(getIndex());
                    String tipo = rowData.get("tipo");
                    if ("INGRESO".equals(tipo)) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else if ("GASTO".equals(tipo)) {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void cargarDatos() {
        cargarTotales();
        cargarUltimosMovimientos();
    }

    private void cargarTotales() {
        try {
            Map<String, BigDecimal> totales = movimientoService.calcularTotalesConsolidados();

            BigDecimal ingresos = totales.getOrDefault("ingresos", BigDecimal.ZERO);
            BigDecimal gastos = totales.getOrDefault("gastos", BigDecimal.ZERO);
            BigDecimal beneficio = totales.getOrDefault("beneficio", BigDecimal.ZERO);

            // Actualizar labels
            lblIngresosTotal.setText(String.format("%.2f €", ingresos.doubleValue()));
            lblGastosTotal.setText(String.format("%.2f €", gastos.doubleValue()));
            lblBeneficioNeto.setText(String.format("%.2f €", beneficio.doubleValue()));

            // Actualizar gráfico
            actualizarGrafico(ingresos, gastos);

        } catch (Exception e) {
            System.err.println("Error al cargar totales: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudieron cargar los totales");
        }
    }

    private void actualizarGrafico(BigDecimal ingresos, BigDecimal gastos) {
        double maxValor = Math.max(ingresos.doubleValue(), gastos.doubleValue());

        if (maxValor > 0) {
            double progIngresos = ingresos.doubleValue() / maxValor;
            double progGastos = gastos.doubleValue() / maxValor;

            barraIngresos.setProgress(progIngresos);
            barraGastos.setProgress(progGastos);
        } else {
            barraIngresos.setProgress(0);
            barraGastos.setProgress(0);
        }

        lblIngresosGrafico.setText(String.format("%.2f €", ingresos.doubleValue()));
        lblGastosGrafico.setText(String.format("%.2f €", gastos.doubleValue()));
    }

    private void cargarUltimosMovimientos() {
        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();

        String sql = "SELECT id_registro, tipo, concepto, fecha, ingreso, gasto, observaciones, origen " +
                "FROM v_economia_consolidada " +
                "ORDER BY fecha DESC, id_registro DESC " +
                "LIMIT 13";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, String> fila = new java.util.HashMap<>();

                // Formatear fecha
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                fila.put("fecha", fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                // id
                fila.put("id", String.valueOf(rs.getInt("id_registro")));

                // Tipo
                fila.put("tipo", rs.getString("tipo"));

                // Concepto
                fila.put("concepto", rs.getString("concepto"));

                // Cantidad (mostrar ingreso o gasto)
                BigDecimal ingreso = rs.getBigDecimal("ingreso");
                BigDecimal gasto = rs.getBigDecimal("gasto");

                // Observaciones
                fila.put("observaciones", rs.getString("observaciones"));

                // Origen
                fila.put("origen", rs.getString("origen"));

                if (ingreso != null && ingreso.compareTo(BigDecimal.ZERO) > 0) {
                    fila.put("cantidad", String.format("+%.2f €", ingreso.doubleValue()));
                } else if (gasto != null && gasto.compareTo(BigDecimal.ZERO) > 0) {
                    fila.put("cantidad", String.format("-%.2f €", gasto.doubleValue()));
                } else {
                    fila.put("cantidad", "0,00 €");
                }

                datos.add(fila);
            }

            tablaMovimientos.setItems(datos);

        } catch (Exception e) {
            System.err.println("Error al cargar movimientos: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudieron cargar los movimientos");
        }
    }

    @FXML
    private void registrarIngreso(ActionEvent event) {
        abrirDialogoMovimiento(true);
    }

    @FXML
    private void registrarGasto(ActionEvent event) {
        abrirDialogoMovimiento(false);
    }

    private void abrirDialogoMovimiento(boolean esIngreso) {
        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "dialogo-movimiento-economico.fxml");

            if (fxmlUrl == null) {
                AlertUtil.mostrarAdvertencia("Diálogo no disponible",
                        "No se encontró el formulario de registro");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            DialogoMovimientoEconomicoController controller = loader.getController();
            controller.setTipoMovimiento(esIngreso ?
                    MovimientoEconomico.Tipo.INGRESO : MovimientoEconomico.Tipo.GASTO);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(esIngreso ? "Registrar Ingreso" : "Registrar Gasto");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.WINDOW_MODAL);

            dialogStage.initOwner(tablaMovimientos.getScene().getWindow());

            dialogStage.showAndWait();

            cargarDatos();

        } catch (Exception e) {
            System.err.println("Error al abrir diálogo: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo abrir el formulario");
        }
    }

    @FXML
    private void verTodosMovimientos(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "todos-movimientos.fxml");
            if (fxmlUrl == null) {
                AlertUtil.mostrarAdvertencia("Módulo no disponible",
                        "La pantalla de todos los movimientos aún no está implementada");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Todos los Movimientos Económicos");
            stage.setMaximized(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Error al cargar todos los movimientos: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo cargar la pantalla");
        }
    }

    @FXML
    private void eliminarMovimientoSeleccionado(ActionEvent event) {
        Map<String, String> seleccion = tablaMovimientos.getSelectionModel().getSelectedItem();

        if (seleccion == null) {
            AlertUtil.mostrarAdvertencia("Ninguna selección", "Por favor, seleccione un movimiento para eliminar.");
            return;
        }
        // 🔍 Detectar el tipo de registro
        String origen = seleccion.get("origen");

        // Si es factura o hinchable → aviso amable
        if ("factura".equals(origen)) {
            AlertUtil.mostrarInfo(
                    "Información",
                    "Las facturas no se pueden eliminar desde esta pantalla.\n\n" +
                            "Gestiónalos en la sección de 'Facturas'."
            );
            return;
        }

        if ("hinchable".equals(origen)) {
            AlertUtil.mostrarInfo(
                    "Información",
                    "Los hinchables no se eliminan desde aquí.\n\n" +
                            "Gestiónalos en la sección de 'Hinchables'."
            );
            return;
        }
        // Confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de que desea eliminar este movimiento?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        confirmacion.getDialogPane().setStyle("-fx-font-size: 13px;");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            int idMovimiento = Integer.parseInt(seleccion.get("id"));

            if (movimientoService.eliminar(idMovimiento)) {
                AlertUtil.mostrarInfo("Eliminado", "El movimiento ha sido eliminado correctamente.");
                cargarDatos(); // Recarga totales y tabla
            } else {
                AlertUtil.mostrarError("Error", "No se pudo eliminar el movimiento.");
            }
        } catch (Exception e) {
            System.err.println("Error al eliminar movimiento: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "Ocurrió un error al intentar eliminar el movimiento.");
        }
    }

    @FXML
    private void refrescarDatos(ActionEvent event) {
        cargarDatos();
        AlertUtil.mostrarInfo("Actualizado", "Datos actualizados correctamente");
    }

    @FXML
    private void volverDashboard(ActionEvent event) {
        NavigationUtil.volverAlDashboard(event);
    }
}
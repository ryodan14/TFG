package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.service.MovimientoEconomicoService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import com.example.ainaraaisitaldea.database.ConnectionFactory;
import com.example.ainaraaisitaldea.util.NavigationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TodosMovimientosController {

    @FXML private Label lblTotalMovimientos;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private ComboBox<String> cbFiltroTipo;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Map<String, String>> tablaMovimientos;
    @FXML private TableColumn<Map<String, String>, String> colFecha;
    @FXML private TableColumn<Map<String, String>, String> colTipo;
    @FXML private TableColumn<Map<String, String>, String> colConcepto;
    @FXML private TableColumn<Map<String, String>, String> colCantidad;
    @FXML private TableColumn<Map<String, String>, String> colObservaciones;

    @FXML private Label lblTotalIngresos;
    @FXML private Label lblTotalGastos;
    @FXML private Label lblBalance;

    private final MovimientoEconomicoService movimientoService = new MovimientoEconomicoService();
    @FXML
    private void initialize() {

        configurarFiltros();
        configurarTabla();
        cargarMovimientos();
    }

    private void configurarFiltros() {
        // Configurar combo de tipo
        cbFiltroTipo.getItems().addAll("Todos", "Ingreso", "Gasto");
        cbFiltroTipo.setValue("Todos");
    }

    private void configurarTabla() {
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

        // Estilo para tipo
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
                    } else {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Estilo para cantidad
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
                    } else {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void cargarMovimientos() {
        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(
                "SELECT id_registro, tipo, concepto, fecha, ingreso, gasto, observaciones, origen " +
                        "FROM v_economia_consolidada WHERE 1=1"
        );

        // Aplicar filtros
        if (dpFechaDesde.getValue() != null) {
            sql.append(" AND fecha >= ?");
        }
        if (dpFechaHasta.getValue() != null) {
            sql.append(" AND fecha <= ?");
        }
        if (cbFiltroTipo.getValue() != null && !"Todos".equals(cbFiltroTipo.getValue())) {
            sql.append(" AND tipo = ?");
        }
        if (txtBuscar.getText() != null && !txtBuscar.getText().trim().isEmpty()) {
            sql.append(" AND concepto LIKE ?");
        }

        sql.append(" ORDER BY fecha DESC, id_registro DESC");

        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalGastos = BigDecimal.ZERO;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (dpFechaDesde.getValue() != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(dpFechaDesde.getValue()));
            }
            if (dpFechaHasta.getValue() != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(dpFechaHasta.getValue()));
            }
            if (cbFiltroTipo.getValue() != null && !"Todos".equals(cbFiltroTipo.getValue())) {
                stmt.setString(paramIndex++, cbFiltroTipo.getValue());
            }
            if (txtBuscar.getText() != null && !txtBuscar.getText().trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + txtBuscar.getText().trim() + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> fila = new java.util.HashMap<>();

                    fila.put("id", String.valueOf(rs.getInt("id_registro")));
                    LocalDate fecha = rs.getDate("fecha").toLocalDate();
                    fila.put("fecha", fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    fila.put("tipo", rs.getString("tipo"));
                    fila.put("concepto", rs.getString("concepto"));

                    BigDecimal ingreso = rs.getBigDecimal("ingreso");
                    BigDecimal gasto = rs.getBigDecimal("gasto");

                    fila.put("origen", rs.getString("origen"));

                    if (ingreso != null && ingreso.compareTo(BigDecimal.ZERO) > 0) {
                        fila.put("cantidad", String.format("+%.2f €", ingreso.doubleValue()));
                        totalIngresos = totalIngresos.add(ingreso);
                    } else if (gasto != null && gasto.compareTo(BigDecimal.ZERO) > 0) {
                        fila.put("cantidad", String.format("-%.2f €", gasto.doubleValue()));
                        totalGastos = totalGastos.add(gasto);
                    } else {
                        fila.put("cantidad", "0,00 €");
                    }

                    String obs = rs.getString("observaciones");
                    fila.put("observaciones", obs != null && !obs.trim().isEmpty() ? obs : "-");

                    datos.add(fila);
                }
            }

            tablaMovimientos.setItems(datos);
            lblTotalMovimientos.setText("Total: " + datos.size() + " movimientos");

            // Actualizar resumen
            lblTotalIngresos.setText(String.format("%.2f €", totalIngresos.doubleValue()));
            lblTotalGastos.setText(String.format("%.2f €", totalGastos.doubleValue()));

            BigDecimal balance = totalIngresos.subtract(totalGastos);
            lblBalance.setText(String.format("%.2f €", balance.doubleValue()));

            // Cambiar color del balance según si es positivo o negativo
            if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                lblBalance.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
            } else {
                lblBalance.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #F44336;");
            }

        } catch (Exception e) {
            System.err.println("Error al cargar movimientos: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudieron cargar los movimientos");
        }
    }

    @FXML
    private void aplicarFiltros(ActionEvent event) {
        cargarMovimientos();
    }

    @FXML
    private void limpiarFiltros(ActionEvent event) {
        dpFechaDesde.setValue(null);
        dpFechaHasta.setValue(null);
        cbFiltroTipo.setValue("Todos");
        txtBuscar.clear();
        cargarMovimientos();
    }

    @FXML
    private void buscar(ActionEvent event) {
        cargarMovimientos();
    }

    @FXML
    private void exportarExcel(ActionEvent event) {
        AlertUtil.mostrarInfo("Próximamente",
                "La función de exportar a Excel estará disponible próximamente");
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
                cargarMovimientos(); // Recarga totales y tabla
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
        cargarMovimientos();
        AlertUtil.mostrarInfo("Actualizado", "Datos actualizados correctamente");
    }
    @FXML
    private void handleVolver(ActionEvent event) throws IOException {
        NavigationUtil.cargarVista(event, "/fxml/economia-global.fxml", "Contabilidad Global");
    }


}
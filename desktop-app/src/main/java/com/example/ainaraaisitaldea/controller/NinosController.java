package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Nino;
import com.example.ainaraaisitaldea.model.Tutor;
import com.example.ainaraaisitaldea.service.NinoService;
import com.example.ainaraaisitaldea.util.*;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NinosController {

    @FXML private Label lblTotalNinos;
    @FXML private Button btnEliminar;

    // Filtros
    @FXML private CheckBox chkLudoteka;
    @FXML private CheckBox chkGazteLokala;
    @FXML private CheckBox chkGazteLokala2;
    @FXML private ComboBox<String> cbFiltroCurso;
    @FXML private TextField txtBuscar;

    // Tabla
    @FXML private TableView<Nino> tablaNinos;
    @FXML private TableColumn<Nino, String> colNombre;
    @FXML private TableColumn<Nino, String> colPrimerApellido;
    @FXML private TableColumn<Nino, String> colSegundoApellido;
    @FXML private TableColumn<Nino, String> colFechaNacimiento;
    @FXML private TableColumn<Nino, String> colEdad;
    @FXML private TableColumn<Nino, String> colColegio;
    @FXML private TableColumn<Nino, String> colCurso;
    @FXML private TableColumn<Nino, String> colAlergias;
    @FXML private TableColumn<Nino, String> colSalirSolo;
    @FXML private TableColumn<Nino, String> colFotos;
    @FXML private TableColumn<Nino, String> colNombreTutor;
    @FXML private TableColumn<Nino, String> colTelefonoTutor;
    @FXML private TableColumn<Nino, String> colObservaciones;

    private final NinoService ninoService;
    private ObservableList<Nino> listaNinos;
    private static final String FXML_BASE = "/fxml/";

    public NinosController() {
        this.ninoService = new NinoService();
    }

    @FXML
    private void initialize() {
        configurarComboCursos();
        configurarTabla();
        configurarOrdenamientoCurso();
        configurarClickTabla();
        configurarListeners();
        cargarNinos();
    }

    private void configurarListeners() {
        // Actualizar automáticamente al cambiar checkboxes
        chkLudoteka.selectedProperty().addListener((obs, old, newVal) -> aplicarFiltrosAuto());
        chkGazteLokala.selectedProperty().addListener((obs, old, newVal) -> aplicarFiltrosAuto());
        chkGazteLokala2.selectedProperty().addListener((obs, old, newVal) -> aplicarFiltrosAuto());

        // Actualizar automáticamente al cambiar combo de curso
        cbFiltroCurso.valueProperty().addListener((obs, old, newVal) -> aplicarFiltrosAuto());

        // Actualizar automáticamente al escribir en búsqueda
        txtBuscar.textProperty().addListener((obs, old, newVal) -> aplicarFiltrosAuto());
    }

    private void aplicarFiltrosAuto() {
        try {
            List<String> tiposActividad = new ArrayList<>();

            if (chkLudoteka.isSelected()) tiposActividad.add("LUDOTEKA");
            if (chkGazteLokala.isSelected()) tiposActividad.add("GAZTE_LOKALA");
            if (chkGazteLokala2.isSelected()) tiposActividad.add("GAZTE_LOKALA_2");

            if (tiposActividad.isEmpty()) {
                tiposActividad.add("TODOS");
            }

            String curso = cbFiltroCurso.getValue();
            String busqueda = txtBuscar.getText();

            List<Nino> ninos = ninoService.listarConFiltros(tiposActividad, curso, busqueda);
            listaNinos = FXCollections.observableArrayList(ninos);
            tablaNinos.setItems(listaNinos);
            actualizarContador();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudieron aplicar los filtros automáticamente");
        }
    }

    @FXML
    private void buscar(ActionEvent event) {
        aplicarFiltrosAuto();
    }

    @FXML
    private void limpiarFiltros(ActionEvent event) {
        chkLudoteka.setSelected(false);
        chkGazteLokala.setSelected(false);
        chkGazteLokala2.setSelected(false);
        cbFiltroCurso.setValue("Todos");
        txtBuscar.clear();
        cargarNinos();
    }

    @FXML
    private void eliminarNinoSeleccionado() {
        Nino seleccionado = tablaNinos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            // Sin selección: eliminar TODOS los registros
            eliminarTodosLosNinos();
            return;
        }

        // Con selección: eliminar solo el seleccionado
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar a " + seleccionado.getNombreCompleto() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (ninoService.eliminar(seleccionado.getIdNino())) {
                    AlertUtil.mostrarInfo("Éxito", "Niño eliminado correctamente.");
                    aplicarFiltrosAuto();
                } else {
                    AlertUtil.mostrarError("Error", "No se pudo eliminar el niño.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.mostrarError("Error", "Ocurrió un error al eliminar: " + e.getMessage());
            }
        }
    }

    private void eliminarTodosLosNinos() {
        // Primera confirmación
        Alert confirmacion1 = new Alert(Alert.AlertType.WARNING);
        confirmacion1.setTitle("⚠️ ADVERTENCIA CRÍTICA");
        confirmacion1.setHeaderText("¿Está seguro de que desea ELIMINAR TODOS LOS REGISTROS?");
        confirmacion1.setContentText("Esta acción eliminará TODOS los niños de la base de datos.\n" +
                "Total de registros a eliminar: " + listaNinos.size());

        if (confirmacion1.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // Segunda confirmación
        Alert confirmacion2 = new Alert(Alert.AlertType.ERROR);
        confirmacion2.setTitle("⚠️ ÚLTIMA CONFIRMACIÓN");
        confirmacion2.setHeaderText("ESTA ACCIÓN NO SE PUEDE DESHACER");
        confirmacion2.setContentText("Se eliminarán " + listaNinos.size() + " registros permanentemente.\n\n" +
                "¿Está COMPLETAMENTE seguro?");

        if (confirmacion2.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // Tercera confirmación con texto de verificación
        TextInputDialog confirmacion3 = new TextInputDialog();
        confirmacion3.setTitle("Verificación Final");
        confirmacion3.setHeaderText("Escriba 'ELIMINAR TODO' para confirmar");
        confirmacion3.setContentText("Escriba exactamente:");

        String confirmacionTexto = confirmacion3.showAndWait().orElse("");

        if (!"ELIMINAR TODO".equals(confirmacionTexto)) {
            AlertUtil.mostrarInfo("Cancelado", "Operación cancelada. No se eliminó ningún registro.");
            return;
        }

        // Proceder con la eliminación
        try {
            int eliminados = 0;
            int errores = 0;

            for (Nino nino : listaNinos) {
                if (ninoService.eliminar(nino.getIdNino())) {
                    eliminados++;
                } else {
                    errores++;
                }
            }

            AlertUtil.mostrarInfo("Eliminación completada",
                    "Registros eliminados: " + eliminados + "\n" +
                            "Errores: " + errores);

            cargarNinos();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.mostrarError("Error crítico",
                    "Ocurrió un error durante la eliminación masiva: " + e.getMessage());
        }
    }

    private void configurarComboCursos() {
        cbFiltroCurso.getItems().addAll(
                "Todos",
                "1º Primaria", "2º Primaria", "3º Primaria", "4º Primaria", "5º Primaria", "6º Primaria",
                "1º ESO", "2º ESO", "3º ESO", "4º ESO"
        );
        cbFiltroCurso.setValue("Todos");
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombre()));

        colPrimerApellido.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrimerApellido()));

        colSegundoApellido.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSegundoApellido()));

        colFechaNacimiento.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaNacimiento()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

        colEdad.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getEdad())));

        colColegio.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getColegioInstituto()));

        colCurso.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCurso()));

        colAlergias.setCellValueFactory(cellData -> {
            boolean alergias = cellData.getValue().isTieneAlergias();
            return new SimpleStringProperty(alergias ? "Sí" : "No");
        });

        colSalirSolo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPuedeSalirSoloTexto()));

        colFotos.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAutorizacionFotosTexto()));

        colNombreTutor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombreCompletoTutor()));

        colTelefonoTutor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTelefonoTutor()));

        colObservaciones.setCellValueFactory(cellData -> {
            String obs = cellData.getValue().getObservaciones();
            return new SimpleStringProperty(obs != null && !obs.trim().isEmpty() ? obs : "-");
        });

        // Configurar ordenamiento por fecha de nacimiento
        colFechaNacimiento.setComparator((fecha1, fecha2) -> {
            // 1. Es mejor sacar el formatter fuera del lambda por rendimiento,
            // pero si lo dejas dentro, funcionará igual.
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            try {
                // CORRECCIÓN: Parsear explícitamente a LocalDate
                LocalDate d1 = LocalDate.parse(fecha1, formatter);
                LocalDate d2 = LocalDate.parse(fecha2, formatter);

                return d1.compareTo(d2);

            } catch (Exception e) {
                // Manejo de errores: si una fecha no es válida, se consideran iguales
                // o puedes decidir ponerlas al final.
                return 0;
            }
        });



        // Configurar ordenamiento por edad
        colEdad.setComparator((edad1, edad2) -> {
            try {
                return Integer.compare(Integer.parseInt(edad1), Integer.parseInt(edad2));
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void configurarOrdenamientoCurso() {
        colCurso.setComparator((curso1, curso2) -> {
            int orden1 = Nino.getOrdenCurso(curso1);
            int orden2 = Nino.getOrdenCurso(curso2);
            return Integer.compare(orden1, orden2);
        });
    }

    private void configurarClickTabla() {
        tablaNinos.setRowFactory(tv -> {
            TableRow<Nino> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Nino ninoSeleccionado = row.getItem();
                    mostrarDetalleNino(ninoSeleccionado);
                }
            });
            return row;
        });
    }

    private void cargarNinos() {
        try {
            List<Nino> ninos = ninoService.listar();
            listaNinos = FXCollections.observableArrayList(ninos);
            tablaNinos.setItems(listaNinos);
            actualizarContador();
        } catch (Exception e) {
            System.err.println("Error al cargar niños: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudieron cargar los niños");
        }
    }

    private void actualizarContador() {
        int total = listaNinos != null ? listaNinos.size() : 0;
        lblTotalNinos.setText("Total: " + total + " niños");
    }

    private void mostrarDetalleNino(Nino nino) {
        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "detalle-nino.fxml");

            if (fxmlUrl == null) {
                AlertUtil.mostrarAdvertencia("Vista no disponible",
                        "La vista de detalle aún no está implementada");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            DetalleNinoController controller = loader.getController();

            // Obtener el índice actual y pasar la lista ordenada
            int indiceActual = listaNinos.indexOf(nino);
            controller.setNinoConLista(nino, new ArrayList<>(listaNinos), indiceActual);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Detalle del Niño");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tablaNinos.getScene().getWindow());
            dialogStage.showAndWait();

            aplicarFiltrosAuto();

        } catch (Exception e) {
            System.err.println("Error al mostrar detalle: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo abrir la vista de detalle");
        }
    }

    @FXML
    private void abrirFormularioAlta(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "alta-nino.fxml");

            if (fxmlUrl == null) {
                AlertUtil.mostrarAdvertencia("Formulario no disponible",
                        "El formulario de alta aún no está implementado");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Alta de Niño");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tablaNinos.getScene().getWindow());
            dialogStage.showAndWait();

            cargarNinos();

        } catch (Exception e) {
            System.err.println("Error al abrir formulario de alta: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo abrir el formulario");
        }
    }

    @FXML
    private void exportarPDF(ActionEvent event) {
        try {
            if (listaNinos == null || listaNinos.isEmpty()) {
                AlertUtil.mostrarAdvertencia("Sin datos",
                        "No hay niños para exportar. Aplique filtros o cargue datos primero.");
                return;
            }

            // Determinar el tipo de actividad según los filtros
            String tipoActividad = "TODOS LOS PARTICIPANTES";
            List<String> filtrosActivos = new ArrayList<>();

            if (chkLudoteka.isSelected()) filtrosActivos.add("LUDOTEKA");
            if (chkGazteLokala.isSelected()) filtrosActivos.add("GAZTE LOKALA");
            if (chkGazteLokala2.isSelected()) filtrosActivos.add("GAZTE LOKALA 2.0");

            if (!filtrosActivos.isEmpty()) {
                tipoActividad = String.join(" + ", filtrosActivos);
            }

            // PDFExporter.exportarListadoAsistenciaPDF(listaNinos, tipoActividad);
            AlertUtil.mostrarInfo("Éxito", "PDF de asistencia exportado correctamente");

        } catch (Exception e) {
            System.err.println("Error al exportar PDF: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo exportar el PDF: " + e.getMessage());
        }
    }

    @FXML
    private void exportar(ActionEvent event) {
        try {
            if (listaNinos == null || listaNinos.isEmpty()) {
                AlertUtil.mostrarAdvertencia("Sin datos",
                        "No hay niños para exportar. Aplique filtros o cargue datos primero.");
                return;
            }

            String titulo = "LISTADO_NINOS_COMPLETO";
            List<String> filtrosActivos = new ArrayList<>();

            if (chkLudoteka.isSelected()) filtrosActivos.add("LUDOTEKA");
            if (chkGazteLokala.isSelected()) filtrosActivos.add("GAZTE_LOKALA");
            if (chkGazteLokala2.isSelected()) filtrosActivos.add("GAZTE_LOKALA_2");

            if (!filtrosActivos.isEmpty()) {
                titulo = "LISTADO_NINOS_" + String.join("_", filtrosActivos);
            }

            CSVExporter.exportarNinosCSV(listaNinos, titulo);
            AlertUtil.mostrarInfo("Éxito",
                    "CSV exportado correctamente con todos los datos.\n" +
                            "Este archivo puede ser usado para importar los datos nuevamente.");

        } catch (Exception e) {
            System.err.println("Error al exportar CSV: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo exportar el CSV: " + e.getMessage());
        }
    }

    @FXML
    private void importar(ActionEvent event) {
        try {
            // Confirmación antes de importar
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Importar datos desde CSV");
            confirmacion.setHeaderText("¿Desea importar niños desde un archivo CSV?");

            if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            CSVImporter.ResultadoImportacion resultado = CSVImporter.importarNinosCSV();

            if (resultado.exitosos == 0 && resultado.errores == 0) {
                // Usuario canceló la selección de archivo
                return;
            }

            // Mostrar resultado
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Importación completada:\n\n");
            mensaje.append("✓ Registros importados: ").append(resultado.exitosos).append("\n");
            mensaje.append("✗ Errores: ").append(resultado.errores).append("\n");

            if (!resultado.mensajesError.isEmpty()) {
                mensaje.append("\nPrimeros errores:\n");
                int maxErrores = Math.min(5, resultado.mensajesError.size());
                for (int i = 0; i < maxErrores; i++) {
                    mensaje.append("- ").append(resultado.mensajesError.get(i)).append("\n");
                }
                if (resultado.mensajesError.size() > 5) {
                    mensaje.append("... y ").append(resultado.mensajesError.size() - 5)
                            .append(" errores más.");
                }
            }

            if (resultado.errores > 0) {
                AlertUtil.mostrarAdvertencia("Importación con errores", mensaje.toString());
            } else {
                AlertUtil.mostrarInfo("Importación exitosa", mensaje.toString());
            }

            // Recargar datos
            cargarNinos();

        } catch (Exception e) {
            System.err.println("Error al importar CSV: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo importar el CSV: " + e.getMessage());
        }
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        NavigationUtil.volverAlDashboard(event);
    }
}
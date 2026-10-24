package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Actividad;
import com.example.ainaraaisitaldea.model.enums.TipoActividad;
import com.example.ainaraaisitaldea.service.ActividadService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import com.example.ainaraaisitaldea.util.NavigationUtil;
import com.example.ainaraaisitaldea.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CalendarioController {

    @FXML
    private Label lblMesAnio;

    @FXML
    private GridPane gridCalendario;

    @FXML
    private ListView<String> listActividadesDia;

    @FXML
    private Label lblDiaSeleccionado;

    @FXML
    private Button btnVistaMensual;

    @FXML
    private Button btnVistaSemanal;

    @FXML
    private VBox paneVistaMensual;

    @FXML
    private VBox paneVistaSemanal;

    @FXML
    private ListView<String> listActividadesSemana;

    @FXML
    private Button btnNuevaActividad;

    @FXML
    private Button btnEditarActividad;

    @FXML
    private Button btnEliminarActividad;


    private final ActividadService actividadService;
    private final SessionManager sessionManager;
    private YearMonth mesActual;
    private LocalDate diaSeleccionado;
    private LocalDate inicioSemana;
    private Actividad actividadSeleccionada;
    private boolean vistaMensual = true;

    private static final String FXML_BASE = "/fxml/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CalendarioController() {
        this.actividadService = new ActividadService();
        this.sessionManager = SessionManager.getInstance();
        this.mesActual = YearMonth.now();
        this.diaSeleccionado = LocalDate.now();
        this.inicioSemana = calcularInicioSemana(LocalDate.now());
    }

    @FXML
    private void initialize() {
        configurarPermisos();
        mostrarVistaMensual();
    }

    private void configurarPermisos() {
        boolean esAdmin = sessionManager.esAdministrador();
        btnNuevaActividad.setVisible(esAdmin);
        btnNuevaActividad.setManaged(esAdmin);
        btnEditarActividad.setVisible(esAdmin);
        btnEditarActividad.setManaged(esAdmin);
        btnEliminarActividad.setVisible(esAdmin);
        btnEliminarActividad.setManaged(esAdmin);
    }

    @FXML
    private void cambiarAVistaMensual(ActionEvent event) {
        mostrarVistaMensual();
    }

    @FXML
    private void cambiarAVistaSemanal(ActionEvent event) {
        mostrarVistaSemanal();
    }

    private void mostrarVistaMensual() {
        vistaMensual = true;
        paneVistaMensual.setVisible(true);
        paneVistaMensual.setManaged(true);
        paneVistaSemanal.setVisible(false);
        paneVistaSemanal.setManaged(false);

        btnVistaMensual.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnVistaSemanal.setStyle("-fx-background-color: white; -fx-text-fill: #2196F3;");

        cargarCalendarioMensual();
        cargarActividadesDia(diaSeleccionado);
    }

    private void mostrarVistaSemanal() {
        vistaMensual = false;
        paneVistaMensual.setVisible(false);
        paneVistaMensual.setManaged(false);
        paneVistaSemanal.setVisible(true);
        paneVistaSemanal.setManaged(true);

        btnVistaMensual.setStyle("-fx-background-color: white; -fx-text-fill: #2196F3;");
        btnVistaSemanal.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        cargarActividadesSemana();
    }

    private void cargarCalendarioMensual() {
        lblMesAnio.setText(
                mesActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) +
                        " " + mesActual.getYear());

        gridCalendario.getChildren().clear();

        // Encabezados de días
        String[] dias = {"L", "M", "X", "J", "V", "S", "D"};
        for (int i = 0; i < dias.length; i++) {
            Label lblDia = new Label(dias[i]);
            lblDia.setStyle("-fx-font-weight: bold; -fx-pref-width: 100; -fx-alignment: center; -fx-font-size: 14px;");
            gridCalendario.add(lblDia, i, 0);
        }

        LocalDate primerDia = mesActual.atDay(1);
        int diaSemana = primerDia.getDayOfWeek().getValue() - 1;
        int diasMes = mesActual.lengthOfMonth();

        int fila = 1;
        int columna = diaSemana;

        for (int dia = 1; dia <= diasMes; dia++) {
            LocalDate fecha = mesActual.atDay(dia);
            VBox celda = crearCeldaDia(dia, fecha);

            gridCalendario.add(celda, columna, fila);

            columna++;
            if (columna > 6) {
                columna = 0;
                fila++;
            }
        }
    }

    private VBox crearCeldaDia(int dia, LocalDate fecha) {
        VBox celda = new VBox(2);
        celda.setAlignment(Pos.TOP_CENTER);
        celda.setPrefSize(100, 80);
        celda.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-cursor: hand; -fx-padding: 5;");

        Label lblNumero = new Label(String.valueOf(dia));
        lblNumero.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Estilo para día actual
        if (fecha.equals(LocalDate.now())) {
            celda.setStyle(celda.getStyle() + "-fx-background-color: #FFF9C4;");
            lblNumero.setStyle(lblNumero.getStyle() + "-fx-text-fill: #F57C00;");
        }

        // Estilo para día seleccionado
        if (fecha.equals(diaSeleccionado)) {
            celda.setStyle(celda.getStyle() + "-fx-border-color: #2196F3; -fx-border-width: 3;");
        }

        celda.getChildren().add(lblNumero);

        // Cargar indicadores de actividades
        try {
            List<Actividad> actividades = actividadService.listarPorFecha(fecha);

            if (!actividades.isEmpty()) {
                // Agrupar por tipo y mostrar indicadores
                for (TipoActividad tipo : TipoActividad.values()) {
                    long count = actividades.stream()
                            .filter(a -> a.getTipo() == tipo)
                            .count();

                    if (count > 0) {
                        Label lblIndicador = new Label(tipo.getDescripcion().substring(0, 1) + count);
                        lblIndicador.setStyle(
                                "-fx-font-size: 9px; " +
                                        "-fx-padding: 2 4; " +
                                        "-fx-background-radius: 3; " +
                                        "-fx-text-fill: white; " +
                                        getColorPorTipo(tipo));
                        celda.getChildren().add(lblIndicador);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Click handler
        celda.setOnMouseClicked(e -> {
            diaSeleccionado = fecha;
            cargarCalendarioMensual();
            cargarActividadesDia(fecha);
        });

        return celda;
    }

    private String getColorPorTipo(TipoActividad tipo) {
        switch (tipo) {
            case LUDOTEKA: return "-fx-background-color: #2196F3;";
            case GAZTE_LOKALA: return "-fx-background-color: #4CAF50;";
            case HINCHABLES: return "-fx-background-color: #FF9800;";
            case OTRAS: return "-fx-background-color: #757575;";
            default: return "-fx-background-color: #9E9E9E;";
        }
    }

    private void cargarActividadesDia(LocalDate fecha) {
        try {
            List<Actividad> actividades = actividadService.listarPorFecha(fecha);
            listActividadesDia.getItems().clear();

            if (lblDiaSeleccionado != null) {
                lblDiaSeleccionado.setText("Actividades del " + fecha.format(DATE_FORMATTER));
            }

            if (actividades.isEmpty()) {
                listActividadesDia.getItems().add("No hay actividades programadas");
            } else {
                for (Actividad act : actividades) {
                    String monitores = act.getMonitores().isEmpty() ? "Sin monitores" :
                            act.getMonitores().stream()
                                    .map(m -> m.getNombreCompleto())
                                    .collect(Collectors.joining(", "));

                    String texto = String.format("🕐 %s | %s (%s) | Monitores: %s",
                            act.getHorarioFormateado(),
                            act.getNombre(),
                            act.getTipo().getDescripcion(),
                            monitores);

                    listActividadesDia.getItems().add(texto);
                }
            }

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    private void cargarActividadesSemana() {
        try {
            listActividadesSemana.getItems().clear();

            // Calcular fin de semana
            LocalDate finSemana = inicioSemana.plusDays(6);

            lblMesAnio.setText("Semana del " + inicioSemana.format(DATE_FORMATTER) +
                    " al " + finSemana.format(DATE_FORMATTER));

            for (int i = 0; i < 7; i++) {
                LocalDate fecha = inicioSemana.plusDays(i);
                List<Actividad> actividades = actividadService.listarPorFecha(fecha);

                String diaSemana = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                String cabecera = String.format("═══ %s %s ═══",
                        diaSemana.toUpperCase(),
                        fecha.format(DATE_FORMATTER));

                listActividadesSemana.getItems().add(cabecera);

                if (actividades.isEmpty()) {
                    listActividadesSemana.getItems().add("   Sin actividades");
                } else {
                    for (Actividad act : actividades) {
                        String monitores = act.getMonitores().isEmpty() ? "Sin monitores" :
                                act.getMonitores().stream()
                                        .limit(2)
                                        .map(m -> m.getNombreCompleto())
                                        .collect(Collectors.joining(", "));

                        String texto = String.format("   🕐 %s - %s (%s) - %s",
                                act.getHorarioFormateado(),
                                act.getNombre(),
                                act.getTipo().getDescripcion(),
                                monitores);

                        listActividadesSemana.getItems().add(texto);
                    }
                }

                listActividadesSemana.getItems().add(""); // Línea en blanco
            }

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    @FXML
    private void mesAnterior(ActionEvent event) {
        if (vistaMensual) {
            mesActual = mesActual.minusMonths(1);
            cargarCalendarioMensual();
        } else {
            inicioSemana = inicioSemana.minusWeeks(1);
            cargarActividadesSemana();
        }
    }

    @FXML
    private void mesSiguiente(ActionEvent event) {
        if (vistaMensual) {
            mesActual = mesActual.plusMonths(1);
            cargarCalendarioMensual();
        } else {
            inicioSemana = inicioSemana.plusWeeks(1);
            cargarActividadesSemana();
        }
    }

    @FXML
    private void nuevaActividad(ActionEvent event) {
        abrirFormularioActividad(null);
    }

    @FXML
    private void editarActividad(ActionEvent event) {
        int indiceSeleccionado = listActividadesDia.getSelectionModel().getSelectedIndex();

        if (indiceSeleccionado < 0) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione una actividad de la lista");
            return;
        }

        try {
            List<Actividad> actividades = actividadService.listarPorFecha(diaSeleccionado);

            if (indiceSeleccionado < actividades.size()) {
                Actividad actividad = actividades.get(indiceSeleccionado);
                abrirFormularioActividad(actividad);
            }

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    @FXML
    private void eliminarActividad(ActionEvent event) {
        int indiceSeleccionado = listActividadesDia.getSelectionModel().getSelectedIndex();

        if (indiceSeleccionado < 0) {
            AlertUtil.mostrarAdvertencia("Sin selección",
                    "Por favor seleccione una actividad de la lista");
            return;
        }

        try {
            List<Actividad> actividades = actividadService.listarPorFecha(diaSeleccionado);

            if (indiceSeleccionado < actividades.size()) {
                Actividad actividad = actividades.get(indiceSeleccionado);

                boolean confirmado = AlertUtil.mostrarConfirmacion(
                        "Confirmar eliminación",
                        "¿Está seguro de eliminar la actividad '" + actividad.getNombre() + "'?");

                if (confirmado) {
                    actividadService.eliminarActividad(actividad.getIdActividad());
                    AlertUtil.mostrarExito("Actividad eliminada correctamente");
                    refrescarCalendario();
                }
            }

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    private void abrirFormularioActividad(Actividad actividad) {
        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "actividad_form.fxml");

            if (fxmlUrl == null) {
                AlertUtil.mostrarAdvertencia("Formulario no disponible",
                        "El formulario de actividad no se encontró");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            ActividadFormController controller = loader.getController();
            controller.setActividad(actividad);
            controller.setControllerPadre(this);

            Stage stage = new Stage();
            stage.setTitle(actividad == null ? "Nueva Actividad" : "Editar Actividad");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refrescarCalendario() {
        if (vistaMensual) {
            cargarCalendarioMensual();
            cargarActividadesDia(diaSeleccionado);
        } else {
            cargarActividadesSemana();
        }
    }

    private LocalDate calcularInicioSemana(LocalDate fecha) {
        return fecha.minusDays(fecha.getDayOfWeek().getValue() - 1);
    }
    @FXML
    private void handleVolver(ActionEvent event) {
        NavigationUtil.volverAlDashboard(event);
    }
    @FXML
    private void volverDashboard(ActionEvent event) {
        NavigationUtil.volverAlDashboard(event);
    }
}
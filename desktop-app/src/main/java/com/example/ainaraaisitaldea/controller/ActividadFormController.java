package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Actividad;
import com.example.ainaraaisitaldea.model.Usuario;
import com.example.ainaraaisitaldea.model.enums.TipoActividad;
import com.example.ainaraaisitaldea.model.enums.EstadoActividad;
import com.example.ainaraaisitaldea.service.ActividadService;
import com.example.ainaraaisitaldea.service.UsuarioService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import com.example.ainaraaisitaldea.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ActividadFormController {

    @FXML
    private Label lblTitulo;

    @FXML
    private ComboBox<TipoActividad> cmbTipo;

    @FXML
    private TextField txtNombre;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private TextField txtHoraInicio;

    @FXML
    private TextField txtHoraFin;

    @FXML
    private Label lblDescripcion;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private ListView<Usuario> listMonitoresDisponibles;

    @FXML
    private VBox paneHinchables;

    @FXML
    private TextField txtDineroRecibido;

    @FXML
    private TextField txtDineroPagado;

    @FXML
    private Label lblBeneficio;

    @FXML
    private TextArea txtObservaciones;

    @FXML
    private CheckBox chkRepetirSemanal;

    @FXML
    private VBox paneRepeticion;

    @FXML
    private TextField txtSemanas;

    private final ActividadService actividadService;
    private final UsuarioService usuarioService;
    private Actividad actividadActual;
    private CalendarioController controllerPadre;
    private List<Usuario> monitoresDisponibles;

    public ActividadFormController() {
        this.actividadService = new ActividadService();
        this.usuarioService = new UsuarioService();
        this.monitoresDisponibles = new ArrayList<>();
    }

    @FXML
    private void initialize() {
        configurarTipos();
        cargarMonitores();
        configurarListeners();

        dpFecha.setValue(LocalDate.now());
        txtHoraInicio.setText("18:00");
        txtHoraFin.setText("20:00");

        // Permitir selección múltiple de monitores
        listMonitoresDisponibles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void configurarTipos() {
        cmbTipo.getItems().addAll(TipoActividad.values());

        // Listener para mostrar/ocultar campos según tipo
        cmbTipo.setOnAction(e -> {
            TipoActividad tipoSeleccionado = cmbTipo.getValue();

            if (tipoSeleccionado == null) return;

            // Mostrar descripción solo para OTRAS
            boolean esOtras = tipoSeleccionado == TipoActividad.OTRAS;
            lblDescripcion.setVisible(esOtras);
            lblDescripcion.setManaged(esOtras);
            txtDescripcion.setVisible(esOtras);
            txtDescripcion.setManaged(esOtras);

            // Mostrar campos económicos solo para HINCHABLES
            boolean esHinchables = tipoSeleccionado == TipoActividad.HINCHABLES;
            paneHinchables.setVisible(esHinchables);
            paneHinchables.setManaged(esHinchables);
        });
    }

    private void cargarMonitores() {
        try {
            monitoresDisponibles = usuarioService.listarMonitores();
            listMonitoresDisponibles.getItems().clear();
            listMonitoresDisponibles.getItems().addAll(monitoresDisponibles);

            // Configurar cómo se muestra cada monitor
            listMonitoresDisponibles.setCellFactory(lv -> new ListCell<Usuario>() {
                @Override
                protected void updateItem(Usuario usuario, boolean empty) {
                    super.updateItem(usuario, empty);
                    if (empty || usuario == null) {
                        setText(null);
                    } else {
                        setText(usuario.getNombreCompleto() + " (@" + usuario.getUsername() + ")");
                    }
                }
            });

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
        }
    }

    private void configurarListeners() {
        // Mostrar/ocultar opciones de repetición
        chkRepetirSemanal.setOnAction(e -> {
            boolean mostrar = chkRepetirSemanal.isSelected();
            paneRepeticion.setVisible(mostrar);
            paneRepeticion.setManaged(mostrar);
            if (mostrar) {
                txtSemanas.setText("4"); // Valor por defecto
            }
        });

        // Calcular beneficio automáticamente
        txtDineroRecibido.textProperty().addListener((obs, old, nuevo) -> calcularBeneficio());
        txtDineroPagado.textProperty().addListener((obs, old, nuevo) -> calcularBeneficio());
    }

    private void calcularBeneficio() {
        try {
            double ingresos = Double.parseDouble(txtDineroRecibido.getText().replace(",", "."));
            double gastos = Double.parseDouble(txtDineroPagado.getText().replace(",", "."));
            double beneficio = ingresos - gastos;

            lblBeneficio.setText(String.format("%.2f €", beneficio));

            if (beneficio >= 0) {
                lblBeneficio.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
            } else {
                lblBeneficio.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
            }

        } catch (NumberFormatException e) {
            lblBeneficio.setText("0.00 €");
        }
    }

    public void setActividad(Actividad actividad) {
        this.actividadActual = actividad;

        if (actividad != null) {
            lblTitulo.setText("Editar Actividad");

            cmbTipo.setValue(actividad.getTipo());
            txtNombre.setText(actividad.getNombre());
            dpFecha.setValue(actividad.getFecha());
            txtHoraInicio.setText(actividad.getHoraInicio().toString());
            txtHoraFin.setText(actividad.getHoraFin().toString());

            if (actividad.getDescripcion() != null) {
                txtDescripcion.setText(actividad.getDescripcion());
            }

            if (actividad.getTipo() == TipoActividad.HINCHABLES) {
                txtDineroRecibido.setText(String.valueOf(actividad.getDineroRecibido()));
                txtDineroPagado.setText(String.valueOf(actividad.getDineroPagadoMonitores()));
                calcularBeneficio();
            }

            if (actividad.getObservaciones() != null) {
                txtObservaciones.setText(actividad.getObservaciones());
            }

            // Seleccionar monitores asignados
            if (actividad.getMonitores() != null && !actividad.getMonitores().isEmpty()) {
                for (Usuario monitor : actividad.getMonitores()) {
                    for (int i = 0; i < listMonitoresDisponibles.getItems().size(); i++) {
                        Usuario m = listMonitoresDisponibles.getItems().get(i);
                        if (m.getIdUsuario().equals(monitor.getIdUsuario())) {
                            listMonitoresDisponibles.getSelectionModel().select(i);
                            break;
                        }
                    }
                }
            }

            // Ocultar opción de repetir al editar
            chkRepetirSemanal.setVisible(false);
            chkRepetirSemanal.setManaged(false);
        }
    }

    public void setControllerPadre(CalendarioController controller) {
        this.controllerPadre = controller;
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            // Validaciones
            if (cmbTipo.getValue() == null) {
                AlertUtil.mostrarAdvertencia("Validación", "El tipo de actividad es obligatorio");
                return;
            }

            if (txtNombre.getText().trim().isEmpty()) {
                AlertUtil.mostrarAdvertencia("Validación", "El nombre es obligatorio");
                return;
            }

            if (dpFecha.getValue() == null) {
                AlertUtil.mostrarAdvertencia("Validación", "La fecha es obligatoria");
                return;
            }


            // Validar formato de horas
            LocalTime horaInicio, horaFin;
            try {
                horaInicio = LocalTime.parse(txtHoraInicio.getText().trim());
                horaFin = LocalTime.parse(txtHoraFin.getText().trim());
            } catch (Exception e) {
                AlertUtil.mostrarAdvertencia("Validación",
                        "Formato de hora inválido. Use HH:MM (Ej: 18:00)");
                return;
            }

            if (horaInicio.isAfter(horaFin)) {
                AlertUtil.mostrarAdvertencia("Validación",
                        "La hora de inicio debe ser anterior a la hora de fin");
                return;
            }

            // Validar descripción para tipo OTRAS
            if (cmbTipo.getValue() == TipoActividad.OTRAS && txtDescripcion.getText().trim().isEmpty()) {
                AlertUtil.mostrarAdvertencia("Validación",
                        "La descripción es obligatoria para actividades de tipo 'Otras'");
                return;
            }

            // Obtener monitores seleccionados
            List<Usuario> monitoresSeleccionados = new ArrayList<>(
                    listMonitoresDisponibles.getSelectionModel().getSelectedItems());

            // Si es repetición semanal
            if (chkRepetirSemanal.isSelected() && actividadActual == null) {
                int numSemanas;
                try {
                    numSemanas = Integer.parseInt(txtSemanas.getText().trim());
                    if (numSemanas < 1 || numSemanas > 52) {
                        AlertUtil.mostrarAdvertencia("Validación",
                                "El número de semanas debe estar entre 1 y 52");
                        return;
                    }
                } catch (NumberFormatException e) {
                    AlertUtil.mostrarAdvertencia("Validación",
                            "El número de semanas debe ser un número válido");
                    return;
                }

                crearActividadesRepetidas(numSemanas, horaInicio, horaFin, monitoresSeleccionados);

            } else {
                // Crear o actualizar actividad única
                Actividad actividad = actividadActual != null ? actividadActual : new Actividad();

                actividad.setTipo(cmbTipo.getValue());
                actividad.setNombre(txtNombre.getText().trim());
                actividad.setFecha(dpFecha.getValue());
                actividad.setHoraInicio(horaInicio);
                actividad.setHoraFin(horaFin);
                actividad.setDescripcion(txtDescripcion.getText().trim());
                actividad.setObservaciones(txtObservaciones.getText().trim());
                actividad.setEstado(EstadoActividad.PROGRAMADA);
                actividad.setMonitores(monitoresSeleccionados);

                // Si es hinchables, guardar datos económicos
                if (cmbTipo.getValue() == TipoActividad.HINCHABLES) {
                    actividad.setDineroRecibido(
                            Double.parseDouble(txtDineroRecibido.getText().replace(",", ".")));
                    actividad.setDineroPagadoMonitores(
                            Double.parseDouble(txtDineroPagado.getText().replace(",", ".")));
                } else {
                    actividad.setDineroRecibido(0.0);
                    actividad.setDineroPagadoMonitores(0.0);
                }

                if (actividadActual == null) {
                    actividadService.crearActividad(actividad);
                    AlertUtil.mostrarExito("Actividad creada correctamente");
                } else {
                    actividadService.actualizarActividad(actividad);
                    AlertUtil.mostrarExito("Actividad actualizada correctamente");
                }
            }

            if (controllerPadre != null) {
                controllerPadre.refrescarCalendario();
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
    private void handleVolver(ActionEvent event) {
        NavigationUtil.volverAlDashboard(event);
    }

    private void crearActividadesRepetidas(int numSemanas, LocalTime horaInicio, LocalTime horaFin,
                                           List<Usuario> monitores) throws Exception {

        LocalDate fechaBase = dpFecha.getValue();
        int actividadesCreadas = 0;

        for (int i = 0; i < numSemanas; i++) {
            Actividad actividad = new Actividad();

            actividad.setTipo(cmbTipo.getValue());
            actividad.setNombre(txtNombre.getText().trim());
            actividad.setFecha(fechaBase.plusWeeks(i));
            actividad.setHoraInicio(horaInicio);
            actividad.setHoraFin(horaFin);
            actividad.setDescripcion(txtDescripcion.getText().trim());
            actividad.setObservaciones(txtObservaciones.getText().trim());
            actividad.setEstado(EstadoActividad.PROGRAMADA);
            actividad.setMonitores(new ArrayList<>(monitores)); // Nueva lista para cada actividad

            if (cmbTipo.getValue() == TipoActividad.HINCHABLES) {
                actividad.setDineroRecibido(
                        Double.parseDouble(txtDineroRecibido.getText().replace(",", ".")));
                actividad.setDineroPagadoMonitores(
                        Double.parseDouble(txtDineroPagado.getText().replace(",", ".")));
            } else {
                actividad.setDineroRecibido(0.0);
                actividad.setDineroPagadoMonitores(0.0);
            }

            actividadService.crearActividad(actividad);
            actividadesCreadas++;
        }

        AlertUtil.mostrarExito(
                "Se crearon " + actividadesCreadas + " actividades repetidas correctamente\n" +
                        "Fechas: desde " + fechaBase.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        " hasta " + fechaBase.plusWeeks(numSemanas - 1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @FXML
    private void cerrar(ActionEvent event) {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}
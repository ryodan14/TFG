package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.component.DiaHinchableCard;
import com.example.ainaraaisitaldea.dao.DetalleDiaHinchableDAO;
import com.example.ainaraaisitaldea.model.Actividad;
import com.example.ainaraaisitaldea.model.DetalleDiaHinchable;
import com.example.ainaraaisitaldea.model.enums.EstadoActividad;
import com.example.ainaraaisitaldea.model.enums.TipoActividad;
import com.example.ainaraaisitaldea.service.ActividadService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del formulario mejorado de salidas de hinchables
 * Permite múltiples días con datos opcionales
 */
public class HinchablesFormController {

    @FXML private TextField txtNombre;
    @FXML private TextArea txtObservaciones;
    @FXML private VBox containerDias;
    @FXML private TextField txtGastosMonitores;
    @FXML private TextField txtTotalIngresos;
    @FXML private Label lblBeneficioNeto;
    @FXML private Button btnAgregarDia;

    private final ActividadService actividadService;
    private final DetalleDiaHinchableDAO detalleDAO;

    private Actividad actividadActual;
    private HinchablesController controllerPadre;
    private boolean modoEdicion = false;

    public HinchablesFormController() {
        this.actividadService = new ActividadService();
        this.detalleDAO = new DetalleDiaHinchableDAO();
    }

    @FXML
    private void initialize() {
        configurarValidaciones();
        agregarDia(); // Agregar un día por defecto
    }
    private void configurarValidaciones() {
        // Total ingresos
        txtTotalIngresos.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[0-9.,]*")) {
                // Solo permitir números, coma y punto
                txtTotalIngresos.setText(newVal.replaceAll("[^0-9.,]", ""));
            }
            calcularBeneficio();
        });

        // Gastos monitores
        txtGastosMonitores.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[0-9.,]*")) {
                txtGastosMonitores.setText(newVal.replaceAll("[^0-9.,]", ""));
            }
            calcularBeneficio();
        });
    }

    // Método para obtener el valor como double correctamente
    private double parsearImporte(TextField field) {
        String texto = field.getText();
        if (texto == null || texto.trim().isEmpty()) return 0.0;

        try {
            // Reemplazar coma por punto para convertir a double
            return Double.parseDouble(texto.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Agregar un nuevo día
     */
    @FXML
    private void agregarDia() {
        DiaHinchableCard card = new DiaHinchableCard();

        // Configurar fecha por defecto (hoy o siguiente al último día)
        if (containerDias.getChildren().isEmpty()) {
            card.getDetalle().setFecha(LocalDate.now());
        } else {
            // Sugerir el día siguiente al último agregado
            LocalDate ultimaFecha = obtenerUltimaFecha();
            if (ultimaFecha != null) {
                card.setDetalle(new DetalleDiaHinchable(ultimaFecha.plusDays(0)));
            }
        }

        // Callback para eliminar
        card.setOnEliminar(() -> eliminarDia(card));

        // Callback para recalcular totales cuando cambie algo
        card.setOnCambio(this::calcularBeneficio);

        containerDias.getChildren().add(card);
        calcularBeneficio();
    }

    /**
     * Eliminar un día
     */
    private void eliminarDia(DiaHinchableCard card) {
        if (containerDias.getChildren().size() <= 1) {
            AlertUtil.mostrarAdvertencia("No se puede eliminar",
                    "Debe haber al menos un día. Si no deseas usar días múltiples, deja los campos vacíos.");
            return;
        }

        containerDias.getChildren().remove(card);
        calcularBeneficio();
    }

    /**
     * Obtener la última fecha agregada
     */
    private LocalDate obtenerUltimaFecha() {
        if (containerDias.getChildren().isEmpty()) {
            return null;
        }

        DiaHinchableCard ultimaCard = (DiaHinchableCard) containerDias.getChildren()
                .get(containerDias.getChildren().size() - 1);
        return ultimaCard.getFecha();
    }

    /**
     * Calcular y mostrar el total de ingresos y beneficio
     */
    private void calcularBeneficio() {
        double totalIngresos = 0.0;

        // Primero intentar obtener el total ingresado manualmente
        String ingresosText = txtTotalIngresos.getText().trim();
        if (!ingresosText.isEmpty()) {
            try {
                totalIngresos = Double.parseDouble(ingresosText.replace(",", ".").replace("€", "").trim());
            } catch (NumberFormatException e) {
                totalIngresos = 0.0;
            }
        }


        // Calcular beneficio
        double gastos = 0.0;
        try {
            String gastosText = txtGastosMonitores.getText().trim();
            if (!gastosText.isEmpty()) {
                gastos = Double.parseDouble(gastosText.replace(",", ".").replace("€", "").trim());
            }
        } catch (NumberFormatException e) {
            gastos = 0.0;
        }

        double beneficio = totalIngresos - gastos;
        lblBeneficioNeto.setText(String.format("%.2f €", beneficio));

        // Color según el beneficio
        if (beneficio >= 0) {
            lblBeneficioNeto.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: green;");
        } else {
            lblBeneficioNeto.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: red;");
        }
    }

    /**
     * Guardar la salida
     */
    @FXML
    private void guardar() {
        // Validar solo el nombre (único campo obligatorio)
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            AlertUtil.mostrarAdvertencia("Campo obligatorio",
                    "El nombre/evento es obligatorio");
            txtNombre.requestFocus();
            return;
        }

        // Validar que todos los días tengan al menos fecha
        List<DiaHinchableCard> diasInvalidos = new ArrayList<>();
        for (var node : containerDias.getChildren()) {
            if (node instanceof DiaHinchableCard) {
                DiaHinchableCard card = (DiaHinchableCard) node;
                if (!card.esValido()) {
                    diasInvalidos.add(card);
                }
            }
        }

        if (!diasInvalidos.isEmpty()) {
            AlertUtil.mostrarAdvertencia("Días incompletos",
                    "Algunos días no tienen fecha seleccionada. Por favor completa o elimina esos días.");
            return;
        }

        try {
            if (modoEdicion) {
                actualizarActividad();
            } else {
                crearNuevaActividad();
            }

            AlertUtil.mostrarExito("Salida guardada correctamente");

            if (controllerPadre != null) {
                controllerPadre.refrescarDatos();
            }

            cerrar();

        } catch (Exception e) {
            AlertUtil.mostrarErrorBD(e);
            e.printStackTrace();
        }
    }

    /**
     * Crear nueva actividad
     */
    private void crearNuevaActividad() throws Exception {
        Actividad actividad = construirActividad();

        // Insertar actividad principal
        int idActividad = actividadService.crearActividad(actividad).getIdActividad();

        if (idActividad > 0) {
            // Insertar detalles de días
            guardarDetallesDias(idActividad);
        }
    }

    /**
     * Actualizar actividad existente
     */
    private void actualizarActividad() throws Exception {
        Actividad actividad = construirActividad();
        actividad.setIdActividad(actividadActual.getIdActividad());

        // Actualizar actividad principal
        actividadService.actualizarActividad(actividad);

        // Eliminar detalles antiguos y crear nuevos
        detalleDAO.eliminarPorActividad(actividad.getIdActividad());
        guardarDetallesDias(actividad.getIdActividad());
    }

    /**
     * Construir objeto Actividad desde el formulario
     */
    private Actividad construirActividad() {
        Actividad actividad = new Actividad();

        // Datos básicos
        actividad.setNombre(txtNombre.getText().trim());
        actividad.setObservaciones(txtObservaciones.getText());
        actividad.setTipo(TipoActividad.HINCHABLES);
        actividad.setEstado(EstadoActividad.REALIZADA);

        // Fecha principal (primera fecha de los días)
        LocalDate primeraFecha = obtenerPrimeraFecha();
        actividad.setFecha(primeraFecha != null ? primeraFecha : LocalDate.now());

        // Horarios null (se gestionan por día)
        actividad.setHoraInicio(null);
        actividad.setHoraFin(null);

        // Importes - usar el total ingresado directamente
        double totalIngresos = 0.0;
        try {
            String ingresosText = txtTotalIngresos.getText().trim();
            if (!ingresosText.isEmpty()) {
                totalIngresos = Double.parseDouble(ingresosText.replace(",", ".").replace("€", "").trim());
            }
        } catch (NumberFormatException e) {
            totalIngresos = 0.0;
        }
        actividad.setDineroRecibido(totalIngresos);

        double gastos = 0.0;
        try {
            String gastosText = txtGastosMonitores.getText().trim();
            if (!gastosText.isEmpty()) {
                gastos = Double.parseDouble(gastosText.replace(",", ".").replace("€", "").trim());
            }
        } catch (NumberFormatException e) {
            gastos = 0.0;
        }
        actividad.setDineroPagadoMonitores(gastos);

        return actividad;
    }

    /**
     * Guardar detalles de días en BD
     */
    private void guardarDetallesDias(int idActividad) throws Exception {
        List<DetalleDiaHinchable> detalles = new ArrayList<>();

        for (var node : containerDias.getChildren()) {
            if (node instanceof DiaHinchableCard) {
                DiaHinchableCard card = (DiaHinchableCard) node;
                DetalleDiaHinchable detalle = card.getDetalle();
                detalle.setIdActividad(idActividad);
                detalles.add(detalle);
            }
        }

        if (!detalles.isEmpty()) {
            detalleDAO.insertarVarios(detalles);
        }
    }

    /**
     * Obtener la primera fecha de los días
     */
    private LocalDate obtenerPrimeraFecha() {
        for (var node : containerDias.getChildren()) {
            if (node instanceof DiaHinchableCard) {
                DiaHinchableCard card = (DiaHinchableCard) node;
                LocalDate fecha = card.getFecha();
                if (fecha != null) {
                    return fecha;
                }
            }
        }
        return null;
    }



    /**
     * Cargar actividad para edición
     */
    public void setActividad(Actividad actividad) {
        if (actividad == null) {
            return;
        }

        this.actividadActual = actividad;
        this.modoEdicion = true;

        // Cargar datos generales
        txtNombre.setText(actividad.getNombre());
        txtObservaciones.setText(actividad.getObservaciones());

        // Cargar importe total
        if (actividad.getDineroRecibido() != null && actividad.getDineroRecibido() > 0) {
            txtTotalIngresos.setText(String.format("%.2f", actividad.getDineroRecibido()));
        }

        if (actividad.getDineroPagadoMonitores() != null) {
            txtGastosMonitores.setText(String.format("%.2f", actividad.getDineroPagadoMonitores()));
        }

        // Cargar detalles de días
        try {
            List<DetalleDiaHinchable> detalles = detalleDAO.obtenerPorActividad(actividad.getIdActividad());

            if (!detalles.isEmpty()) {
                // Limpiar el día por defecto
                containerDias.getChildren().clear();

                // Cargar cada día
                for (DetalleDiaHinchable detalle : detalles) {
                    DiaHinchableCard card = new DiaHinchableCard();
                    card.setDetalle(detalle);
                    card.setOnEliminar(() -> eliminarDia(card));
                    card.setOnCambio(this::calcularBeneficio);
                    containerDias.getChildren().add(card);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudieron cargar los detalles de días");
        }

        calcularBeneficio();
    }

    /**
     * Establecer el controlador padre
     */
    public void setControllerPadre(HinchablesController controllerPadre) {
        this.controllerPadre = controllerPadre;
    }

    /**
     * Cerrar el formulario
     */
    @FXML
    private void cerrar() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}
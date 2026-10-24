package com.example.ainaraaisitaldea.component;

import com.example.ainaraaisitaldea.model.DetalleDiaHinchable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Componente visual para representar un día de salida de hinchable
 * Contiene: fecha, horario, importe y observaciones
 */
public class DiaHinchableCard extends VBox {

    private final DatePicker dpFecha;
    private final TextField txtHoraInicio;
    private final TextField txtHoraFin;
    private final Button btnEliminar;

    private DetalleDiaHinchable detalle;
    private Runnable onEliminar;
    private Runnable onCambio;

    public DiaHinchableCard() {
        // Configurar el contenedor
        setSpacing(8);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; " +
                "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Crear controles
        dpFecha = new DatePicker();
        dpFecha.setPromptText("Fecha");
        dpFecha.setPrefWidth(140);

        txtHoraInicio = new TextField();
        txtHoraInicio.setPromptText("HH:MM");
        txtHoraInicio.setPrefWidth(70);

        txtHoraFin = new TextField();
        txtHoraFin.setPromptText("HH:MM");
        txtHoraFin.setPrefWidth(70);

        btnEliminar = new Button("X");
        btnEliminar.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-padding: 5 10;");
        btnEliminar.setTooltip(new Tooltip("Eliminar este día"));

        // Construir UI
        construirUI();

        // Configurar listeners
        configurarListeners();
    }

    private void construirUI() {
        // Fila principal
        HBox filaPrincipal = new HBox(85); // menos espacio entre elementos
        filaPrincipal.setAlignment(Pos.CENTER_LEFT);

        // Fecha/día
        VBox boxFecha = new VBox(0); // sin espacio extra entre label y datepicker
        Label lblDia = new Label("Fecha:");
        lblDia.setStyle("-fx-font-weight: bold;");
        boxFecha.getChildren().addAll(lblDia, dpFecha);

        // Horario
        VBox boxHorario = new VBox(0); // sin espacio extra entre label y horario
        Label lblHorario = new Label("Horario (opcional):");
        lblHorario.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        HBox horarioBox = new HBox(1); // menos espacio entre inicio y fin
        horarioBox.getChildren().addAll(txtHoraInicio, new Label("-"), txtHoraFin);
        boxHorario.getChildren().addAll(lblHorario, horarioBox);

        // Espaciador para empujar horario a la derecha
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botón eliminar al final
        HBox.setHgrow(btnEliminar, Priority.NEVER);

        filaPrincipal.getChildren().addAll(boxFecha, spacer, boxHorario, btnEliminar);

        // Añadir al contenedor principal
        getChildren().add(filaPrincipal);
    }


    private void configurarListeners() {
        // Validar horarios mientras se escriben
        txtHoraInicio.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormatoHora(txtHoraInicio);
            if (onCambio != null) onCambio.run();
        });

        txtHoraFin.textProperty().addListener((obs, oldVal, newVal) -> {
            validarFormatoHora(txtHoraFin);
            if (onCambio != null) onCambio.run();
        });


        // Fecha cambiada
        dpFecha.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (onCambio != null) onCambio.run();
        });

        // Botón eliminar
        btnEliminar.setOnAction(e -> {
            if (onEliminar != null) {
                onEliminar.run();
            }
        });
    }

    private void validarFormatoHora(TextField field) {
        String text = field.getText();
        if (!text.matches("[0-9:]*")) {
            field.setText(text.replaceAll("[^0-9:]", ""));
        }
    }

    private void validarFormatoNumero(TextField field) {
        String text = field.getText();
        // Guardar la posición del cursor
        int pos = field.getCaretPosition();
        // Reemplazar solo caracteres inválidos
        String nuevo = text.replaceAll("[^0-9.]", "");
        if (!nuevo.equals(text)) {
            field.setText(nuevo);
            // Colocar el cursor al final
            field.positionCaret(nuevo.length());
        }
    }


    /**
     * Cargar datos de un DetalleDiaHinchable
     */
    public void setDetalle(DetalleDiaHinchable detalle) {
        this.detalle = detalle;

        if (detalle.getFecha() != null) {
            dpFecha.setValue(detalle.getFecha());
        }

        if (detalle.getHoraInicio() != null) {
            txtHoraInicio.setText(detalle.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        if (detalle.getHoraFin() != null) {
            txtHoraFin.setText(detalle.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")));
        }


    }

    /**
     * Obtener DetalleDiaHinchable con los datos actuales
     */
    public DetalleDiaHinchable getDetalle() {
        DetalleDiaHinchable detalle = new DetalleDiaHinchable();

        detalle.setFecha(dpFecha.getValue());
        detalle.setHoraInicio(parsearHora(txtHoraInicio.getText()));
        detalle.setHoraFin(parsearHora(txtHoraFin.getText()));

        return detalle;
    }

    /**
     * Validar que tenga al menos la fecha
     */
    public boolean esValido() {
        return dpFecha.getValue() != null;
    }

    /**
     * Obtener mensaje de error si no es válido
     */
    public String getMensajeError() {
        if (dpFecha.getValue() == null) {
            return "Debe seleccionar una fecha";
        }

        // Validar horario si está presente
        if (!txtHoraInicio.getText().isEmpty() || !txtHoraFin.getText().isEmpty()) {
            if (parsearHora(txtHoraInicio.getText()) == null ||
                    parsearHora(txtHoraFin.getText()) == null) {
                return "Formato de horario incorrecto (debe ser HH:MM)";
            }
        }

        return null;
    }


    /**
     * Obtener la fecha
     */
    public LocalDate getFecha() {
        return dpFecha.getValue();
    }

    private LocalTime parsearHora(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(texto, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Double parsearImporte(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(texto.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setOnEliminar(Runnable onEliminar) {
        this.onEliminar = onEliminar;
    }

    public void setOnCambio(Runnable onCambio) {
        this.onCambio = onCambio;
    }
}
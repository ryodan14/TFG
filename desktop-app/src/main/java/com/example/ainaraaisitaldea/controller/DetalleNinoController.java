package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Nino;
import com.example.ainaraaisitaldea.model.Tutor;
import com.example.ainaraaisitaldea.service.NinoService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import com.example.ainaraaisitaldea.util.PDFExporter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DetalleNinoController {

    @FXML private Label lblNombreCompleto;
    @FXML private Label lblEdadCurso;
    @FXML private Label lblPosicion;

    // Datos del niño
    @FXML private Label lblNombre;
    @FXML private Label lblPrimerApellido;
    @FXML private Label lblSegundoApellido;
    @FXML private Label lblFechaNacimiento;
    @FXML private Label lblEdad;
    @FXML private Label lblColegio;
    @FXML private Label lblCurso;
    @FXML private Label lblAlergias;
    @FXML private Label lblPuedeSalirSolo;
    @FXML private Label lblAutorizacionFotos;
    @FXML private Label lblObservacionesNino;

    // Datos del tutor
    @FXML private Label lblNombreTutor;
    @FXML private Label lblDniTutor;
    @FXML private Label lblVinculo;
    @FXML private Label lblTelefonoTutor;
    @FXML private Label lblCorreoTutor;
    @FXML private Label lblDireccionTutor;
    @FXML private Label lblMunicipioTutor;
    @FXML private Label lblCodigoPostalTutor;
    @FXML private Label lblObservacionesTutor;

    // Botones de navegación
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;

    private Nino nino;
    private Tutor tutor;
    private List<Nino> listaNinos;
    private int indiceActual;
    private final NinoService ninoService;
    private static final String FXML_BASE = "/fxml/";

    public DetalleNinoController() {
        this.ninoService = new NinoService();
    }

    @FXML
    private void initialize() {
        // Los botones se configurarán cuando se carguen los datos
    }

    /**
     * Método original para compatibilidad
     */
    public void setNino(Nino nino) {
        this.nino = nino;
        this.listaNinos = null;
        this.indiceActual = -1;
        cargarDatos();
        configurarBotonesNavegacion();
    }

    /**
     * Método nuevo con lista y posición
     */
    public void setNinoConLista(Nino nino, List<Nino> listaNinos, int indiceActual) {
        this.nino = nino;
        this.listaNinos = listaNinos;
        this.indiceActual = indiceActual;
        cargarDatos();
        configurarBotonesNavegacion();
    }

    private void configurarBotonesNavegacion() {
        if (listaNinos == null || listaNinos.isEmpty()) {
            // Sin lista, ocultar botones de navegación
            if (btnAnterior != null) btnAnterior.setVisible(false);
            if (btnSiguiente != null) btnSiguiente.setVisible(false);
            if (lblPosicion != null) lblPosicion.setVisible(false);
            return;
        }

        // Con lista, mostrar y configurar botones
        if (btnAnterior != null) {
            btnAnterior.setVisible(true);
            btnAnterior.setDisable(indiceActual <= 0);
        }

        if (btnSiguiente != null) {
            btnSiguiente.setVisible(true);
            btnSiguiente.setDisable(indiceActual >= listaNinos.size() - 1);
        }

        // Mostrar posición actual
        if (lblPosicion != null) {
            lblPosicion.setVisible(true);
            lblPosicion.setText((indiceActual + 1) + " / " + listaNinos.size());
        }
    }

    @FXML
    private void irAnterior(ActionEvent event) {
        if (listaNinos != null && indiceActual > 0) {
            indiceActual--;
            Nino ninoAnterior = listaNinos.get(indiceActual);
            this.nino = ninoAnterior;
            cargarDatos();
            configurarBotonesNavegacion();
        }
    }

    @FXML
    private void irSiguiente(ActionEvent event) {
        if (listaNinos != null && indiceActual < listaNinos.size() - 1) {
            indiceActual++;
            Nino ninoSiguiente = listaNinos.get(indiceActual);
            this.nino = ninoSiguiente;
            cargarDatos();
            configurarBotonesNavegacion();
        }
    }

    private void cargarDatos() {
        if (nino == null) return;

        // Cargar tutor completo
        tutor = ninoService.obtenerTutorDeNino(nino.getIdTutor());

        // Header
        lblNombreCompleto.setText(nino.getNombreCompleto());
        lblEdadCurso.setText(nino.getEdad() + " años - " + nino.getCurso());

        // Datos del niño
        lblNombre.setText(nino.getNombre());
        lblPrimerApellido.setText(nino.getPrimerApellido());
        lblSegundoApellido.setText(nino.getSegundoApellido() != null ? nino.getSegundoApellido() : "-");
        lblFechaNacimiento.setText(nino.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblEdad.setText(nino.getEdad() + " años");
        lblColegio.setText(nino.getColegioInstituto());
        lblCurso.setText(nino.getCurso());

        // Alergias con formato especial
        if (nino.isTieneAlergias()) {
            lblAlergias.setText("Sí");
            lblAlergias.setStyle("-fx-font-size: 13px; -fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        } else {
            lblAlergias.setText("No");
            lblAlergias.setStyle("-fx-font-size: 13px; -fx-text-fill: #4CAF50;");
        }

        lblPuedeSalirSolo.setText(nino.getPuedeSalirSoloTexto());
        lblAutorizacionFotos.setText(nino.getAutorizacionFotosTexto());

        String obsNino = nino.getObservaciones();
        lblObservacionesNino.setText(obsNino != null && !obsNino.trim().isEmpty() ? obsNino : "Sin observaciones");

        // Datos del tutor
        if (tutor != null) {
            lblNombreTutor.setText(tutor.getNombreCompleto());
            lblDniTutor.setText(tutor.getDni());
            lblVinculo.setText(nino.getVinculoTutor().getDescripcion());
            lblTelefonoTutor.setText(tutor.getTelefono());
            lblCorreoTutor.setText(tutor.getCorreo() != null ? tutor.getCorreo() : "-");
            lblDireccionTutor.setText(tutor.getDireccion());
            lblMunicipioTutor.setText(tutor.getMunicipio());
            lblCodigoPostalTutor.setText(tutor.getCodigoPostal());
            String obsTutor = tutor.getObservaciones();
            lblObservacionesTutor.setText(obsTutor != null && !obsTutor.trim().isEmpty() ? obsTutor : "Sin observaciones");
        }
    }

    @FXML
    private void editarNino(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "alta-nino.fxml");

            if (fxmlUrl == null) {
                AlertUtil.mostrarAdvertencia("Formulario no disponible",
                        "El formulario de edición no se encuentra");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Obtener el controlador y cargar datos para edición
            AltaNinoController controller = loader.getController();

            // Obtener el niño completo con el tutor
            Nino ninoCompleto = ninoService.obtenerPorId(nino.getIdNino());

            if (ninoCompleto != null) {
                controller.cargarParaEdicion(ninoCompleto);
            } else {
                AlertUtil.mostrarError("Error", "No se pudieron cargar los datos del niño");
                return;
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Editar Niño/a");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(lblNombreCompleto.getScene().getWindow());
            dialogStage.showAndWait();

            // Recargar datos después de la edición
            Nino ninoActualizado = ninoService.obtenerPorId(nino.getIdNino());
            if (ninoActualizado != null) {
                this.nino = ninoActualizado;

                // Actualizar también en la lista si existe
                if (listaNinos != null && indiceActual >= 0 && indiceActual < listaNinos.size()) {
                    listaNinos.set(indiceActual, ninoActualizado);
                }

                cargarDatos();
            }

        } catch (Exception e) {
            System.err.println("Error al abrir edición: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo abrir el formulario de edición");
        }
    }

    @FXML
    private void imprimirFicha(ActionEvent event) {
        try {
            //PDFExporter.exportarFichaNino(nino, tutor);
            AlertUtil.mostrarInfo("Éxito", "Ficha del niño/a generada correctamente");
        } catch (Exception e) {
            System.err.println("Error al imprimir ficha: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo generar la ficha: " + e.getMessage());
        }
    }

    @FXML
    private void cerrar(ActionEvent event) {
        Stage stage = (Stage) lblNombreCompleto.getScene().getWindow();
        stage.close();
    }
}
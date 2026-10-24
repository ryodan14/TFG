package com.example.ainaraaisitaldea.controller;

import com.example.ainaraaisitaldea.model.Actividad;
import com.example.ainaraaisitaldea.service.ActividadService;
import com.example.ainaraaisitaldea.service.AuthService;
import com.example.ainaraaisitaldea.service.MovimientoEconomicoService;
import com.example.ainaraaisitaldea.util.AlertUtil;
import com.example.ainaraaisitaldea.util.NavigationUtil;
import com.example.ainaraaisitaldea.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblFechaHoy;
    @FXML private ListView<String> listProximasActividades;

    // Labels de Economía Global
    @FXML private Label lblIngresosTotal;
    @FXML private Label lblGastosTotal;
    @FXML private Label lblBeneficioNeto;

    // Labels de Facturas
    @FXML private Label lblFacturasPendientes;
    @FXML private Label lblFacturasPagadas;
    @FXML private Label lblFacturasTotal;

    // Labels de Presupuestos
    @FXML private Label lblPresupuestosEnviados;
    @FXML private Label lblPresupuestosAceptados;
    @FXML private Label lblPresupuestosPendientes;

    // Botón de usuarios (solo para admin)
    @FXML private Button btnUsuarios;

    private final AuthService authService;
    private final ActividadService actividadService;
    private final SessionManager sessionManager;

    private static final String FXML_BASE = "/fxml/";

    public DashboardController() {
        this.authService = new AuthService();
        this.actividadService = new ActividadService();
        this.sessionManager = SessionManager.getInstance();
    }

    @FXML
    private void initialize() {
        cargarDatosIniciales();
        cargarEstadisticasEconomia();
        cargarEstadisticasFacturas();
        cargarEstadisticasPresupuestos();
        configurarVisibilidadSegunRol();
    }

    private void cargarDatosIniciales() {
        // Mostrar bienvenida
        if (sessionManager.getUsuarioActual() != null) {
            String mensaje = sessionManager.getUsuarioActual().getNombreCompleto();
            if (lblBienvenida != null) {
                lblBienvenida.setText("");
            }
        }

        // Mostrar fecha
        if (lblFechaHoy != null) {
            LocalDate hoy = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",
                    new Locale("es", "ES"));
            String fechaTexto = hoy.format(formatter);
            // Capitalizar primera letra
            fechaTexto = fechaTexto.substring(0, 1).toUpperCase() + fechaTexto.substring(1);
            lblFechaHoy.setText(fechaTexto);
        }

        // Cargar próximas 5 actividades
        cargarProximasActividades();
    }

    private void cargarProximasActividades() {
        if (listProximasActividades == null) return;

        try {
            List<Actividad> actividades = actividadService.listarProximasActividades(5);

            listProximasActividades.getItems().clear();

            if (actividades.isEmpty()) {
                listProximasActividades.getItems().add("No hay actividades programadas");
            } else {
                for (Actividad act : actividades) {
                    String texto = String.format("%s - %s - %s",
                            act.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            act.getNombre(),
                            act.getTipo().getDescripcion());
                    listProximasActividades.getItems().add(texto);
                }
            }

        } catch (Exception e) {
            System.err.println("Error al cargar próximas actividades: " + e.getMessage());
            listProximasActividades.getItems().add("Error al cargar actividades");
        }
    }

    private void cargarEstadisticasEconomia() {
        try {
            MovimientoEconomicoService service = new MovimientoEconomicoService();
            Map<String, BigDecimal> totales = service.calcularTotalesConsolidados();

            BigDecimal ingresos = totales.getOrDefault("ingresos", BigDecimal.ZERO);
            BigDecimal gastos = totales.getOrDefault("gastos", BigDecimal.ZERO);
            BigDecimal beneficio = totales.getOrDefault("beneficio", BigDecimal.ZERO);

            if (lblIngresosTotal != null) {
                lblIngresosTotal.setText(String.format("%.2f €", ingresos.doubleValue()));
            }
            if (lblGastosTotal != null) {
                lblGastosTotal.setText(String.format("%.2f €", gastos.doubleValue()));
            }
            if (lblBeneficioNeto != null) {
                lblBeneficioNeto.setText(String.format("%.2f €", beneficio.doubleValue()));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar estadísticas de economía: " + e.getMessage());
            if (lblIngresosTotal != null) lblIngresosTotal.setText("Error");
            if (lblGastosTotal != null) lblGastosTotal.setText("Error");
            if (lblBeneficioNeto != null) lblBeneficioNeto.setText("Error");
        }
    }

    private void cargarEstadisticasFacturas() {
        // TODO: Implementar cálculo real desde la base de datos
        if (lblFacturasPendientes != null) lblFacturasPendientes.setText("Pendientes: 0");
        if (lblFacturasPagadas != null) lblFacturasPagadas.setText("Pagadas: 0");
        if (lblFacturasTotal != null) lblFacturasTotal.setText("Total mes: 0,00 €");
    }

    private void cargarEstadisticasPresupuestos() {
        // TODO: Implementar cálculo real desde la base de datos
        if (lblPresupuestosEnviados != null) lblPresupuestosEnviados.setText("Enviados: 0");
        if (lblPresupuestosAceptados != null) lblPresupuestosAceptados.setText("Aceptados: 0");
        if (lblPresupuestosPendientes != null) lblPresupuestosPendientes.setText("Pendientes: 0");
    }

    private void configurarVisibilidadSegunRol() {
        // Ocultar botón de usuarios si no es administrador
        if (btnUsuarios != null && !sessionManager.esAdministrador()) {
            btnUsuarios.setVisible(false);
            btnUsuarios.setManaged(false);
        }
    }

    // ==================== NAVEGACIÓN A MÓDULOS ====================

    @FXML
    private void abrirEconomiaGlobal(MouseEvent event) {
        cargarPantalla(FXML_BASE + "economia-global.fxml", "Economía Global");
    }

    @FXML
    private void abrirHinchables(MouseEvent event) {
        cargarPantalla(FXML_BASE + "hinchables.fxml", "Gestión de Hinchables");
    }

    @FXML
    private void abrirFacturas(MouseEvent event) {
        cargarPantalla(FXML_BASE + "facturas.fxml", "Facturas");
    }

    @FXML
    private void abrirPresupuestos(MouseEvent event) {
        cargarPantalla(FXML_BASE + "presupuestos.fxml", "Presupuestos");
    }

    @FXML
    private void abrirCalendario(ActionEvent event) {
        cargarPantalla(FXML_BASE + "calendario.fxml", "Calendario de Actividades");
    }

    @FXML
    private void abrirNinos(MouseEvent event) {
        cargarPantalla(FXML_BASE + "ninos.fxml", "Gestión de Niños");
    }

    @FXML
    private void abrirUsuarios(MouseEvent event) {
        if (!sessionManager.esAdministrador()) {
            AlertUtil.mostrarAdvertencia("Acceso Denegado",
                    "No tienes permisos para acceder a esta sección");
            return;
        }
        cargarPantalla(FXML_BASE + "usuarios.fxml", "Gestión de Usuarios");
    }

    @FXML
    private void registrarMovimientoEconomico(ActionEvent event) {
        // Abrir diálogo para registrar ingreso/gasto
        cargarDialogo(FXML_BASE + "dialogo-movimiento-economico.fxml", "Registrar Ingreso/Gasto");
    }

    // ==================== ACCESOS DIRECTOS EXTERNOS ====================

    @FXML
    private void abrirGmail(ActionEvent event) {
        abrirURL("https://mail.google.com");
    }

    @FXML
    private void abrirChatGPT(ActionEvent event) {
        abrirURL("https://chat.openai.com");
    }

    @FXML
    private void abrirDrive(ActionEvent event) {
        abrirURL("https://drive.google.com");
    }

    @FXML
    private void abrirSedeEuskadi(ActionEvent event) {
        abrirURL("https://www.euskadi.eus/inicio/");
    }

    @FXML
    private void abrirSedeBizkaia(ActionEvent event) {
        abrirURL("https://www.bizkaia.eus/");
    }

    @FXML
    private void abrirEFaktur(ActionEvent event) {
        abrirURL("https://efaktur.eu");
    }

    private void abrirURL(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                AlertUtil.mostrarError("Error", "No se puede abrir el navegador en este sistema");
            }
        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo abrir la URL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== NAVEGACIÓN Y GESTIÓN DE SESIÓN ====================

    @FXML
    private void cerrarSesion(ActionEvent event) {
        authService.logout();

        try {
            URL fxmlUrl = getClass().getResource(FXML_BASE + "login.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: No se encontró login.fxml");
                System.exit(0);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) lblBienvenida.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ainara Aisi Taldea - Inicio de sesión");
            stage.setMaximized(true);  // ✅ Maximiza la ventana
            stage.show();              // ✅ ¡ESTA LÍNEA FALTABA!

        } catch (Exception e) {
            AlertUtil.mostrarError("Error", "No se pudo cerrar sesión");
            e.printStackTrace();
        }
    }

    private void cargarPantalla(String fxmlPath, String titulo) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                System.err.println("ERROR: No se encontró " + fxmlPath);
                AlertUtil.mostrarAdvertencia("Módulo no disponible",
                        "La pantalla " + titulo + " aún no está implementada.\n\n" +
                                "Ruta esperada: " + fxmlPath);
                return;
            }

            System.out.println("Cargando pantalla: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) lblBienvenida.getScene().getWindow();
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle(titulo);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Error al cargar pantalla: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo cargar la pantalla: " + e.getMessage());
        }
    }

    private void cargarDialogo(String fxmlPath, String titulo) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                System.err.println("ERROR: No se encontró " + fxmlPath);
                AlertUtil.mostrarAdvertencia("Diálogo no disponible",
                        "El diálogo " + titulo + " aún no está implementado.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(titulo);
            dialogStage.setScene(new Scene(root));
            dialogStage.initOwner(lblBienvenida.getScene().getWindow());
            dialogStage.showAndWait();

            // Recargar estadísticas después de cerrar el diálogo
            cargarEstadisticasEconomia();

            // Recargar estadísticas después de cerrar el diálogo
            cargarEstadisticasEconomia();

        } catch (Exception e) {
            System.err.println("Error al cargar diálogo: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.mostrarError("Error", "No se pudo cargar el diálogo: " + e.getMessage());
        }
    }

}
package com.example.ainaraaisitaldea.util;

import com.example.ainaraaisitaldea.model.Nino;
import com.example.ainaraaisitaldea.model.Tutor;
import com.example.ainaraaisitaldea.service.NinoService;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exporta la lista de niños a un archivo CSV con TODOS los datos
     * incluyendo IDs para permitir reimportación sin duplicados
     */
    public static void exportarNinosCSV(List<Nino> ninos, String titulo) {
        if (ninos == null || ninos.isEmpty()) {
            System.err.println("No hay niños para exportar");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar CSV");
        fileChooser.setInitialFileName(titulo + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file == null) {
            System.out.println("Exportación cancelada por el usuario");
            return;
        }

        NinoService ninoService = new NinoService();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // Escribir BOM para UTF-8
            writer.write('\ufeff');

            // ===== ENCABEZADOS (22 columnas: IDs + datos) =====
            writer.write("ID_Nino;ID_Tutor;");
            writer.write("Nombre;Primer_Apellido;Segundo_Apellido;Fecha_Nacimiento;");
            writer.write("Colegio_Instituto;Curso;Alergias;Puede_Salir_Solo;Autorizacion_Fotos;");
            writer.write("Observaciones;Vinculo_Tutor;");
            writer.write("Nombre_Tutor;Primer_Apellido_Tutor;Segundo_Apellido_Tutor;DNI_Tutor;");
            writer.write("Telefono_Tutor;Correo_Tutor;Direccion_Tutor;Municipio_Tutor;Codigo_Postal_Tutor");
            writer.newLine();

            // ===== DATOS =====
            int registrosExportados = 0;

            for (Nino nino : ninos) {
                try {
                    // Obtener el tutor completo con todos sus datos
                    Tutor tutor = null;
                    if (nino.getIdTutor() != null) {
                        tutor = ninoService.obtenerTutorDeNino(nino.getIdTutor());
                    }

                    // IDs (campos 0-1)
                    writer.write(String.valueOf(nino.getIdNino()));
                    writer.write(";");
                    writer.write(nino.getIdTutor() != null ? String.valueOf(nino.getIdTutor()) : "");
                    writer.write(";");

                    // DATOS DEL NIÑO (campos 2-12)
                    writer.write(escaparCSV(nino.getNombre()));
                    writer.write(";");

                    writer.write(escaparCSV(nino.getPrimerApellido()));
                    writer.write(";");

                    writer.write(escaparCSV(nino.getSegundoApellido() != null ? nino.getSegundoApellido() : ""));
                    writer.write(";");

                    writer.write(nino.getFechaNacimiento().format(DATE_FORMATTER));
                    writer.write(";");

                    writer.write(escaparCSV(nino.getColegioInstituto()));
                    writer.write(";");

                    writer.write(escaparCSV(nino.getCurso()));
                    writer.write(";");

                    writer.write(nino.isTieneAlergias() ? "SÍ" : "NO");
                    writer.write(";");

                    writer.write(nino.isPuedeSalirSolo() ? "SÍ" : "NO");
                    writer.write(";");

                    writer.write(nino.isAutorizacionFotos() ? "SÍ" : "NO");
                    writer.write(";");

                    String obs = nino.getObservaciones();
                    writer.write(escaparCSV(obs != null && !obs.trim().isEmpty() ? obs : "-"));
                    writer.write(";");

                    writer.write(nino.getVinculoTutor().name());
                    writer.write(";");

                    // DATOS DEL TUTOR (campos 13-21)
                    if (tutor != null) {
                        writer.write(escaparCSV(tutor.getNombre()));
                        writer.write(";");

                        writer.write(escaparCSV(tutor.getPrimerApellido()));
                        writer.write(";");

                        writer.write(escaparCSV(tutor.getSegundoApellido() != null ? tutor.getSegundoApellido() : ""));
                        writer.write(";");

                        writer.write(escaparCSV(tutor.getDni()));
                        writer.write(";");

                        writer.write(escaparCSV(tutor.getTelefono()));
                        writer.write(";");

                        String correo = tutor.getCorreo();
                        writer.write(escaparCSV(correo != null && !correo.trim().isEmpty() ? correo : "-"));
                        writer.write(";");

                        writer.write(escaparCSV(tutor.getDireccion()));
                        writer.write(";");

                        writer.write(escaparCSV(tutor.getMunicipio()));
                        writer.write(";");

                        writer.write(escaparCSV(tutor.getCodigoPostal()));
                    } else {
                        // Si no hay tutor, escribir campos vacíos
                        writer.write(";;;;;;;;");
                    }

                    writer.newLine();
                    registrosExportados++;

                } catch (Exception e) {
                    System.err.println("Error al exportar niño " + nino.getNombreCompleto() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("✓ CSV exportado correctamente: " + file.getAbsolutePath());
            System.out.println("✓ Total de registros exportados: " + registrosExportados + " de " + ninos.size());

        } catch (IOException e) {
            System.err.println("Error al exportar CSV: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al exportar CSV", e);
        }
    }

    /**
     * Escapa caracteres especiales en CSV
     */
    private static String escaparCSV(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "";
        }

        // Si contiene punto y coma, comillas o saltos de línea, envolver en comillas
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            // Escapar comillas dobles duplicándolas
            String valorEscapado = valor.replace("\"", "\"\"");
            return "\"" + valorEscapado + "\"";
        }

        return valor;
    }
}
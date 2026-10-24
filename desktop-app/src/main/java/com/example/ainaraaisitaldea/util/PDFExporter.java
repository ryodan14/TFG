package com.example.ainaraaisitaldea.util;

import com.example.ainaraaisitaldea.model.Nino;
import com.example.ainaraaisitaldea.model.Tutor;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    private static final String PDF_DIRECTORY = System.getProperty("user.home")
            + File.separator + "Downloads" + File.separator;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // === 1. LISTADO DE NIÑOS ===
    public static String generarListadoNinosPDF(List<Nino> ninos) throws IOException {
        String fileName = "Listado_Ninos_" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + ".pdf";
        String filePath = PDF_DIRECTORY + fileName;

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        PdfFont bold = PdfFontFactory.createFont();
        PdfFont normal = PdfFontFactory.createFont();

        // Título
        doc.add(new Paragraph("LISTADO DE NIÑOS - AINARA AISI TALDEA")
                .setFont(bold).setFontSize(16));
        doc.add(new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFont(normal).setFontSize(10));
        doc.add(new Paragraph("\n"));

        // Tabla
        float[] colWidths = {1, 2, 2, 1, 1, 1, 1, 1}; // ajusta según columnas
        Table table = new Table(5);

        // Cabeceras
        String[] headers = {"Nº", "Nombre", "1º Ape.", "2º Ape.", "Nac.", "Edad", "Colegio", "Tutor"};
        for (String h : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h).setFont(bold))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
        }

        // Filas
        for (int i = 0; i < ninos.size(); i++) {
            Nino n = ninos.get(i);
            table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1)).setFont(normal)));
            table.addCell(new Cell().add(new Paragraph(n.getNombre()).setFont(normal)));
            table.addCell(new Cell().add(new Paragraph(n.getPrimerApellido()).setFont(normal)));
            table.addCell(new Cell().add(new Paragraph(n.getSegundoApellido() != null ? n.getSegundoApellido() : "-")).setFont(normal));
            table.addCell(new Cell().add(new Paragraph(n.getFechaNacimiento().format(DATE_FORMATTER))).setFont(normal));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(n.getEdad()))).setFont(normal));
            table.addCell(new Cell().add(new Paragraph(n.getColegioInstituto() != null ? n.getColegioInstituto() : "-")).setFont(normal));
            table.addCell(new Cell().add(new Paragraph(n.getNombreCompletoTutor() != null ? n.getNombreCompletoTutor() : "-")).setFont(normal));
        }

        doc.add(table);
        doc.add(new Paragraph("\nTotal: " + ninos.size() + " niños").setFont(bold));

        doc.close();
        return filePath;
    }

    // === 2. FICHA INDIVIDUAL DE NIÑO ===
    public static String generarFichaNinoPDF(Nino nino, Tutor tutor) throws IOException {
        String fileName = "Ficha_" + nino.getPrimerApellido() + "_" + nino.getNombre() + "_" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + ".pdf";
        String filePath = PDF_DIRECTORY + fileName;

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        PdfFont bold = PdfFontFactory.createFont();
        PdfFont normal = PdfFontFactory.createFont();

        // Título
        doc.add(new Paragraph("FICHA DEL NIÑO/A").setFont(bold).setFontSize(18));
        doc.add(new Paragraph("\n"));

        // Datos del niño
        doc.add(new Paragraph("DATOS DEL NIÑO/A").setFont(bold).setFontSize(12));
        agregarLinea(doc, "Nombre completo:", nino.getNombreCompleto(), normal);
        agregarLinea(doc, "Fecha nacimiento:", nino.getFechaNacimiento().format(DATE_FORMATTER), normal);
        agregarLinea(doc, "Edad:", nino.getEdad() + " años", normal);
        agregarLinea(doc, "Colegio/Instituto:", nino.getColegioInstituto(), normal);
        agregarLinea(doc, "Curso:", nino.getCurso(), normal);



        agregarLinea(doc, "Puede salir solo:", nino.getPuedeSalirSoloTexto(), normal);
        agregarLinea(doc, "Autorización fotos:", nino.getAutorizacionFotosTexto(), normal);
        if (nino.getObservaciones() != null && !nino.getObservaciones().trim().isEmpty()) {
            agregarLinea(doc, "Observaciones:", nino.getObservaciones(), normal);
        }

        doc.add(new Paragraph("\n"));

        // Datos del tutor
        doc.add(new Paragraph("DATOS DEL TUTOR/A").setFont(bold).setFontSize(12));
        if (tutor != null) {
            agregarLinea(doc, "Nombre completo:", tutor.getNombreCompleto(), normal);
            agregarLinea(doc, "DNI:", tutor.getDni(), normal);
            agregarLinea(doc, "Teléfono:", tutor.getTelefono(), normal);
            agregarLinea(doc, "Correo:", tutor.getCorreo(), normal);
            agregarLinea(doc, "Dirección:", tutor.getDireccion() + ", " + tutor.getMunicipio() + " (CP: " + tutor.getCodigoPostal() + ")", normal);
        }

        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Documento generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")))
                .setFont(normal).setFontSize(9));

        doc.close();
        return filePath;
    }

    // === Auxiliar ===
    private static void agregarLinea(Document doc, String etiqueta, String valor, PdfFont font) {
        if (valor == null) valor = "-";
        doc.add(new Paragraph(new Text(etiqueta).setFont(font).setBold())
                .add(new Text(" " + valor).setFont(font)));
    }
}
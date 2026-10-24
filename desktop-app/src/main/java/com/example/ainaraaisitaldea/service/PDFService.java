package com.example.ainaraaisitaldea.service;

import com.example.ainaraaisitaldea.dao.DetalleDiaHinchableDAO;
import com.example.ainaraaisitaldea.dao.ActividadDAO;
import com.example.ainaraaisitaldea.model.Actividad;
import com.example.ainaraaisitaldea.model.DetalleDiaHinchable;
import com.example.ainaraaisitaldea.model.Factura;
import com.example.ainaraaisitaldea.model.Nino;
import com.example.ainaraaisitaldea.model.enums.TipoActividad;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.LineSeparator;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generación de documentos PDF
 * ACTUALIZADO con reporte anual completo de hinchables
 */
public class PDFService {

    private static final String PDF_DIRECTORY = System.getProperty("user.home") + "/GestionAsociacion/PDFs/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final DetalleDiaHinchableDAO detalleDAO;
    private final ActividadDAO actividadDAO;

    public PDFService() {
        // Crear directorio si no existe
        File directory = new File(PDF_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        this.detalleDAO = new DetalleDiaHinchableDAO();
        this.actividadDAO = new ActividadDAO();
    }

    // ... [Métodos anteriores: generarListaAsistenciaPDF, generarFacturaPDF, generarResumenEconomicoPDF] ...

    /**
     * NUEVO: Genera PDF anual completo de hinchables con desglose detallado
     */
    public String generarReporteAnualHinchables(int anio) throws Exception {
        String fileName = "Reporte_Anual_Hinchables_" + anio + ".pdf";
        String filePath = PDF_DIRECTORY + fileName;

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Fuentes
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Colores personalizados
        DeviceRgb azul = new DeviceRgb(33, 150, 243);
        DeviceRgb verde = new DeviceRgb(76, 175, 80);
        DeviceRgb rojo = new DeviceRgb(244, 67, 54);
        DeviceRgb gris = new DeviceRgb(189, 189, 189);

        // ==================== PORTADA ====================
        document.add(new Paragraph("\n\n\n\n"));

        Paragraph titulo = new Paragraph("REPORTE ANUAL")
                .setFont(boldFont)
                .setFontSize(28)
                .setFontColor(azul)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("ECONOMÍA DE HINCHABLES")
                .setFont(boldFont)
                .setFontSize(22)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(subtitulo);

        document.add(new Paragraph("\n"));

        Paragraph anioDoc = new Paragraph(String.valueOf(anio))
                .setFont(boldFont)
                .setFontSize(48)
                .setFontColor(azul)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(anioDoc);

        document.add(new Paragraph("\n\n"));

        Paragraph fechaGen = new Paragraph("Generado el: " + LocalDate.now().format(DATE_FORMATTER))
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(gris);
        document.add(fechaGen);

        // Nueva página para el resumen
        document.add(new AreaBreak());

        // ==================== RESUMEN EJECUTIVO ====================
        Paragraph headerResumen = new Paragraph("📊 RESUMEN EJECUTIVO ANUAL")
                .setFont(boldFont)
                .setFontSize(18)
                .setFontColor(azul);
        document.add(headerResumen);

        document.add(new LineSeparator(new SolidLine()));
        document.add(new Paragraph("\n"));

        // Obtener todas las actividades del año
        List<Actividad> actividadesAnuales = obtenerActividadesAnuales(anio);

        // Calcular totales anuales
        int totalSalidas = actividadesAnuales.size();
        double totalIngresos = actividadesAnuales.stream()
                .mapToDouble(a -> a.getDineroRecibido() != null ? a.getDineroRecibido() : 0.0)
                .sum();
        double totalGastos = actividadesAnuales.stream()
                .mapToDouble(a -> a.getDineroPagadoMonitores() != null ? a.getDineroPagadoMonitores() : 0.0)
                .sum();
        double beneficioNeto = totalIngresos - totalGastos;

        // Contar total de días
        int totalDias = 0;
        for (Actividad act : actividadesAnuales) {
            List<DetalleDiaHinchable> detalles = detalleDAO.obtenerPorActividad(act.getIdActividad());
            totalDias += detalles.isEmpty() ? 1 : detalles.size();
        }

        // Tabla de resumen anual
        Table resumenTable = new Table(new float[]{2, 1});
        resumenTable.setWidth(UnitValue.createPercentValue(60));

        agregarFilaResumen(resumenTable, "Total de Salidas:", String.valueOf(totalSalidas), boldFont, regularFont, null);
        agregarFilaResumen(resumenTable, "Total de Días:", String.valueOf(totalDias), boldFont, regularFont, null);
        agregarFilaResumen(resumenTable, "Total Ingresos:", String.format("%.2f €", totalIngresos), boldFont, regularFont, verde);
        agregarFilaResumen(resumenTable, "Total Gastos:", String.format("%.2f €", totalGastos), boldFont, regularFont, rojo);

        Cell labelBeneficio = new Cell().add(new Paragraph("Beneficio Neto:").setFont(boldFont).setFontSize(12))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(null).setPadding(8);
        Cell valorBeneficio = new Cell().add(new Paragraph(String.format("%.2f €", beneficioNeto))
                        .setFont(boldFont).setFontSize(14).setFontColor(beneficioNeto >= 0 ? verde : rojo))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(null).setPadding(8);
        resumenTable.addCell(labelBeneficio);
        resumenTable.addCell(valorBeneficio);

        document.add(resumenTable);

        // ==================== RESUMEN POR MESES ====================
        document.add(new Paragraph("\n\n"));

        Paragraph headerMeses = new Paragraph("📅 DESGLOSE POR MESES")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(azul);
        document.add(headerMeses);

        document.add(new LineSeparator(new SolidLine()));
        document.add(new Paragraph("\n"));

        // Tabla de resumen mensual
        Table mesesTable = new Table(new float[]{1.5f, 1, 1.2f, 1.2f, 1.2f});
        mesesTable.setWidth(UnitValue.createPercentValue(100));

        // Encabezados
        String[] headersMeses = {"Mes", "Salidas", "Ingresos", "Gastos", "Beneficio"};
        for (String header : headersMeses) {
            Cell cell = new Cell().add(new Paragraph(header).setFont(boldFont).setFontSize(10))
                    .setBackgroundColor(azul)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5);
            mesesTable.addHeaderCell(cell);
        }

        // Datos por mes
        String[] nombresMeses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        for (int mes = 1; mes <= 12; mes++) {
            final int mesFinal = mes;
            List<Actividad> actsMes = actividadesAnuales.stream()
                    .filter(a -> a.getFecha().getMonthValue() == mesFinal)
                    .collect(Collectors.toList());

            if (actsMes.isEmpty()) continue; // Saltar meses sin actividad

            double ingresosMes = actsMes.stream()
                    .mapToDouble(a -> a.getDineroRecibido() != null ? a.getDineroRecibido() : 0.0)
                    .sum();
            double gastosMes = actsMes.stream()
                    .mapToDouble(a -> a.getDineroPagadoMonitores() != null ? a.getDineroPagadoMonitores() : 0.0)
                    .sum();
            double beneficioMes = ingresosMes - gastosMes;

            mesesTable.addCell(new Cell().add(new Paragraph(nombresMeses[mes - 1]).setFont(regularFont).setFontSize(9)));
            mesesTable.addCell(new Cell().add(new Paragraph(String.valueOf(actsMes.size())).setFont(regularFont).setFontSize(9)).setTextAlignment(TextAlignment.CENTER));
            mesesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f €", ingresosMes)).setFont(regularFont).setFontSize(9).setFontColor(verde)).setTextAlignment(TextAlignment.RIGHT));
            mesesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f €", gastosMes)).setFont(regularFont).setFontSize(9).setFontColor(rojo)).setTextAlignment(TextAlignment.RIGHT));
            mesesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f €", beneficioMes)).setFont(regularFont).setFontSize(9).setFontColor(beneficioMes >= 0 ? verde : rojo).setBold()).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(mesesTable);

        // Nueva página para el detalle de salidas
        document.add(new AreaBreak());

        // ==================== DETALLE DE SALIDAS ====================
        Paragraph headerDetalle = new Paragraph("📋 DETALLE COMPLETO DE SALIDAS")
                .setFont(boldFont)
                .setFontSize(18)
                .setFontColor(azul);
        document.add(headerDetalle);

        document.add(new LineSeparator(new SolidLine()));
        document.add(new Paragraph("\n"));

        // Ordenar por fecha
        actividadesAnuales.sort(Comparator.comparing(Actividad::getFecha));

        int numeroSalida = 1;
        for (Actividad actividad : actividadesAnuales) {
            // Encabezado de salida
            Paragraph numSalida = new Paragraph("SALIDA #" + numeroSalida)
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setFontColor(azul)
                    .setBackgroundColor(new DeviceRgb(227, 242, 253))
                    .setPadding(8);
            document.add(numSalida);

            // Nombre y descripción
            Paragraph nombre = new Paragraph(actividad.getNombre())
                    .setFont(boldFont)
                    .setFontSize(12);
            document.add(nombre);


            document.add(new Paragraph("\n"));

            // Obtener detalles de días
            List<DetalleDiaHinchable> detalles = detalleDAO.obtenerPorActividad(actividad.getIdActividad());

            if (!detalles.isEmpty()) {
                // Tabla de días
                Paragraph labelDias = new Paragraph("Días de la salida:")
                        .setFont(boldFont)
                        .setFontSize(11);
                document.add(labelDias);

                Table diasTable = new Table(new float[]{2, 2, 1.5f, 3});
                diasTable.setWidth(UnitValue.createPercentValue(90));

                // Encabezados tabla días
                String[] headersDias = {"Fecha", "Horario", "Importe", "Observaciones"};
                for (String header : headersDias) {
                    diasTable.addHeaderCell(new Cell().add(new Paragraph(header).setFont(boldFont).setFontSize(9))
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(4));
                }

                // Datos de cada día
                for (DetalleDiaHinchable detalle : detalles) {
                    diasTable.addCell(new Cell().add(new Paragraph(detalle.getFecha().format(DATE_FORMATTER)).setFont(regularFont).setFontSize(9)));

                    String horario = detalle.tieneHorario() ? detalle.getHorarioFormateado() : "Sin horario";
                    diasTable.addCell(new Cell().add(new Paragraph(horario).setFont(regularFont).setFontSize(9)));

                    String importe = detalle.tieneImporte() ? String.format("%.2f €", detalle.getImporte()) : "-";
                    diasTable.addCell(new Cell().add(new Paragraph(importe).setFont(regularFont).setFontSize(9)).setTextAlignment(TextAlignment.RIGHT));

                    String obs = (detalle.getObservaciones() != null && !detalle.getObservaciones().isEmpty())
                            ? detalle.getObservaciones() : "-";
                    diasTable.addCell(new Cell().add(new Paragraph(obs).setFont(regularFont).setFontSize(8)));
                }

                document.add(diasTable);
                document.add(new Paragraph("\n"));
            }

            // Resumen económico de la salida
            Table resumenSalidaTable = new Table(new float[]{2, 1});
            resumenSalidaTable.setWidth(UnitValue.createPercentValue(50));

            double ingresosSalida = actividad.getDineroRecibido() != null ? actividad.getDineroRecibido() : 0.0;
            double gastosSalida = actividad.getDineroPagadoMonitores() != null ? actividad.getDineroPagadoMonitores() : 0.0;
            double beneficioSalida = ingresosSalida - gastosSalida;

            agregarFilaResumen(resumenSalidaTable, "Total Ingresos:", String.format("%.2f €", ingresosSalida), regularFont, regularFont, verde);
            agregarFilaResumen(resumenSalidaTable, "Gastos Monitores:", String.format("%.2f €", gastosSalida), regularFont, regularFont, rojo);
            agregarFilaResumen(resumenSalidaTable, "Beneficio:", String.format("%.2f €", beneficioSalida), boldFont, boldFont, beneficioSalida >= 0 ? verde : rojo);

            document.add(resumenSalidaTable);

            // Observaciones generales
            if (actividad.getObservaciones() != null && !actividad.getObservaciones().isEmpty()) {
                document.add(new Paragraph("\n"));
                Paragraph obsLabel = new Paragraph("Observaciones generales:")
                        .setFont(boldFont)
                        .setFontSize(9);
                document.add(obsLabel);

                Paragraph obs = new Paragraph(actividad.getObservaciones())
                        .setFont(regularFont)
                        .setFontSize(9)
                        .setItalic()
                        .setFontColor(gris);
                document.add(obs);
            }

            // Estado
            Paragraph estado = new Paragraph("Estado: " + actividad.getEstado().name())
                    .setFont(regularFont)
                    .setFontSize(8)
                    .setFontColor(gris);
            document.add(estado);

            // Separador entre salidas
            document.add(new Paragraph("\n"));
            LineSeparator separator = new LineSeparator(new SolidLine());
            separator.setStrokeColor(gris);
            document.add(separator);
            document.add(new Paragraph("\n"));

            numeroSalida++;

            // Nueva página cada 3 salidas para mejor legibilidad
            if (numeroSalida % 3 == 0 && numeroSalida < actividadesAnuales.size()) {
                document.add(new AreaBreak());
            }
        }

        // Pie de página final
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Fin del reporte - Generado automáticamente por Sistema de Gestión de Asociación")
                .setFont(regularFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(gris)
                .setItalic();
        document.add(footer);

        document.close();

        return filePath;
    }

    /**
     * Método auxiliar para agregar fila a tabla de resumen
     */
    private void agregarFilaResumen(Table table, String label, String valor,
                                    PdfFont labelFont, PdfFont valorFont, DeviceRgb color) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFont(labelFont).setFontSize(11))
                .setBorder(null).setPadding(5);
        Cell valorCell = new Cell().add(new Paragraph(valor).setFont(valorFont).setFontSize(11))
                .setTextAlignment(TextAlignment.RIGHT).setBorder(null).setPadding(5);

        if (color != null) {
            valorCell.setFontColor(color);
        }

        table.addCell(labelCell);
        table.addCell(valorCell);
    }

    /**
     * Obtener todas las actividades de hinchables de un año
     */
    private List<Actividad> obtenerActividadesAnuales(int anio) throws Exception {
        List<Actividad> actividadesAnuales = new ArrayList<>();

        for (int mes = 1; mes <= 12; mes++) {
            List<Actividad> actsMes = actividadDAO.listarPorMes(anio, mes).stream()
                    .filter(a -> a.getTipo() == TipoActividad.HINCHABLES)
                    .collect(Collectors.toList());
            actividadesAnuales.addAll(actsMes);
        }

        return actividadesAnuales;
    }

    /**
     * Genera PDF de lista de asistencia para niños
     */
    public String generarListaAsistenciaPDF(List<Nino> ninos) throws IOException {
        String fileName = "Lista_Asistencia_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        String filePath = PDF_DIRECTORY + fileName;

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Fuentes
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Encabezado
        Paragraph header = new Paragraph("LISTA DE ASISTENCIA")
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
        document.add(header);

        Paragraph fecha = new Paragraph("Fecha: " + LocalDate.now().format(DATE_FORMATTER))
                .setFont(regularFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(fecha);

        document.add(new Paragraph("\n"));

        // Tabla
        float[] columnWidths = {1, 3, 2, 2, 1};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Encabezados de tabla
        String[] headers = {"Nº", "Nombre y Apellidos", "Curso", "Centro", "Asistió"};
        for (String headerText : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(headerText).setFont(boldFont))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            table.addHeaderCell(headerCell);
        }

        // Datos
        int contador = 1;
        for (Nino nino : ninos) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(contador++)).setFont(regularFont)));
            table.addCell(new Cell().add(new Paragraph(nino.getNombreCompleto()).setFont(regularFont)));
            table.addCell(new Cell().add(new Paragraph(nino.getCurso() != null ? nino.getCurso() : "").setFont(regularFont)));
           table.addCell(new Cell().add(new Paragraph("☐").setFont(regularFont).setFontSize(16)).setTextAlignment(TextAlignment.CENTER));
        }

        document.add(table);

        // Pie de página
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Total de niños: " + ninos.size())
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(footer);

        document.close();

        return filePath;
    }

    /**
     * Genera PDF de factura/presupuesto
     */
    public String generarFacturaPDF(Factura factura) throws IOException {
        String fileName = factura.getNumeroFactura().replace("/", "-") + ".pdf";
        String filePath = PDF_DIRECTORY + fileName;

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Fuentes
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Encabezado - Tipo de documento
        String tipoDoc = factura.getTipo().getDescripcion().toUpperCase();
        Paragraph header = new Paragraph(tipoDoc)
                .setFont(boldFont)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
        document.add(header);

        // Número de factura
        Paragraph numero = new Paragraph(factura.getNumeroFactura())
                .setFont(boldFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(numero);

        document.add(new Paragraph("\n"));

        // Información de la asociación (izquierda)
        Paragraph infoAsociacion = new Paragraph()
                .setFont(regularFont)
                .setFontSize(10)
                .add("ASOCIACIÓN LOCAL\n")
                .add("Calle Principal, 123\n")
                .add("NIF: G12345678\n")
                .add("Tel: 943-123-456\n");
        document.add(infoAsociacion);

        document.add(new Paragraph("\n"));

        // Información del cliente
        Paragraph infoCliente = new Paragraph()
                .setFont(boldFont)
                .setFontSize(11)
                .add("CLIENTE:\n");
        document.add(infoCliente);

        Paragraph datosCliente = new Paragraph()
                .setFont(regularFont)
                .setFontSize(10)
                .add(factura.getCliente() + "\n");

        if (factura.getNifCliente() != null && !factura.getNifCliente().isEmpty()) {
            datosCliente.add("NIF: " + factura.getNifCliente() + "\n");
        }
        if (factura.getDireccionCliente() != null && !factura.getDireccionCliente().isEmpty()) {
            datosCliente.add(factura.getDireccionCliente() + "\n");
        }
        document.add(datosCliente);

        document.add(new Paragraph("\n"));

        // Fechas
        Table fechasTable = new Table(2);
        fechasTable.setWidth(UnitValue.createPercentValue(50));

        fechasTable.addCell(new Cell().add(new Paragraph("Fecha emisión:").setFont(boldFont).setFontSize(10)).setBorder(null));
        fechasTable.addCell(new Cell().add(new Paragraph(factura.getFechaEmision().format(DATE_FORMATTER)).setFont(regularFont).setFontSize(10)).setBorder(null));

        if (factura.getFechaVencimiento() != null) {
            fechasTable.addCell(new Cell().add(new Paragraph("Fecha vencimiento:").setFont(boldFont).setFontSize(10)).setBorder(null));
            fechasTable.addCell(new Cell().add(new Paragraph(factura.getFechaVencimiento().format(DATE_FORMATTER)).setFont(regularFont).setFontSize(10)).setBorder(null));
        }

        document.add(fechasTable);
        document.add(new Paragraph("\n"));

        // Concepto
        Paragraph conceptoHeader = new Paragraph("CONCEPTO")
                .setFont(boldFont)
                .setFontSize(11);
        document.add(conceptoHeader);

        Paragraph concepto = new Paragraph(factura.getConcepto())
                .setFont(regularFont)
                .setFontSize(10);
        document.add(concepto);

        document.add(new Paragraph("\n"));

        // Tabla de importes
        float[] columnWidths = {3, 1};
        Table importesTable = new Table(UnitValue.createPercentArray(columnWidths));
        importesTable.setWidth(UnitValue.createPercentValue(60));
        importesTable.setMarginLeft(200);

        // Base imponible
        importesTable.addCell(new Cell().add(new Paragraph("Base Imponible:").setFont(regularFont).setFontSize(10)));
        importesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f €", factura.getBaseImponible())).setFont(regularFont).setFontSize(10)).setTextAlignment(TextAlignment.RIGHT));

        // IVA
        importesTable.addCell(new Cell().add(new Paragraph("IVA (" + factura.getIvaPorcentaje() + "%):").setFont(regularFont).setFontSize(10)));
        importesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f €", factura.getIvaCantidad())).setFont(regularFont).setFontSize(10)).setTextAlignment(TextAlignment.RIGHT));

        // Total
        importesTable.addCell(new Cell().add(new Paragraph("TOTAL:").setFont(boldFont).setFontSize(11)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        importesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f €", factura.getTotal())).setFont(boldFont).setFontSize(11)).setTextAlignment(TextAlignment.RIGHT).setBackgroundColor(ColorConstants.LIGHT_GRAY));

        document.add(importesTable);

        document.add(new Paragraph("\n\n"));

        // Estado
        String estadoTexto = "Estado: " + factura.getEstado().getDescripcion();
        Paragraph estado = new Paragraph(estadoTexto)
                .setFont(boldFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);

        if (factura.getEstado().name().equals("PAGADA")) {
            estado.setFontColor(ColorConstants.GREEN);
        } else if (factura.getEstado().name().equals("PENDIENTE")) {
            estado.setFontColor(ColorConstants.ORANGE);
        }

        document.add(estado);

        // Observaciones
        if (factura.getObservaciones() != null && !factura.getObservaciones().isEmpty()) {
            document.add(new Paragraph("\n"));
            Paragraph obsHeader = new Paragraph("Observaciones:")
                    .setFont(boldFont)
                    .setFontSize(9);
            document.add(obsHeader);

            Paragraph obs = new Paragraph(factura.getObservaciones())
                    .setFont(regularFont)
                    .setFontSize(8);
            document.add(obs);
        }

        // Pie de página
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Gracias por su confianza")
                .setFont(regularFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        document.add(footer);

        document.close();

        return filePath;
    }

    /**
     * Genera PDF de resumen económico de hinchables
     */
    public String generarResumenEconomicoPDF(int anio, int mes,
                                             int totalActividades,
                                             double totalIngresos,
                                             double totalGastos) throws IOException {

        String fileName = "Resumen_Economico_" + anio + "_" + String.format("%02d", mes) + ".pdf";
        String filePath = PDF_DIRECTORY + fileName;

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Encabezado
        Paragraph header = new Paragraph("RESUMEN ECONÓMICO - HINCHABLES")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(header);

        Paragraph periodo = new Paragraph(mes + "/" + anio)
                .setFont(regularFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(periodo);

        document.add(new Paragraph("\n\n"));

        // Tabla de resumen
        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(60));
        table.setMarginLeft(100);

        table.addCell(new Cell().add(new Paragraph("Total de actividades:").setFont(boldFont)));
        table.addCell(new Cell().add(new Paragraph(String.valueOf(totalActividades)).setFont(regularFont)).setTextAlignment(TextAlignment.RIGHT));

        table.addCell(new Cell().add(new Paragraph("Total ingresos:").setFont(boldFont)));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f €", totalIngresos)).setFont(regularFont).setFontColor(ColorConstants.GREEN)).setTextAlignment(TextAlignment.RIGHT));

        table.addCell(new Cell().add(new Paragraph("Total gastos:").setFont(boldFont)));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f €", totalGastos)).setFont(regularFont).setFontColor(ColorConstants.RED)).setTextAlignment(TextAlignment.RIGHT));

        double beneficio = totalIngresos - totalGastos;
        table.addCell(new Cell().add(new Paragraph("Beneficio neto:").setFont(boldFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f €", beneficio)).setFont(boldFont).setFontSize(12)).setTextAlignment(TextAlignment.RIGHT).setBackgroundColor(ColorConstants.LIGHT_GRAY));

        document.add(table);

        document.close();

        return filePath;
    }
}
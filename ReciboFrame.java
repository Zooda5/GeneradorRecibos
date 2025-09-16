package com.mycompany.generadorrecibos;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.TextAlignment;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.awt.Desktop;

public class ReciboFrame extends JFrame {

    private Apartamento apartamento;
    private Lectura lectura;

    public ReciboFrame(Apartamento apto, Lectura l) {
        this.apartamento = apto;
        this.lectura = l;

        setTitle("Recibo de Agua - Apto " + apto.getNumero());
        setSize(950, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Encabezado
        JPanel headerPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        headerPanel.add(new JLabel("Apartamento: " + apto.getNumero()));
        headerPanel.add(new JLabel("Propietario: " + apto.getPropietario()));
        headerPanel.add(new JLabel("Medidor: " + apto.getMedidor()));
        headerPanel.add(new JLabel("Fecha Generación: " + l.getFechaActual()));
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Tabla principal
        String[] columnas = {"Lectura Anterior", "Fecha Anterior", "Lectura Actual", "Fecha Actual",
                "Consumo (m³)", "Valor Acueducto", "Valor Alcantarillado", "Total"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modelo);
        modelo.addRow(new Object[]{
                lectura.getLecturaInicial(),
                lectura.getFechaInicial(),
                lectura.getLecturaActual(),
                lectura.getFechaActual(),
                lectura.getConsumo(),
                lectura.getValorAcueducto(),
                lectura.getValorAlcantarillado(),
                lectura.getValorPagar()
        });
        tabla.setRowHeight(26);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tablaScroll = new JScrollPane(tabla);
        tablaScroll.setPreferredSize(new Dimension(900, tabla.getRowHeight() + 30));
        tablaScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, tabla.getRowHeight() + 30));
        mainPanel.add(tablaScroll);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Tabla detalle precios
        String[] columnasDetalle = {"Concepto", "Precio Unitario (m³)", "Consumo (m³)", "Subtotal"};
        DefaultTableModel modeloDetalle = new DefaultTableModel(columnasDetalle, 0);
        JTable tablaDetalle = new JTable(modeloDetalle);
        modeloDetalle.addRow(new Object[]{"Acueducto", lectura.getValorAcueducto() / lectura.getConsumo(),
                lectura.getConsumo(), lectura.getValorAcueducto()});
        modeloDetalle.addRow(new Object[]{"Alcantarillado", lectura.getValorAlcantarillado() / lectura.getConsumo(),
                lectura.getConsumo(), lectura.getValorAlcantarillado()});
        modeloDetalle.addRow(new Object[]{"Total", "-", "-", lectura.getValorPagar()});
        tablaDetalle.setRowHeight(26);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablaDetalle.getColumnCount(); i++) {
            tablaDetalle.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        mainPanel.add(new JLabel("Detalle de Precios:"));
        JScrollPane detalleScroll = new JScrollPane(tablaDetalle);
        detalleScroll.setPreferredSize(new Dimension(900, tablaDetalle.getRowHeight() * tablaDetalle.getRowCount() + 10));
        detalleScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, tablaDetalle.getRowHeight() * tablaDetalle.getRowCount() + 10));
        mainPanel.add(detalleScroll);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Gráfica
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<Integer, Double> consumoMensual = new TreeMap<>();
        for (Lectura lx : apto.getHistorialLecturas()) {
            consumoMensual.put(lx.getFechaActual().getMonthValue(), lx.getConsumo());
        }
        for (int mes = 1; mes <= 12; mes++) {
            double valor = consumoMensual.getOrDefault(mes, 0.0);
            dataset.addValue(valor, "Consumo", getNombreMesEsp(mes));
        }
        JFreeChart chart = ChartFactory.createBarChart("Consumo Mensual", "Mes", "m³", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 102, 204));
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 300));
        mainPanel.add(chartPanel);

        // Botones exportar e imprimir PDF
        JButton btnPDF = new JButton("Exportar a PDF");
        btnPDF.addActionListener(e -> exportarPDF(chart, false));

        JButton btnImprimir = new JButton("Imprimir PDF");
        btnImprimir.addActionListener(e -> exportarPDF(chart, true));

        JPanel panelBoton = new JPanel();
        panelBoton.add(btnPDF);
        panelBoton.add(btnImprimir);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(panelBoton);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private String getNombreMesEsp(int mes) {
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                          "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return meses[mes - 1];
    }

    private void exportarPDF(JFreeChart chart, boolean imprimir) {
        try {
            // Carpeta del apartamento
            String baseDir = System.getProperty("user.dir");
            File aptoDir = new File(baseDir, "Apartamento_" + apartamento.getNumero());
            if (!aptoDir.exists()) {
                aptoDir.mkdirs();
            }

            // Nombre con fecha de la lectura
            String nombreArchivo = "Recibo_" + apartamento.getNumero() + "_" + lectura.getFechaActual() + ".pdf";
            File archivoPDF = new File(aptoDir, nombreArchivo);

            PdfWriter writer = new PdfWriter(archivoPDF);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            Paragraph titulo = new Paragraph("Recibo de Agua")
                    .setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER);
            doc.add(titulo);

            Paragraph info = new Paragraph(
                    "Apartamento: " + apartamento.getNumero() + "   |   " +
                    "Propietario: " + apartamento.getPropietario() + "   |   " +
                    "Medidor: " + apartamento.getMedidor() + "   |   " +
                    "Fecha Generación: " + lectura.getFechaActual()
            ).setTextAlignment(TextAlignment.CENTER);
            doc.add(info);
            doc.add(new Paragraph("\n"));

            float[] columnWidths = {100, 100, 100, 100, 120, 80, 80, 80};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"Lectura Anterior", "Fecha Anterior", "Lectura Actual", "Fecha Actual",
                    "Consumo (m³)", "Valor Acueducto", "Valor Alcantarillado", "Total"};
            for (String h : headers) table.addCell(new Cell().add(new Paragraph(h).setBold()));

            table.addCell(String.valueOf(lectura.getLecturaInicial()));
            table.addCell(String.valueOf(lectura.getFechaInicial()));
            table.addCell(String.valueOf(lectura.getLecturaActual()));
            table.addCell(String.valueOf(lectura.getFechaActual()));
            table.addCell(String.valueOf(lectura.getConsumo()));
            table.addCell(String.valueOf(lectura.getValorAcueducto()));
            table.addCell(String.valueOf(lectura.getValorAlcantarillado()));
            table.addCell(String.valueOf(lectura.getValorPagar()));
            doc.add(table);
            doc.add(new Paragraph("\n"));

            float[] columnWidthsDetalle = {120, 120, 80, 80};
            Table tableDetalle = new Table(UnitValue.createPercentArray(columnWidthsDetalle));
            tableDetalle.setWidth(UnitValue.createPercentValue(100));

            String[] headersDetalle = {"Concepto", "Precio Unitario (m³)", "Consumo (m³)", "Subtotal"};
            for (String h : headersDetalle) tableDetalle.addCell(new Cell().add(new Paragraph(h).setBold()));

            tableDetalle.addCell("Acueducto");
            tableDetalle.addCell(String.valueOf(lectura.getValorAcueducto() / lectura.getConsumo()));
            tableDetalle.addCell(String.valueOf(lectura.getConsumo()));
            tableDetalle.addCell(String.valueOf(lectura.getValorAcueducto()));

            tableDetalle.addCell("Alcantarillado");
            tableDetalle.addCell(String.valueOf(lectura.getValorAlcantarillado() / lectura.getConsumo()));
            tableDetalle.addCell(String.valueOf(lectura.getConsumo()));
            tableDetalle.addCell(String.valueOf(lectura.getValorAlcantarillado()));

            tableDetalle.addCell("Total");
            tableDetalle.addCell("-");
            tableDetalle.addCell("-");
            tableDetalle.addCell(String.valueOf(lectura.getValorPagar()));

            doc.add(new Paragraph("Detalle de precios").setBold());
            doc.add(tableDetalle);
            doc.add(new Paragraph("\n"));

            BufferedImage bufferedImage = chart.createBufferedImage(500, 250);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            Image chartImage = new Image(ImageDataFactory.create(baos.toByteArray()));
            chartImage.setAutoScale(true);
            doc.add(new Paragraph("Consumo Mensual").setBold());
            doc.add(chartImage);
            doc.add(new Paragraph("\n"));

            LocalDate fechaLimite = lectura.getFechaActual()
                    .with(TemporalAdjusters.firstDayOfNextMonth()).minusDays(1);
            doc.add(new Paragraph("Fecha Límite de Pago: " + fechaLimite));

            doc.close();

            if (imprimir) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.print(archivoPDF);
                } else {
                    JOptionPane.showMessageDialog(this, "Impresión no soportada en este sistema.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "PDF generado en: " + archivoPDF.getAbsolutePath());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar/imprimir PDF: " + ex.getMessage());
        }
    }
}

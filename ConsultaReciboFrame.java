package com.mycompany.generadorrecibos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

public class ConsultaReciboFrame extends JFrame {

    private DefaultTableModel modelo;
    private JTable tabla;
    private Apartamento apartamento;

    public ConsultaReciboFrame(Apartamento apto) {
        this.apartamento = apto;

        setTitle("Consulta Recibo - Apto " + apto.getNumero());
        setSize(1000, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(
            new String[]{
                "Lectura Anterior",
                "Fecha Anterior",
                "Lectura Actual",
                "Fecha Actual",
                "Consumo",
                "Valor Acueducto",
                "Valor Alcantarillado",
                "Total",
                "Estado"
            }, 0
        );

        tabla = new JTable(modelo);
        cargarTabla();
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnMostrar = new JButton("Mostrar Recibo Seleccionado");
        btnMostrar.addActionListener(e -> mostrarReciboPDF());

        JButton btnActualizarEstado = new JButton("Actualizar Estado de Pago");
        btnActualizarEstado.addActionListener(e -> actualizarEstado());

        JPanel panelBoton = new JPanel();
        panelBoton.add(btnMostrar);
        panelBoton.add(btnActualizarEstado);
        add(panelBoton, BorderLayout.SOUTH);
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Lectura l : apartamento.getHistorialLecturas()) {
            modelo.addRow(new Object[]{
                l.getLecturaInicial(),
                l.getFechaInicial(),
                l.getLecturaActual(),
                l.getFechaActual(),
                l.getConsumo(),
                l.getValorAcueducto(),
                l.getValorAlcantarillado(),
                l.getValorPagar(),
                l.getEstado()
            });
        }
    }

    private void mostrarReciboPDF() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una lectura.");
            return;
        }

        Lectura l = apartamento.getHistorialLecturas().get(fila);

        // Guardar PDF en carpeta del apartamento
        try {
            String baseDir = System.getProperty("user.dir");
            File aptoDir = new File(baseDir, "Apartamento_" + apartamento.getNumero());
            if (!aptoDir.exists()) {
                aptoDir.mkdirs();
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM_yyyy", new Locale("es", "ES"));
            String nombreMes = l.getFechaActual().format(formatter);
            String nombreArchivo = "Recibo_" + apartamento.getNumero() + "_" + nombreMes + ".pdf";

            File archivoPDF = new File(aptoDir, nombreArchivo);

            PdfWriter writer = new PdfWriter(archivoPDF);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            doc.add(new Paragraph("Recibo de Agua - Apto " + apartamento.getNumero()));
            doc.add(new Paragraph("Propietario: " + apartamento.getPropietario()));
            doc.add(new Paragraph("Medidor: " + apartamento.getMedidor()));
            doc.add(new Paragraph("Fecha de lectura: " + l.getFechaActual()));
            doc.add(new Paragraph("Consumo: " + l.getConsumo() + " m³"));
            doc.add(new Paragraph("Valor a pagar: " + l.getValorPagar()));

            doc.close();

            // Abre la ventana de recibo detallado
            new ReciboFrame(apartamento, l);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar PDF: " + ex.getMessage());
        }
    }

    private void actualizarEstado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una lectura.");
            return;
        }

        Lectura lectura = apartamento.getHistorialLecturas().get(fila);

        EstadoPago[] opciones = EstadoPago.values();
        EstadoPago nuevoEstado = (EstadoPago) JOptionPane.showInputDialog(
                this,
                "Seleccione el estado del recibo:",
                "Estado de Pago",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                lectura.getEstado()
        );

        if (nuevoEstado != null) {
            lectura.setEstado(nuevoEstado);
            JOptionPane.showMessageDialog(this, "Estado actualizado a: " + nuevoEstado);
            cargarTabla();
        }
    }
}

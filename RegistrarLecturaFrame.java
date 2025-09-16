package com.mycompany.generadorrecibos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class RegistrarLecturaFrame extends JFrame {

    private DefaultTableModel modelo;
    private JTable tabla;
    private JTextField txtLecturaActual, txtFechaActual;
    private Apartamento apartamento;

    public RegistrarLecturaFrame(Apartamento apto) {
        this.apartamento = apto;

        setTitle("Registrar Nueva Lectura - Apto " + apto.getNumero());
        setSize(900, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Encabezados modificados
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

        // Panel de entrada
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Nueva Lectura Actual:"));
        txtLecturaActual = new JTextField();
        panel.add(txtLecturaActual);

        panel.add(new JLabel("Fecha Actual:"));
        txtFechaActual = new JTextField(LocalDate.now().toString()); // ✅ Fecha por defecto
        panel.add(txtFechaActual);

        // Botones
        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> registrarNuevaLectura());

        JButton btnEliminar = new JButton("Eliminar Lectura");
        btnEliminar.addActionListener(e -> eliminarLectura());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnEliminar);

        add(panel, BorderLayout.NORTH);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarTabla() {
    modelo.setRowCount(0);
    for (Lectura l : apartamento.getHistorialLecturas()) {
        modelo.addRow(new Object[]{
            l.getLecturaInicial(),      // Lectura Anterior
            l.getFechaInicial(),        // Fecha Anterior
            l.getLecturaActual(),       // Lectura Actual
            l.getFechaActual(),         // Fecha Actual
            l.getConsumo(),             // Consumo
            l.getValorAcueducto(),      // Valor Acueducto
            l.getValorAlcantarillado(), // Valor Alcantarillado
            l.getValorPagar(),          // Total
            l.getEstado()               // Estado
        });
    }
}

    private void registrarNuevaLectura() {
        try {
            double lecturaActual = Double.parseDouble(txtLecturaActual.getText());
            LocalDate fechaActual = LocalDate.parse(txtFechaActual.getText().trim());

            double lecturaAnterior;
            LocalDate fechaAnterior;

            if (apartamento.getHistorialLecturas().isEmpty()) {
                lecturaAnterior = 0;
                fechaAnterior = fechaActual;
            } else {
                Lectura ultima = apartamento.getHistorialLecturas()
                        .get(apartamento.getHistorialLecturas().size() - 1);
                lecturaAnterior = ultima.getLecturaActual();
                fechaAnterior = ultima.getFechaActual();
            }

            if (lecturaActual < lecturaAnterior) {
                JOptionPane.showMessageDialog(this,
                        "Error: la nueva lectura no puede ser menor que la anterior.");
                return;
            }

            Lectura nueva = new Lectura(
    lecturaAnterior,
    lecturaActual,
    fechaAnterior,
    fechaActual
);
apartamento.getHistorialLecturas().add(nueva);


            cargarTabla();
            JOptionPane.showMessageDialog(this, "Nueva lectura registrada correctamente.");
            txtLecturaActual.setText("");
            txtFechaActual.setText(LocalDate.now().toString()); // ✅ reinicia con fecha de hoy

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un número válido para la lectura.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en los datos: " + ex.getMessage());
        }
    }

    private void eliminarLectura() {
        int filaSeleccionada = tabla.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una lectura para eliminar.");
            return;
        }

        // Bloquear eliminación de la primera lectura
        if (filaSeleccionada == 0) {
            JOptionPane.showMessageDialog(this, "La primera lectura no puede eliminarse.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea eliminar esta lectura?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            apartamento.getHistorialLecturas().remove(filaSeleccionada);
            cargarTabla();
            JOptionPane.showMessageDialog(this, "Lectura eliminada correctamente.");
        }
    }
}

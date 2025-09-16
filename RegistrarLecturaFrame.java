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

        // Encabezados
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
        txtFechaActual = new JTextField(LocalDate.now().toString());
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

    private void registrarNuevaLectura() {
        try {
            double lecturaActual = Double.parseDouble(txtLecturaActual.getText().trim());
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

            // Crear nueva lectura
            Lectura nueva = new Lectura(lecturaAnterior, lecturaActual, fechaAnterior, fechaActual);
            apartamento.getHistorialLecturas().add(nueva);

            // Ajustar lecturas posteriores automáticamente si se modifica la primera lectura
            if (apartamento.getHistorialLecturas().size() > 1) {
                for (int i = 1; i < apartamento.getHistorialLecturas().size(); i++) {
                    Lectura actual = apartamento.getHistorialLecturas().get(i);
                    Lectura anterior = apartamento.getHistorialLecturas().get(i - 1);

                    // La lectura inicial siempre debe ser la lectura actual del mes anterior
                    double nuevaInicial = anterior.getLecturaActual();
                    double nuevaActual = actual.getLecturaActual(); // no cambia, solo se recalcula consumo
                    actual.setLecturas(nuevaInicial, nuevaActual);
                }
            }

            cargarTabla();
            JOptionPane.showMessageDialog(this, "Nueva lectura registrada correctamente.");
            txtLecturaActual.setText("");
            txtFechaActual.setText(LocalDate.now().toString());

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

            // Reajustar lectura inicial de la lectura siguiente
            if (filaSeleccionada < apartamento.getHistorialLecturas().size()) {
                Lectura siguiente = apartamento.getHistorialLecturas().get(filaSeleccionada);
                Lectura anterior = apartamento.getHistorialLecturas().get(filaSeleccionada - 1);
                siguiente.setLecturas(anterior.getLecturaActual(), siguiente.getLecturaActual());
            }

            cargarTabla();
            JOptionPane.showMessageDialog(this, "Lectura eliminada correctamente.");
        }
    }
}

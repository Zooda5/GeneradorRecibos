package com.mycompany.generadorrecibos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SelectorApartamentoFrame extends JFrame {
    private JTable tabla;
    private DefaultTableModel modelo;

    public SelectorApartamentoFrame() {
        setTitle("Gestión de Apartamentos");
        setSize(700, 450); // un poco más grande para que todo respire mejor
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        modelo = new DefaultTableModel(new Object[]{"Número", "Propietario", "Medidor"}, 0);
        tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);

        // Panel con FlowLayout para que los botones se acomoden bien
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAgregar = new JButton("Agregar Apartamento");
        JButton btnEditar = new JButton("Editar Apartamento");
        JButton btnEliminar = new JButton("Eliminar Apartamento");
        JButton btnLecturas = new JButton("Ver/Agregar Lecturas");

        // 🔹 Definimos un tamaño fijo para todos los botones
        Dimension botonGrande = new Dimension(200, 30);
        btnAgregar.setPreferredSize(botonGrande);
        btnEditar.setPreferredSize(botonGrande);
        btnEliminar.setPreferredSize(botonGrande);
        btnLecturas.setPreferredSize(botonGrande);

        // Acciones
        btnAgregar.addActionListener(e -> new RegistroApartamentoFrame(this, null).setVisible(true));

        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un apartamento de la tabla.");
                return;
            }
            String numero = (String) modelo.getValueAt(fila, 0);
            Apartamento seleccionado = BaseDatos.buscarApartamento(numero);
            if (seleccionado != null) {
                new RegistroApartamentoFrame(this, seleccionado).setVisible(true);
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un apartamento de la tabla.");
                return;
            }
            String numero = (String) modelo.getValueAt(fila, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de eliminar el apartamento " + numero + "?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                BaseDatos.getApartamentos().removeIf(a -> a.getNumero().equals(numero));
                cargarApartamentos();
            }
        });

        btnLecturas.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un apartamento de la tabla.");
                return;
            }
            String numero = (String) modelo.getValueAt(fila, 0);
            Apartamento seleccionado = BaseDatos.buscarApartamento(numero);
            if (seleccionado != null) {
                new RegistrarLecturaFrame(seleccionado).setVisible(true);
            }
        });

        // Agregamos los botones al panel
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLecturas);

        // Añadimos los componentes al frame
        add(scroll, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        cargarApartamentos();
    }

    public void cargarApartamentos() {
        modelo.setRowCount(0);
        for (Apartamento a : BaseDatos.getApartamentos()) {
            modelo.addRow(new Object[]{a.getNumero(), a.getPropietario(), a.getMedidor()});
        }
    }
}

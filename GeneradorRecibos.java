package com.mycompany.generadorrecibos;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Properties;

public class GeneradorRecibos extends JFrame {

    private static final String CONFIG_FILE =
            System.getProperty("user.home") + File.separator + ".generadorrecibos_config.properties";

    public GeneradorRecibos() {
        setTitle("Generador de Recibos de Agua");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 1, 10, 10)); // 🔹 Ahora 6 filas

        // 🔹 Intentar cargar primero desde CSV
        List<Apartamento> apartamentosCargados = GestorCSV.cargarDatos();

        // Si CSV está vacío, usamos Excel como respaldo inicial
        if (apartamentosCargados.isEmpty()) {
            apartamentosCargados = ExcelManager.cargarApartamentosDesdeExcel();
        }

        BaseDatos.setApartamentos(apartamentosCargados);

        // Botón para registrar apartamentos
        JButton btnRegistrar = new JButton("Registrar Apartamento");
        btnRegistrar.addActionListener(e -> {
            SelectorApartamentoFrame frame = new SelectorApartamentoFrame();
            frame.setVisible(true);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    // Guardar cambios en Excel
                    for (Apartamento apto : BaseDatos.getApartamentos()) {
                        ExcelManager.exportarApartamento(apto);
                    }
                    ExcelManager.exportarConsolidado(BaseDatos.getApartamentos());
                    // Guardar también en CSV
                    GestorCSV.guardarDatos(BaseDatos.getApartamentos());
                }
            });
        });
        add(btnRegistrar);

        // Botón para registrar nueva lectura
        JButton btnNuevaLectura = new JButton("Registrar Nueva Lectura");
        btnNuevaLectura.addActionListener(e -> {
            if (BaseDatos.getApartamentos().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay apartamentos registrados aún.");
                return;
            }
            String[] opciones = BaseDatos.getApartamentos().stream()
                    .map(Apartamento::getNumero)
                    .toArray(String[]::new);

            String seleccionado = (String) JOptionPane.showInputDialog(
                    this,
                    "Seleccione el apartamento:",
                    "Seleccionar Apartamento",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );

            if (seleccionado != null) {
                Apartamento apto = BaseDatos.buscarApartamento(seleccionado);
                if (apto != null) {
                    RegistrarLecturaFrame frame = new RegistrarLecturaFrame(apto);
                    frame.setVisible(true);
                    frame.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            // Guardar cambios en Excel
                            ExcelManager.actualizarBackups(BaseDatos.getApartamentos(), apto);
                            // Guardar también en CSV
                            GestorCSV.guardarDatos(BaseDatos.getApartamentos());
                        }
                    });
                }
            }
        });
        add(btnNuevaLectura);

        // Botón para consultar recibos
        JButton btnRecibo = new JButton("Consultar Recibos");
        btnRecibo.addActionListener(e -> {
            if (BaseDatos.getApartamentos().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay apartamentos registrados aún.");
                return;
            }
            String[] opciones = BaseDatos.getApartamentos().stream()
                    .map(Apartamento::getNumero)
                    .toArray(String[]::new);

            String seleccionado = (String) JOptionPane.showInputDialog(
                    this,
                    "Seleccione el apartamento:",
                    "Seleccionar Apartamento",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );

            if (seleccionado != null) {
                Apartamento apto = BaseDatos.buscarApartamento(seleccionado);
                if (apto != null) {
                    new ConsultaReciboFrame(apto).setVisible(true);
                }
            }
        });
        add(btnRecibo);

        // 🔹 Botón para gestionar precios
        JButton btnGestionPrecios = new JButton("Gestionar Precios");
        btnGestionPrecios.addActionListener(e -> {
            GestionPreciosFrame frame = new GestionPreciosFrame();
            frame.setVisible(true);
        });
        add(btnGestionPrecios);

        // Botón para exportar a Excel
        JButton btnExportarExcel = new JButton("Exportar a Excel");
        btnExportarExcel.addActionListener(e -> {
            if (BaseDatos.getApartamentos().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay apartamentos para exportar.");
                return;
            }

            String[] opcionesExport = {"Apartamento Específico", "Consolidado"};
            int opcion = JOptionPane.showOptionDialog(
                    this,
                    "Seleccione qué desea exportar:",
                    "Exportar a Excel",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcionesExport,
                    opcionesExport[0]
            );

            try {
                if (opcion == 0) { // Apartamento específico
                    String[] opciones = BaseDatos.getApartamentos().stream()
                            .map(Apartamento::getNumero)
                            .toArray(String[]::new);

                    String seleccionado = (String) JOptionPane.showInputDialog(
                            this,
                            "Seleccione el apartamento:",
                            "Seleccionar Apartamento",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            opciones,
                            opciones[0]
                    );

                    if (seleccionado != null) {
                        Apartamento apto = BaseDatos.buscarApartamento(seleccionado);
                        if (apto != null) {
                            ExcelManager.exportarApartamento(apto);
                            JOptionPane.showMessageDialog(this, "Apartamento exportado exitosamente.");
                        }
                    }
                } else if (opcion == 1) { // Consolidado
                    ExcelManager.exportarConsolidado(BaseDatos.getApartamentos());
                    JOptionPane.showMessageDialog(this, "Consolidado exportado exitosamente.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al exportar a Excel: " + ex.getMessage());
            }
        });
        add(btnExportarExcel);

        // 🔹 Guardar CSV automáticamente al cerrar la aplicación
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                for (Apartamento apto : BaseDatos.getApartamentos()) {
                    ExcelManager.exportarApartamento(apto);
                }
                ExcelManager.exportarConsolidado(BaseDatos.getApartamentos());
                GestorCSV.guardarDatos(BaseDatos.getApartamentos());
                dispose();
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (debeMostrarAviso()) {
                AvisoPreciosFrame aviso = new AvisoPreciosFrame();
                aviso.setVisible(true);

                aviso.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        guardarPreferencia(!aviso.chkNoMostrar.isSelected());
                        new GeneradorRecibos().setVisible(true);
                    }
                });
            } else {
                new GeneradorRecibos().setVisible(true);
            }
        });
    }

    // Configuración de aviso
    private static boolean debeMostrarAviso() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            return Boolean.parseBoolean(prop.getProperty("mostrarAviso", "true"));
        } catch (IOException e) {
            return true;
        }
    }

    private static void guardarPreferencia(boolean mostrar) {
        Properties prop = new Properties();
        prop.setProperty("mostrarAviso", String.valueOf(mostrar));
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            prop.store(output, "Configuración del Generador de Recibos");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

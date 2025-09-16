package com.mycompany.generadorrecibos;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;

public class RegistroApartamentoFrame extends JFrame {

    private JTextField txtNumero, txtPropietario, txtMedidor;
    private JTextField txtLecturaInicial, txtLecturaActual, txtFechaInicial, txtFechaActual;
    private JButton btnGuardar;
    private SelectorApartamentoFrame parent; // referencia al selector
    private Apartamento apartamento; // para edición

    // Constructor para crear
    public RegistroApartamentoFrame(SelectorApartamentoFrame parent) {
        this(parent, null);
    }

    // Constructor para editar
    public RegistroApartamentoFrame(SelectorApartamentoFrame parent, Apartamento apartamento) {
        this.parent = parent;
        this.apartamento = apartamento;

        setTitle(apartamento == null ? "Registrar Apartamento" : "Editar Apartamento");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(9, 2, 10, 10));

        // Campos básicos
        add(new JLabel("Número de Apartamento:"));
        txtNumero = new JTextField();
        add(txtNumero);

        add(new JLabel("Propietario:"));
        txtPropietario = new JTextField();
        add(txtPropietario);

        add(new JLabel("Número de Medidor:"));
        txtMedidor = new JTextField();
        add(txtMedidor);

        // Campos de lecturas
        add(new JLabel("Lectura Inicial:"));
        txtLecturaInicial = new JTextField();
        add(txtLecturaInicial);

        add(new JLabel("Fecha Inicial (YYYY-MM-DD):"));
        txtFechaInicial = new JTextField(LocalDate.now().toString());
        add(txtFechaInicial);

        add(new JLabel("Lectura Actual:"));
        txtLecturaActual = new JTextField();
        add(txtLecturaActual);

        add(new JLabel("Fecha Actual (YYYY-MM-DD):"));
        txtFechaActual = new JTextField(LocalDate.now().toString());
        add(txtFechaActual);

        // Botón guardar
        btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarApartamento());
        add(new JLabel()); // Espaciador
        add(btnGuardar);

        // Precargar datos si estamos editando
        if (apartamento != null) {
            txtNumero.setText(apartamento.getNumero());
            txtNumero.setEnabled(false); // no se puede cambiar el número
            txtPropietario.setText(apartamento.getPropietario());
            txtMedidor.setText(apartamento.getMedidor());

            // Precargar lecturas si existen
            if (!apartamento.getHistorialLecturas().isEmpty()) {
                Lectura primera = apartamento.getHistorialLecturas().get(0);
                txtLecturaInicial.setText(String.valueOf(primera.getLecturaInicial()));
                txtLecturaActual.setText(String.valueOf(primera.getLecturaActual()));
                txtFechaInicial.setText(primera.getFechaInicial().toString());
                txtFechaActual.setText(primera.getFechaActual().toString());
            }
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void guardarApartamento() {
        String numero = txtNumero.getText().trim();
        String propietario = txtPropietario.getText().trim();
        String medidor = txtMedidor.getText().trim();
        String lecturaInicialStr = txtLecturaInicial.getText().trim();
        String lecturaActualStr = txtLecturaActual.getText().trim();
        String fechaInicialStr = txtFechaInicial.getText().trim();
        String fechaActualStr = txtFechaActual.getText().trim();

        if (numero.isEmpty() || propietario.isEmpty() || medidor.isEmpty()
                || lecturaInicialStr.isEmpty() || lecturaActualStr.isEmpty()
                || fechaInicialStr.isEmpty() || fechaActualStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return;
        }

        try {
            double lecturaInicial = Double.parseDouble(lecturaInicialStr);
            double lecturaActual = Double.parseDouble(lecturaActualStr);
            LocalDate fechaInicial = LocalDate.parse(fechaInicialStr);
            LocalDate fechaActual = LocalDate.parse(fechaActualStr);

            if (apartamento != null) {
                // Guardamos valores básicos
                apartamento.setPropietario(propietario);
                apartamento.setMedidor(medidor);

                // Lectura inicial y actual anteriores
                double lecturaInicialAnterior = apartamento.getHistorialLecturas().isEmpty() ?
                        lecturaInicial : apartamento.getHistorialLecturas().get(0).getLecturaInicial();
                double lecturaActualAnterior = apartamento.getHistorialLecturas().isEmpty() ?
                        lecturaActual : apartamento.getHistorialLecturas().get(0).getLecturaActual();

                // Reemplazar o crear primera lectura
                Lectura primera = new Lectura(lecturaInicial, lecturaActual, fechaInicial, fechaActual);
                if (!apartamento.getHistorialLecturas().isEmpty()) {
                    apartamento.getHistorialLecturas().set(0, primera);
                } else {
                    apartamento.getHistorialLecturas().add(primera);
                }

                // Ajuste de lecturas posteriores si cambia la inicial o la actual
                double diferenciaInicial = lecturaInicial - lecturaInicialAnterior;
                double diferenciaActual = lecturaActual - lecturaActualAnterior;

                if ((diferenciaInicial != 0 || diferenciaActual != 0) && apartamento.getHistorialLecturas().size() > 1) {
                    int opcion = JOptionPane.showConfirmDialog(
                            this,
                            "La lectura inicial o actual cambió. ¿Desea ajustar automáticamente todas las lecturas posteriores?\n" +
                                    "Diferencia inicial: " + diferenciaInicial + ", Diferencia actual: " + diferenciaActual,
                            "Ajustar lecturas",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (opcion == JOptionPane.YES_OPTION) {
                        for (int i = 1; i < apartamento.getHistorialLecturas().size(); i++) {
                            Lectura l = apartamento.getHistorialLecturas().get(i);
                            Lectura ajustada = new Lectura(
                                    l.getLecturaInicial() + diferenciaInicial,
                                    l.getLecturaActual() + diferenciaActual,
                                    l.getFechaInicial(),
                                    l.getFechaActual()
                            );
                            apartamento.getHistorialLecturas().set(i, ajustada);
                        }
                    }
                }

                JOptionPane.showMessageDialog(this, "Apartamento actualizado correctamente.");
            } else {
                // creación
                if (BaseDatos.buscarApartamento(numero) != null) {
                    JOptionPane.showMessageDialog(this, "El apartamento ya existe.");
                    return;
                }
                Apartamento nuevo = new Apartamento(numero, propietario, medidor);
                Lectura primeraLectura = new Lectura(lecturaInicial, lecturaActual, fechaInicial, fechaActual);
                nuevo.getHistorialLecturas().add(primeraLectura);

                // Crear carpeta del apartamento
                String baseDir = System.getProperty("user.dir");
                File aptoDir = new File(baseDir, "Apartamento_" + numero);
                if (!aptoDir.exists()) {
                    aptoDir.mkdirs();
                }

                BaseDatos.agregarApartamento(nuevo);
                JOptionPane.showMessageDialog(this,
                        "Apartamento registrado exitosamente con su primera lectura.\n" +
                                "Se creó la carpeta: " + aptoDir.getAbsolutePath());
            }

            parent.cargarApartamentos(); // refrescar tabla
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en los datos: " + ex.getMessage());
        }
    }
}

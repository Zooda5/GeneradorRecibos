package com.mycompany.generadorrecibos;

import javax.swing.*;
import java.awt.*;

public class GestionPreciosFrame extends JFrame {

    private JTextField txtAcueducto, txtAlcantarillado;

    public GestionPreciosFrame() {
        setTitle("Gestión de Precios");
        setSize(400,150);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3,2,5,5));

        // Precios actuales
        add(new JLabel("Precio Acueducto:"));
        txtAcueducto = new JTextField(String.valueOf(BaseDatos.getPrecioAcueducto()));
        add(txtAcueducto);

        add(new JLabel("Precio Alcantarillado:"));
        txtAlcantarillado = new JTextField(String.valueOf(BaseDatos.getPrecioAlcantarillado()));
        add(txtAlcantarillado);

        JButton btnGuardar = new JButton("Guardar Precios");
        btnGuardar.addActionListener(e -> {
            try {
                double precioAcu = Double.parseDouble(txtAcueducto.getText());
                double precioAlc = Double.parseDouble(txtAlcantarillado.getText());

                BaseDatos.setPrecioAcueducto(precioAcu);
                BaseDatos.setPrecioAlcantarillado(precioAlc);

                JOptionPane.showMessageDialog(this,"Precios actualizados correctamente.");
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,"Ingrese valores numéricos válidos.");
            }
        });
        add(new JLabel()); // espaciador
        add(btnGuardar);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}

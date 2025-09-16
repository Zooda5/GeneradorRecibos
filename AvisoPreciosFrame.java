package com.mycompany.generadorrecibos;

import javax.swing.*;
import java.awt.*;

public class AvisoPreciosFrame extends JFrame {

    JCheckBox chkNoMostrar; // accesible desde GeneradorRecibos
    private boolean mostrarMensaje = true; // controla si se muestra de nuevo

    public AvisoPreciosFrame() {
        setTitle("Aviso de Precios");
        setSize(450, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Mensaje principal
        JLabel mensaje = new JLabel(
            "<html><center>Antes de registrar nuevas lecturas, asegúrese de<br>" +
            "actualizar el precio del Acueducto y Alcantarillado<br>" +
            "en la sección 'Gestión de Precios' si han cambiado.</center></html>",
            SwingConstants.CENTER
        );
        add(mensaje, BorderLayout.CENTER);

        // Panel inferior para checkbox y botón
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));

        // Casilla "No volver a mostrar"
        chkNoMostrar = new JCheckBox("No volver a mostrar este mensaje");
        chkNoMostrar.setAlignmentX(Component.CENTER_ALIGNMENT); // centrar horizontalmente
        panelInferior.add(chkNoMostrar);
        panelInferior.add(Box.createRigidArea(new Dimension(0, 10))); // espacio entre checkbox y botón

        // Botón Aceptar
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAceptar.addActionListener(e -> {
            mostrarMensaje = !chkNoMostrar.isSelected(); // guarda estado en variable
            dispose(); // cierra la ventana
        });
        panelInferior.add(btnAceptar);

        add(panelInferior, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    // Método para obtener si se debe mostrar de nuevo
    public boolean mostrarMensaje() {
        return mostrarMensaje;
    }
}

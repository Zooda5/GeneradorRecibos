package com.mycompany.generadorrecibos;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class GestorCSV {

    private static final String ARCHIVO;

    static {
        // Detecta la carpeta donde se está ejecutando el programa (.exe o .jar)
        String ruta = System.getProperty("user.dir"); 
        ARCHIVO = Paths.get(ruta, "datos.csv").toString();
    }

    // -------------------- GUARDAR --------------------
    public static void guardarDatos(List<Apartamento> apartamentos) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ARCHIVO), "UTF-8"))) {
            for (Apartamento apto : apartamentos) {
                for (Lectura lectura : apto.getHistorialLecturas()) {
                    writer.println(
                        apto.getNumero() + ";" +
                        apto.getPropietario() + ";" +
                        apto.getMedidor() + ";" +
                        lectura.getLecturaInicial() + ";" +
                        lectura.getLecturaActual() + ";" +
                        lectura.getFechaInicial() + ";" +
                        lectura.getFechaActual() + ";" +
                        lectura.getConsumo() + ";" +
                        lectura.getValorAcueducto() + ";" +
                        lectura.getValorAlcantarillado() + ";" +
                        lectura.getValorPagar() + ";" +
                        lectura.getEstado()
                    );
                }
            }
        } catch (IOException e) {
            System.out.println("Error guardando datos en CSV: " + e.getMessage());
        }
    }

    // -------------------- CARGAR --------------------
    public static List<Apartamento> cargarDatos() {
        List<Apartamento> apartamentos = new ArrayList<>();

        if (!Files.exists(Paths.get(ARCHIVO))) {
            return apartamentos; // No hay archivo aún
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ARCHIVO), "UTF-8"))) {
            String linea;
            Map<String, Apartamento> mapa = new HashMap<>();

            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                if (partes.length < 12) continue; // línea inválida

                String numero = partes[0];
                String propietario = partes[1];
                String medidor = partes[2];

                double lecturaInicial = Double.parseDouble(partes[3]);
                double lecturaActual = Double.parseDouble(partes[4]);
                LocalDate fechaInicial = LocalDate.parse(partes[5]);
                LocalDate fechaActual = LocalDate.parse(partes[6]);

                double consumo = Double.parseDouble(partes[7]);
                double valorAcueducto = Double.parseDouble(partes[8]);
                double valorAlcantarillado = Double.parseDouble(partes[9]);
                double valorPagar = Double.parseDouble(partes[10]);

                EstadoPago estado = EstadoPago.valueOf(partes[11]);

                Apartamento apto = mapa.getOrDefault(
                        numero,
                        new Apartamento(numero, propietario, medidor)
                );

                Lectura lectura = new Lectura(lecturaInicial, lecturaActual, fechaInicial, fechaActual);
                lectura.setValores(consumo, valorAcueducto, valorAlcantarillado, valorPagar);
                lectura.setEstado(estado);

                apto.getHistorialLecturas().add(lectura);
                mapa.put(numero, apto);
            }

            apartamentos.addAll(mapa.values());

        } catch (IOException e) {
            System.out.println("Error leyendo datos del CSV: " + e.getMessage());
        }

        return apartamentos;
    }
}

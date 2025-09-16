package com.mycompany.generadorrecibos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BaseDatos {

    private static List<Apartamento> apartamentos = new ArrayList<>();

    private static double precioAcueducto = 3070.0;
    private static double precioAlcantarillado = 3740.0;

    private static final String CONFIG_FILE = System.getProperty("user.home")
            + File.separator + "config_recibos.properties";

    static {
        cargarPrecios();
    }

    // ------------------ APARTAMENTOS ------------------
    public static List<Apartamento> getApartamentos() {
        return apartamentos;
    }

    public static void setApartamentos(List<Apartamento> lista) {
        apartamentos = lista; // ✅ Nuevo método para inicializar toda la lista
    }

    public static Apartamento buscarApartamento(String numero) {
        for (Apartamento a : apartamentos) {
            if (a.getNumero().equals(numero)) {
                return a;
            }
        }
        return null;
    }

    public static void agregarApartamento(Apartamento apartamento) {
        if (buscarApartamento(apartamento.getNumero()) == null) {
            apartamentos.add(apartamento);
        }
    }

    public static void actualizarApartamento(String numero, Apartamento actualizado) {
        for (int i = 0; i < apartamentos.size(); i++) {
            if (apartamentos.get(i).getNumero().equals(numero)) {
                apartamentos.set(i, actualizado);
                break;
            }
        }
    }

    // ------------------ PRECIOS ------------------
    public static double getPrecioAcueducto() {
        return precioAcueducto;
    }

    public static double getPrecioAlcantarillado() {
        return precioAlcantarillado;
    }

    public static void setPrecioAcueducto(double nuevoPrecio) {
        precioAcueducto = nuevoPrecio;
        guardarPrecios();
    }

    public static void setPrecioAlcantarillado(double nuevoPrecio) {
        precioAlcantarillado = nuevoPrecio;
        guardarPrecios();
    }

    // ------------------ GUARDAR Y CARGAR CONFIG ------------------
    private static void cargarPrecios() {
        try {
            File archivo = new File(CONFIG_FILE);
            if (archivo.exists()) {
                Properties prop = new Properties();
                prop.load(new FileInputStream(archivo));
                precioAcueducto = Double.parseDouble(prop.getProperty("precioAcueducto", "3070.0"));
                precioAlcantarillado = Double.parseDouble(prop.getProperty("precioAlcantarillado", "3740.0"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            precioAcueducto = 3070.0;
            precioAlcantarillado = 3740.0;
        }
    }

    private static void guardarPrecios() {
        try {
            Properties prop = new Properties();
            prop.setProperty("precioAcueducto", String.valueOf(precioAcueducto));
            prop.setProperty("precioAlcantarillado", String.valueOf(precioAlcantarillado));
            prop.store(new FileOutputStream(CONFIG_FILE), "Precios de Acueducto y Alcantarillado");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

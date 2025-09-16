package com.mycompany.generadorrecibos;

import java.util.ArrayList;
import java.util.List;

public class Apartamento {
    private String numero;
    private String propietario;
    private String medidor;
    private List<Lectura> historialLecturas;

    public Apartamento(String numero, String propietario, String medidor) {
        this.numero = numero;
        this.propietario = propietario;
        this.medidor = medidor;
        this.historialLecturas = new ArrayList<>();
    }

    // Getters
    public String getNumero() {
        return numero;
    }

    public String getPropietario() {
        return propietario;
    }

    public String getMedidor() {
        return medidor;
    }

    public List<Lectura> getHistorialLecturas() {
        return historialLecturas;
    }

    // Setters
    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public void setMedidor(String medidor) {
        this.medidor = medidor;
    }

    // Métodos nuevos para ExcelManager
    public void agregarLectura(Lectura lectura) {
        historialLecturas.add(lectura);
    }

    /**
     * Devuelve la última lectura registrada o null si no hay ninguna.
     */
    public Lectura getUltimaLectura() {
        if (historialLecturas.isEmpty()) {
            return null;
        }
        return historialLecturas.get(historialLecturas.size() - 1);
    }

    /**
     * Devuelve el consumo entre la última y penúltima lectura.
     * Retorna 0 si hay menos de 2 lecturas.
     */
    public double getConsumoUltimoMes() {
        if (historialLecturas.size() < 2) {
            return 0;
        }
        Lectura ultima = getUltimaLectura();
        Lectura anterior = historialLecturas.get(historialLecturas.size() - 2);
        return ultima.getValorPagar() - anterior.getValorPagar();
    }

    @Override
    public String toString() {
        return "Apartamento " + numero + " - " + propietario;
    }
}

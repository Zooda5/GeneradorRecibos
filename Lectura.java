package com.mycompany.generadorrecibos;

import java.time.LocalDate;

public class Lectura {
    private double lecturaInicial;
    private double lecturaActual;
    private LocalDate fechaInicial;
    private LocalDate fechaActual;

    private double consumo;
    private double valorAcueducto;
    private double valorAlcantarillado;
    private double valorPagar;

    private EstadoPago estado; // PENDIENTE, PAGADO, NO_PAGO

    public Lectura(double lecturaInicial, double lecturaActual,
                   LocalDate fechaInicial, LocalDate fechaActual) {
        this.lecturaInicial = lecturaInicial;
        this.lecturaActual = lecturaActual;
        this.fechaInicial = fechaInicial;
        this.fechaActual = fechaActual;

        calcularConsumoYValores();
        this.estado = EstadoPago.PENDIENTE; // por defecto
    }

    private void calcularConsumoYValores() {
        this.consumo = Math.max(0, lecturaActual - lecturaInicial);
        this.valorAcueducto = consumo * BaseDatos.getPrecioAcueducto();
        this.valorAlcantarillado = consumo * BaseDatos.getPrecioAlcantarillado();
        this.valorPagar = valorAcueducto + valorAlcantarillado;
    }

    // Getters
    public double getLecturaInicial() { return lecturaInicial; }
    public double getLecturaActual() { return lecturaActual; }
    public LocalDate getFechaInicial() { return fechaInicial; }
    public LocalDate getFechaActual() { return fechaActual; }
    public double getConsumo() { return consumo; }
    public double getValorAcueducto() { return valorAcueducto; }
    public double getValorAlcantarillado() { return valorAlcantarillado; }
    public double getValorPagar() { return valorPagar; }
    public EstadoPago getEstado() { return estado; }

    // Setters
    public void setEstado(EstadoPago estado) { this.estado = estado; }

    // Nuevo método para actualizar lecturas y recalcular valores
    public void setLecturas(double nuevaInicial, double nuevaActual) {
        this.lecturaInicial = nuevaInicial;
        this.lecturaActual = nuevaActual;
        calcularConsumoYValores();
    }

    @Override
    public String toString() {
        return "Lectura Inicial: " + lecturaInicial +
                " | Lectura Actual: " + lecturaActual +
                " | Fecha Inicial: " + fechaInicial +
                " | Fecha Actual: " + fechaActual +
                " | Consumo: " + consumo +
                " | Valor Acueducto: $" + valorAcueducto +
                " | Valor Alcantarillado: $" + valorAlcantarillado +
                " | Total a Pagar: $" + valorPagar +
                " | Estado: " + estado;
    }
}

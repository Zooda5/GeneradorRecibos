package com.mycompany.generadorrecibos;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExcelManager {

    private static final String RUTA_BASE = System.getProperty("user.dir") + File.separator;

    private static File obtenerCarpetaApartamento(String numeroApto) {
        File dir = new File(RUTA_BASE, "Apartamento_" + numeroApto);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    // ------------------ EXPORTAR ------------------

    public static void exportarApartamento(Apartamento apto) {
        File carpeta = obtenerCarpetaApartamento(apto.getNumero());
        File archivo = new File(carpeta, "Historial.xlsx");
        Workbook workbook;
        Sheet sheet;

        try {
            if (archivo.exists()) {
                FileInputStream fis = new FileInputStream(archivo);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Historial");
                crearCabecera(sheet, new String[]{
                        "Lectura Inicial", "Fecha Inicial",
                        "Lectura Actual", "Fecha Actual",
                        "Consumo", "Valor Acueducto",
                        "Valor Alcantarillado", "Total", "Estado",
                        "Propietario", "Medidor"
                });
            }

            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row != null) sheet.removeRow(row);
            }

            int filaNum = 1;
            for (Lectura lectura : apto.getHistorialLecturas()) {
                Row fila = sheet.createRow(filaNum++);
                llenarFilaLectura(fila, lectura, apto);
            }

            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                workbook.write(fos);
            }
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportarConsolidado(List<Apartamento> apartamentos) {
        File archivo = new File(RUTA_BASE, "Consolidado.xlsx");
        Workbook workbook;
        Sheet sheet;

        try {
            if (archivo.exists()) {
                FileInputStream fis = new FileInputStream(archivo);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Consolidado");
                crearCabecera(sheet, new String[]{
                        "Apartamento", "Propietario", "Medidor",
                        "Lectura Inicial", "Fecha Inicial",
                        "Lectura Actual", "Fecha Actual",
                        "Consumo", "Valor Acueducto",
                        "Valor Alcantarillado", "Total", "Estado"
                });
            }

            int filaNum = 1;
            for (Apartamento apto : apartamentos) {
                Lectura ultima = apto.getUltimaLectura();
                if (ultima == null) continue;
                Row fila = sheet.createRow(filaNum++);
                llenarFilaLecturaConsolidado(fila, ultima, apto);
            }

            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                workbook.write(fos);
            }
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void actualizarBackups(List<Apartamento> apartamentos, Apartamento apto) {
        exportarApartamento(apto);
        exportarConsolidado(apartamentos);
    }

    // ------------------ AUXILIARES ------------------

    private static void crearCabecera(Sheet sheet, String[] columnas) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            header.createCell(i).setCellValue(columnas[i]);
        }
    }

    private static void llenarFilaLectura(Row fila, Lectura lectura, Apartamento apto) {
        fila.createCell(0).setCellValue(lectura.getLecturaInicial());
        fila.createCell(1).setCellValue(lectura.getFechaInicial().toString());
        fila.createCell(2).setCellValue(lectura.getLecturaActual());
        fila.createCell(3).setCellValue(lectura.getFechaActual().toString());
        fila.createCell(4).setCellValue(lectura.getConsumo());
        fila.createCell(5).setCellValue(lectura.getValorAcueducto());
        fila.createCell(6).setCellValue(lectura.getValorAlcantarillado());
        fila.createCell(7).setCellValue(lectura.getValorPagar());
        fila.createCell(8).setCellValue(lectura.getEstado().toString());
        fila.createCell(9).setCellValue(apto.getPropietario());
        fila.createCell(10).setCellValue(apto.getMedidor());
    }

    private static void llenarFilaLecturaConsolidado(Row fila, Lectura lectura, Apartamento apto) {
        fila.createCell(0).setCellValue(apto.getNumero());
        fila.createCell(1).setCellValue(apto.getPropietario());
        fila.createCell(2).setCellValue(apto.getMedidor());
        fila.createCell(3).setCellValue(lectura.getLecturaInicial());
        fila.createCell(4).setCellValue(lectura.getFechaInicial().toString());
        fila.createCell(5).setCellValue(lectura.getLecturaActual());
        fila.createCell(6).setCellValue(lectura.getFechaActual().toString());
        fila.createCell(7).setCellValue(lectura.getConsumo());
        fila.createCell(8).setCellValue(lectura.getValorAcueducto());
        fila.createCell(9).setCellValue(lectura.getValorAlcantarillado());
        fila.createCell(10).setCellValue(lectura.getValorPagar());
        fila.createCell(11).setCellValue(lectura.getEstado().toString());
    }

    // ------------------ CARGAR APARTAMENTOS ------------------

    public static List<Apartamento> cargarApartamentosDesdeExcel() {
        List<Apartamento> lista = new ArrayList<>();
        File baseFolder = new File(RUTA_BASE);
        File[] carpetas = baseFolder.listFiles((dir, name) -> name.startsWith("Apartamento_") && new File(dir, name).isDirectory());
        if (carpetas == null) return lista;

        for (File carpeta : carpetas) {
            File archivo = new File(carpeta, "Historial.xlsx");
            if (!archivo.exists()) continue;

            String numero = carpeta.getName().replace("Apartamento_", "");
            try (FileInputStream fis = new FileInputStream(archivo);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null) continue;

                Apartamento apto = null;
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row fila = sheet.getRow(i);
                    if (fila == null) continue;

                    String propietario = getCellString(fila, 9, "Desconocido");
                    String medidor = getCellString(fila, 10, "Desconocido");
                    if (apto == null) apto = new Apartamento(numero, propietario, medidor);

                    try {
                        double lecturaInicial = getCellNumeric(fila, 0);
                        LocalDate fechaInicial = LocalDate.parse(getCellString(fila, 1, LocalDate.now().toString()));
                        double lecturaActual = getCellNumeric(fila, 2);
                        LocalDate fechaActual = LocalDate.parse(getCellString(fila, 3, LocalDate.now().toString()));
                        EstadoPago estado = EstadoPago.valueOf(getCellString(fila, 8, "PENDIENTE"));

                        Lectura lectura = new Lectura(lecturaInicial, lecturaActual, fechaInicial, fechaActual);
                        lectura.setEstado(estado);

                        apto.agregarLectura(lectura);
                    } catch (Exception ex) {
                        System.out.println("Fila " + i + " ignorada por datos inválidos.");
                    }
                }

                if (apto != null) lista.add(apto);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lista;
    }

    // ------------------ LECTURA CELDAS ------------------

    private static String getCellString(Row fila, int index, String defaultVal) {
        Cell c = fila.getCell(index);
        if (c == null) return defaultVal;
        try {
            if (c.getCellType() == CellType.STRING) return c.getStringCellValue();
            else if (c.getCellType() == CellType.NUMERIC) return String.valueOf((int) c.getNumericCellValue());
            else return defaultVal;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static double getCellNumeric(Row fila, int index) {
        Cell c = fila.getCell(index);
        if (c == null) return 0.0;
        try {
            if (c.getCellType() == CellType.NUMERIC) return c.getNumericCellValue();
            else if (c.getCellType() == CellType.STRING) return Double.parseDouble(c.getStringCellValue());
            else return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}

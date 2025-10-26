package modelo.datos;

import modelo.estructuras.IDiccionario;
import modelo.estructuras.ListaDoble;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NormalizadorFecha {

    private LocalDate fechaMinima;
    private LocalDate fechaMaxima;
    private DateTimeFormatter formateador;

    public NormalizadorFecha() {
        this.formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.fechaMinima = LocalDate.of(1900, 1, 1);
        this.fechaMaxima = LocalDate.now();
    }

    public void extraerRango(ListaDoble<IDiccionario<String, String>> filas, String columnaFecha) {
        ListaDoble<LocalDate> fechas = new ListaDoble<>();

        for (int i = 0; i < filas.tamanio(); i++) {
            IDiccionario<String, String> fila = filas.obtener(i);
            String fechaStr = fila.obtener(columnaFecha);
            if (fechaStr != null && !fechaStr.isEmpty() && !fechaStr.equals("null")) {
                try {
                    LocalDate fecha = LocalDate.parse(fechaStr.trim(), formateador);
                    fechas.agregar(fecha);
                } catch (Exception e) {
                    // ignorar fechas invalidas
                }
            }
        }

        if (fechas.tamanio() > 0) {
            fechaMinima = encontrarMinimo(fechas);
            fechaMaxima = encontrarMaximo(fechas);
        }
    }

    private LocalDate encontrarMinimo(ListaDoble<LocalDate> fechas) {
        LocalDate min = fechas.obtener(0);
        for (int i = 1; i < fechas.tamanio(); i++) {
            LocalDate actual = fechas.obtener(i);
            if (actual.isBefore(min)) {
                min = actual;
            }
        }
        return min;
    }

    private LocalDate encontrarMaximo(ListaDoble<LocalDate> fechas) {
        LocalDate max = fechas.obtener(0);
        for (int i = 1; i < fechas.tamanio(); i++) {
            LocalDate actual = fechas.obtener(i);
            if (actual.isAfter(max)) {
                max = actual;
            }
        }
        return max;
    }

    public double normalizar(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty() || fechaStr.equals("null")) {
            return 0.5; // Valor medio si es nula
        }

        try {
            LocalDate fecha = LocalDate.parse(fechaStr.trim(), formateador);

            long diasDesdeMinimo = java.time.temporal.ChronoUnit.DAYS.between(fechaMinima, fecha);
            long diasTotales = java.time.temporal.ChronoUnit.DAYS.between(fechaMinima, fechaMaxima);

            if (diasTotales == 0) {
                return 0.5;
            }

            return (double) diasDesdeMinimo / diasTotales;
        } catch (Exception e) {
            return 0.5;
        }
    }

    public LocalDate getFechaMinima() {
        return fechaMinima;
    }

    public LocalDate getFechaMaxima() {
        return fechaMaxima;
    }
}

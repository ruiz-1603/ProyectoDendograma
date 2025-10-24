package modelo.datos;

import modelo.estructuras.IDiccionario;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Responsabilidad: Normalizar fechas a valores numéricos [0, 1]
 */
public class NormalizadorFecha {

    private LocalDate fechaMinima;
    private LocalDate fechaMaxima;
    private DateTimeFormatter formateador;

    public NormalizadorFecha() {
        this.formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.fechaMinima = LocalDate.of(1900, 1, 1);
        this.fechaMaxima = LocalDate.now();
    }

    public void extraerRango(List<IDiccionario<String, String>> filas, String columnaFecha) {
        List<LocalDate> fechas = new ArrayList<>();

        for (IDiccionario<String, String> fila : filas) {
            String fechaStr = fila.obtener(columnaFecha);
            if (fechaStr != null && !fechaStr.isEmpty() && !fechaStr.equals("null")) {
                try {
                    LocalDate fecha = LocalDate.parse(fechaStr.trim(), formateador);
                    fechas.add(fecha);
                } catch (Exception e) {
                    // Ignorar fechas inválidas
                }
            }
        }

        if (!fechas.isEmpty()) {
            fechaMinima = Collections.min(fechas);
            fechaMaxima = Collections.max(fechas);
        }
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

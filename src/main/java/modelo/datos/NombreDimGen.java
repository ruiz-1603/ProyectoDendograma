package modelo.datos;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsabilidad: Generar nombres descriptivos para cada dimensión del vector
 */
public class NombreDimGen {

    private ConfiguradorColumnas configurador;
    private ExtractorCategorias extractorCategorias;

    public NombreDimGen(ConfiguradorColumnas configurador,
                                       ExtractorCategorias extractorCategorias) {
        this.configurador = configurador;
        this.extractorCategorias = extractorCategorias;
    }

    public String[] generar() {
        List<String> nombres = new ArrayList<>();

        // 1. Columnas numéricas
        for (String col : configurador.getColumnasNumericas()) {
            nombres.add(col);
        }

        // 2. Columnas categóricas (one-hot)
        for (String columna : configurador.getColumnasCategoricas()) {
            List<String> categorias = extractorCategorias.obtenerCategorias(columna);
            if (categorias != null) {
                for (String cat : categorias) {
                    nombres.add(columna + "_" + cat);
                }
            }
        }

        // 3. Columnas de conteo
        for (String col : configurador.getColumnasConteo()) {
            nombres.add(col + "_conteo");
        }

        // 4. Columnas de JSON array
        for (String col : configurador.getColumnasJsonArray()) {
            nombres.add(col + "_conteo");
        }

        // 5. Fecha normalizada
        nombres.add("release_date_normalizada");

        return nombres.toArray(new String[0]);
    }

    public int calcularTotalDimensiones() {
        int total = 0;
        total += configurador.getColumnasNumericas().length;
        total += extractorCategorias.contarDimensionesOneHot();
        total += configurador.getColumnasConteo().length;
        total += configurador.getColumnasJsonArray().length;
        total += 1; // Fecha
        return total;
    }
}

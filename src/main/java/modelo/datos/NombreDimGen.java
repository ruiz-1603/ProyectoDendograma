package modelo.datos;

import modelo.estructuras.ListaDoble;

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
        ListaDoble<String> nombres = new ListaDoble<>();

        // 1. Columnas numéricas
        for (String col : configurador.getColumnasNumericas()) {
            nombres.agregar(col);
        }

        // 2. Columnas categóricas (one-hot)
        for (String columna : configurador.getColumnasCategoricas()) {
            ListaDoble<String> categorias = extractorCategorias.obtenerCategorias(columna);
            if (categorias != null) {
                for (int i = 0; i < categorias.tamanio(); i++) {
                    String cat = categorias.obtener(i);
                    nombres.agregar(columna + "_" + cat);
                }
            }
        }

        // 3. Columnas de conteo
        for (String col : configurador.getColumnasConteo()) {
            nombres.agregar(col + "_conteo");
        }

        // 4. Columnas de JSON array
        for (String col : configurador.getColumnasJsonArray()) {
            nombres.agregar(col + "_conteo");
        }

        // 5. Fecha normalizada
        nombres.agregar("release_date_normalizada");

        String[] resultado = new String[nombres.tamanio()];
        for (int i = 0; i < nombres.tamanio(); i++) {
            resultado[i] = nombres.obtener(i);
        }
        return resultado;
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
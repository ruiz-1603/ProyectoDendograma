package modelo.datos;

import modelo.estructuras.Diccionario;
import modelo.estructuras.IDiccionario;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Responsabilidad: Extraer y mantener categorías únicas de columnas categóricas
 */
public class ExtractorCategorias {

    private IDiccionario<String, List<String>> categoriasUnicas;

    public ExtractorCategorias() {
        this.categoriasUnicas = new Diccionario<>();
    }

    public void extraer(List<IDiccionario<String, String>> filas, String[] columnasCategoricas) {
        categoriasUnicas.limpiar();

        for (String columna : columnasCategoricas) {
            Set<String> unicos = new TreeSet<>();

            for (IDiccionario<String, String> fila : filas) {
                String valor = fila.obtener(columna);
                if (valor != null && !valor.isEmpty() && !valor.equals("null")) {
                    unicos.add(valor.trim());
                }
            }

            if (unicos.isEmpty()) {
                unicos.add("desconocido");
            }

            categoriasUnicas.poner(columna, new ArrayList<>(unicos));
        }
    }

    public List<String> obtenerCategorias(String columna) {
        return categoriasUnicas.obtener(columna);
    }

    public int contarDimensionesOneHot() {
        int total = 0;
        for (String columna : categoriasUnicas.conjuntoClaves().aArreglo()) {
            List<String> categorias = categoriasUnicas.obtener((String) columna);
            if (categorias == null) {
                categorias = new ArrayList<>();
            }
            total += categorias.size();
        }
        return total;
    }

    public IDiccionario<String, List<String>> getCategoriasUnicas() {
        return categoriasUnicas;
    }
}

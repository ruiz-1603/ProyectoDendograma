package modelo.datos;

import java.util.*;

public class SelectorColumnas {

    private String[] todasLasColumnas;
    private Set<String> columnasSeleccionadas;
    private Map<String, Integer> indiceColumnas;

    /**
     * Constructor con todas las columnas disponibles
     * Complejidad: O(m)
     */
    public SelectorColumnas(String[] columnasDisponibles) {
        if (columnasDisponibles == null || columnasDisponibles.length == 0) {
            throw new IllegalArgumentException("Array de columnas no puede estar vacío");
        }

        this.todasLasColumnas = columnasDisponibles.clone();
        this.columnasSeleccionadas = new LinkedHashSet<>();
        this.indiceColumnas = new LinkedHashMap<>();

        // Por defecto, todas las columnas están seleccionadas
        for (int i = 0; i < columnasDisponibles.length; i++) {
            columnasSeleccionadas.add(columnasDisponibles[i]);
            indiceColumnas.put(columnasDisponibles[i], i);
        }
    }

    /**
     * Selecciona una columna (la incluye)
     * Complejidad: O(1)
     */
    public void seleccionar(String columna) {
        if (!indiceColumnas.containsKey(columna)) {
            throw new IllegalArgumentException("Columna no existe: " + columna);
        }
        columnasSeleccionadas.add(columna);
    }

    /**
     * Ignora una columna (la excluye)
     * Complejidad: O(1)
     */
    public void ignorar(String columna) {
        if (!indiceColumnas.containsKey(columna)) {
            throw new IllegalArgumentException("Columna no existe: " + columna);
        }
        columnasSeleccionadas.remove(columna);
    }

    /**
     * Selecciona múltiples columnas
     * Complejidad: O(m)
     */
    public void seleccionarMultiples(String[] columnas) {
        if (columnas == null) return;

        for (String col : columnas) {
            seleccionar(col);
        }
    }

    /**
     * Ignora múltiples columnas
     * Complejidad: O(m)
     */
    public void ignorarMultiples(String[] columnas) {
        if (columnas == null) return;

        for (String col : columnas) {
            ignorar(col);
        }
    }

    /**
     * Verifica si una columna está seleccionada
     * Complejidad: O(1)
     */
    public boolean estaSeleccionada(String columna) {
        return columnasSeleccionadas.contains(columna);
    }

    /**
     * Obtiene todas las columnas seleccionadas
     * Complejidad: O(m)
     */
    public String[] obtenerColumnasSeleccionadas() {
        return columnasSeleccionadas.toArray(new String[0]);
    }

    /**
     * Obtiene todas las columnas ignoradas
     * Complejidad: O(m)
     */
    public String[] obtenerColumnasIgnoradas() {
        List<String> ignoradas = new ArrayList<>();

        for (String col : todasLasColumnas) {
            if (!columnasSeleccionadas.contains(col)) {
                ignoradas.add(col);
            }
        }

        return ignoradas.toArray(new String[0]);
    }

    /**
     * Obtiene índices de columnas seleccionadas en orden
     * Complejidad: O(m)
     */
    public int[] obtenerIndicesSeleccionados() {
        List<Integer> indices = new ArrayList<>();

        for (String col : todasLasColumnas) {
            if (columnasSeleccionadas.contains(col)) {
                indices.add(indiceColumnas.get(col));
            }
        }

        int[] resultado = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            resultado[i] = indices.get(i);
        }
        return resultado;
    }

    /**
     * Selecciona todas las columnas
     * Complejidad: O(m)
     */
    public void seleccionarTodas() {
        columnasSeleccionadas.clear();
        for (String col : todasLasColumnas) {
            columnasSeleccionadas.add(col);
        }
    }

    /**
     * Ignora todas las columnas
     * Complejidad: O(1)
     */
    public void ignorarTodas() {
        columnasSeleccionadas.clear();
    }

    /**
     * Obtiene número de columnas seleccionadas
     * Complejidad: O(1)
     */
    public int obtenerNumeroSeleccionadas() {
        return columnasSeleccionadas.size();
    }

    /**
     * Obtiene número de columnas ignoradas
     * Complejidad: O(1)
     */
    public int obtenerNumeroIgnoradas() {
        return todasLasColumnas.length - columnasSeleccionadas.size();
    }

    /**
     * Obtiene número total de columnas disponibles
     * Complejidad: O(1)
     */
    public int obtenerNumeroTotal() {
        return todasLasColumnas.length;
    }

    /**
     * Obtiene todas las columnas disponibles
     * Complejidad: O(m)
     */
    public String[] obtenerTodasLasColumnas() {
        return todasLasColumnas.clone();
    }

    /**
     * Valida que haya al menos una columna seleccionada
     * Complejidad: O(1)
     */
    public boolean esValido() {
        return columnasSeleccionadas.size() > 0;
    }

    /**
     * Imprime el estado del selector
     * Complejidad: O(m)
     */
    public void imprimir() {
        System.out.println("=== Selector de Columnas ===");
        System.out.println("Total de columnas: " + todasLasColumnas.length);
        System.out.println("Seleccionadas: " + columnasSeleccionadas.size());
        System.out.println("Ignoradas: " + obtenerNumeroIgnoradas());
        System.out.println();

        System.out.println("COLUMNAS SELECCIONADAS:");
        for (String col : columnasSeleccionadas) {
            System.out.println("  ✓ " + col);
        }

        String[] ignoradas = obtenerColumnasIgnoradas();
        if (ignoradas.length > 0) {
            System.out.println();
            System.out.println("COLUMNAS IGNORADAS:");
            for (String col : ignoradas) {
                System.out.println("  ✗ " + col);
            }
        }
    }

    @Override
    public String toString() {
        return "SelectorColumnas [total=" + todasLasColumnas.length +
                ", seleccionadas=" + columnasSeleccionadas.size() +
                ", ignoradas=" + obtenerNumeroIgnoradas() + "]";
    }
}

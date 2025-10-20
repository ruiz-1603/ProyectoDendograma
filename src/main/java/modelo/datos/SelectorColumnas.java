package modelo.datos;

import modelo.estructuras.Vector;

import java.util.*;

public class SelectorColumnas {

    private String[] todasLasColumnas;
    private Set<String> columnasSeleccionadas;
    private Map<String, Integer> indiceColumnas;

    // todas las columnas
    public SelectorColumnas(String[] columnasDisponibles) {
        if (columnasDisponibles == null || columnasDisponibles.length == 0) {
            throw new IllegalArgumentException("Array de columnas no puede estar vacío");
        }

        this.todasLasColumnas = columnasDisponibles.clone();
        this.columnasSeleccionadas = new LinkedHashSet<>();
        this.indiceColumnas = new LinkedHashMap<>();

        // selecciona todas por defecto
        for (int i = 0; i < columnasDisponibles.length; i++) {
            columnasSeleccionadas.add(columnasDisponibles[i]);
            indiceColumnas.put(columnasDisponibles[i], i);
        }
    }

    public Vector[] aplicarSeleccion(Vector[] vectoresOriginales) {
        if (vectoresOriginales == null || vectoresOriginales.length == 0) {
            return new Vector[0];
        }

        int[] indices = getIndicesSeleccionados();
        if (indices.length == 0) {
            return new Vector[0];
        }

        Vector[] vectoresFiltrados = new Vector[vectoresOriginales.length];

        for (int i = 0; i < vectoresOriginales.length; i++) {
            double[] datosOriginales = vectoresOriginales[i].getDatos();
            double[] datosFiltrados = new double[indices.length];
            for (int j = 0; j < indices.length; j++) {
                datosFiltrados[j] = datosOriginales[indices[j]];
            }
            vectoresFiltrados[i] = new Vector(datosFiltrados, vectoresOriginales[i].getEtiqueta());
        }

        return vectoresFiltrados;
    }

    // solo una y la incluye
    public void seleccionar(String columna) {
        if (!indiceColumnas.containsKey(columna)) {
            throw new IllegalArgumentException("Columna no existe: " + columna);
        }
        columnasSeleccionadas.add(columna);
    }

    // excluye la columna
    public void ignorar(String columna) {
        if (!indiceColumnas.containsKey(columna)) {
            throw new IllegalArgumentException("Columna no existe: " + columna);
        }
        columnasSeleccionadas.remove(columna);
    }

    public void seleccionarMultiples(String[] columnas) {
        if (columnas == null) return;

        for (String col : columnas) {
            seleccionar(col);
        }
    }

    public void ignorarMultiples(String[] columnas) {
        if (columnas == null) return;

        for (String col : columnas) {
            ignorar(col);
        }
    }

    public boolean estaSeleccionada(String columna) {
        return columnasSeleccionadas.contains(columna);
    }

    public String[] getColumnasSeleccionadas() {
        return columnasSeleccionadas.toArray(new String[0]);
    }

    public String[] getColumnasIgnoradas() {
        List<String> ignoradas = new ArrayList<>();

        for (String col : todasLasColumnas) {
            if (!columnasSeleccionadas.contains(col)) {
                ignoradas.add(col);
            }
        }

        return ignoradas.toArray(new String[0]);
    }

    // en orden
    public int[] getIndicesSeleccionados() {
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

    public void seleccionarTodas() {
        columnasSeleccionadas.clear();
        for (String col : todasLasColumnas) {
            columnasSeleccionadas.add(col);
        }
    }

    public void ignorarTodas() {
        columnasSeleccionadas.clear();
    }

    public int getNumeroSeleccionadas() {
        return columnasSeleccionadas.size();
    }

    public int getNumeroIgnoradas() {
        return todasLasColumnas.length - columnasSeleccionadas.size();
    }

    public int getNumeroTotal() {
        return todasLasColumnas.length;
    }

    public String[] getTodasLasColumnas() {
        return todasLasColumnas.clone();
    }

    // al menos una seleccionada
    public boolean esValido() {
        return columnasSeleccionadas.size() > 0;
    }

    public void imprimir() {
        System.out.println("=== Selector de Columnas ===");
        System.out.println("Total de columnas: " + todasLasColumnas.length);
        System.out.println("Seleccionadas: " + columnasSeleccionadas.size());
        System.out.println("Ignoradas: " + getNumeroIgnoradas());
        System.out.println();

        System.out.println("COLUMNAS SELECCIONADAS:");
        for (String col : columnasSeleccionadas) {
            System.out.println("  ✓ " + col);
        }

        String[] ignoradas = getColumnasIgnoradas();
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
                ", ignoradas=" + getNumeroIgnoradas() + "]";
    }
}

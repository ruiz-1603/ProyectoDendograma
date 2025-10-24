package modelo.datos;

import modelo.estructuras.Diccionario;
import modelo.estructuras.IDiccionario;
import modelo.estructuras.ListaDoble;
import modelo.estructuras.Vector;

public class SelectorColumnas {

    private String[] todasLasColumnas;
    private IDiccionario<String, Boolean> columnasSeleccionadas;
    private IDiccionario<String, Integer> indiceColumnas;

    // todas las columnas
    public SelectorColumnas(String[] columnasDisponibles) {
        if (columnasDisponibles == null || columnasDisponibles.length == 0) {
            throw new IllegalArgumentException("Array de columnas no puede estar vacío");
        }

        this.todasLasColumnas = columnasDisponibles.clone();
        this.columnasSeleccionadas = new Diccionario<>();
        this.indiceColumnas = new Diccionario<>();

        // selecciona todas por defecto
        for (int i = 0; i < columnasDisponibles.length; i++) {
            columnasSeleccionadas.poner(columnasDisponibles[i], true);
            indiceColumnas.poner(columnasDisponibles[i], i);
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
        if (!indiceColumnas.contieneClave(columna)) {
            throw new IllegalArgumentException("Columna no existe: " + columna);
        }
        columnasSeleccionadas.poner(columna, true);
    }

    // excluye la columna
    public void ignorar(String columna) {
        if (!indiceColumnas.contieneClave(columna)) {
            throw new IllegalArgumentException("Columna no existe: " + columna);
        }
        columnasSeleccionadas.eliminar(columna);
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
        return columnasSeleccionadas.contieneClave(columna);
    }

    public String[] getColumnasSeleccionadas() {
        ListaDoble<String> claves = columnasSeleccionadas.conjuntoClaves();
        String[] resultado = new String[claves.tamanio()];
        for (int i = 0; i < claves.tamanio(); i++) {
            resultado[i] = claves.obtener(i);
        }
        return resultado;
    }

    public String[] getColumnasIgnoradas() {
        ListaDoble<String> ignoradas = new ListaDoble<>();

        for (String col : todasLasColumnas) {
            if (!columnasSeleccionadas.contieneClave(col)) {
                ignoradas.agregar(col);
            }
        }

        String[] resultado = new String[ignoradas.tamanio()];
        for (int i = 0; i < ignoradas.tamanio(); i++) {
            resultado[i] = ignoradas.obtener(i);
        }
        return resultado;
    }

    // en orden
    public int[] getIndicesSeleccionados() {
        ListaDoble<Integer> indices = new ListaDoble<>();

        for (String col : todasLasColumnas) {
            if (columnasSeleccionadas.contieneClave(col)) {
                indices.agregar(indiceColumnas.obtener(col));
            }
        }

        int[] resultado = new int[indices.tamanio()];
        for (int i = 0; i < indices.tamanio(); i++) {
            resultado[i] = indices.obtener(i);
        }
        return resultado;
    }

    public void seleccionarTodas() {
        columnasSeleccionadas.limpiar();
        for (String col : todasLasColumnas) {
            columnasSeleccionadas.poner(col, true);
        }
    }

    public void ignorarTodas() {
        columnasSeleccionadas.limpiar();
    }

    public int getNumeroSeleccionadas() {
        return columnasSeleccionadas.tamanio();
    }

    public int getNumeroIgnoradas() {
        return todasLasColumnas.length - columnasSeleccionadas.tamanio();
    }

    public int getNumeroTotal() {
        return todasLasColumnas.length;
    }

    public String[] getTodasLasColumnas() {
        return todasLasColumnas.clone();
    }

    // al menos una seleccionada
    public boolean esValido() {
        return columnasSeleccionadas.tamanio() > 0;
    }

    public void imprimir() {
        System.out.println("=== Selector de Columnas ===");
        System.out.println("Total de columnas: " + todasLasColumnas.length);
        System.out.println("Seleccionadas: " + columnasSeleccionadas.tamanio());
        System.out.println("Ignoradas: " + getNumeroIgnoradas());
        System.out.println();

        System.out.println("COLUMNAS SELECCIONADAS:");
        ListaDoble<String> clavesSeleccionadas = columnasSeleccionadas.conjuntoClaves();
        for (int i = 0; i < clavesSeleccionadas.tamanio(); i++) {
            System.out.println("  ✓ " + clavesSeleccionadas.obtener(i));
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
                ", seleccionadas=" + columnasSeleccionadas.tamanio() +
                ", ignoradas=" + getNumeroIgnoradas() + "]";
    }
}
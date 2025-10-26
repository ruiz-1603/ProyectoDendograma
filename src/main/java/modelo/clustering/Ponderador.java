package modelo.clustering;

import modelo.estructuras.Vector;
import modelo.datos.SelectorColumnas;

public class Ponderador {

    private double[] pesos;
    private String[] nombresDimensiones;

    // con pesos
    public Ponderador(double[] pesos) {
        if (pesos == null || pesos.length == 0) {
            throw new IllegalArgumentException("Array de pesos no puede estar vacío");
        }

        // validar que todos los pesos sean no-negativos
        for (double peso : pesos) {
            if (peso < 0) {
                throw new IllegalArgumentException("Los pesos no pueden ser negativos");
            }
        }

        this.pesos = pesos.clone();
        this.nombresDimensiones = new String[pesos.length];

        for (int i = 0; i < pesos.length; i++) {
            this.nombresDimensiones[i] = "Dimensión_" + i;
        }
    }

    // con pesos y nombres
    public Ponderador(double[] pesos, String[] nombresDimensiones) {
        this(pesos);

        if (nombresDimensiones != null && nombresDimensiones.length == pesos.length) {
            this.nombresDimensiones = nombresDimensiones.clone();
        }
    }

    public Ponderador filtrarPesos(SelectorColumnas selector) {
        int[] indicesSeleccionados = selector.getIndicesSeleccionados();
        double[] nuevosPesos = new double[indicesSeleccionados.length];
        String[] nuevosNombres = new String[indicesSeleccionados.length];

        for (int i = 0; i < indicesSeleccionados.length; i++) {
            int indiceOriginal = indicesSeleccionados[i];
            nuevosPesos[i] = this.pesos[indiceOriginal];
            nuevosNombres[i] = this.nombresDimensiones[indiceOriginal];
        }

        return new Ponderador(nuevosPesos, nuevosNombres);
    }

    public Vector[] aplicarPesos(Vector[] vectores) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        Vector[] resultado = new Vector[vectores.length];

        for (int v = 0; v < vectores.length; v++) {
            Vector original = vectores[v];

            if (original.dimension() != pesos.length) {
                throw new IllegalArgumentException(
                        "Dimensión del vector (" + original.dimension() +
                                ") no coincide con número de pesos (" + pesos.length + ")"
                );
            }

            double[] datosPonderados = new double[pesos.length];

            for (int i = 0; i < pesos.length; i++) {
                datosPonderados[i] = pesos[i] * original.getPosicion(i);
            }

            resultado[v] = new Vector(datosPonderados, original.getEtiqueta());
        }

        return resultado;
    }

    public double[] getPesos() {
        return pesos.clone();
    }

    public String[] getNombresDimensiones() {
        return nombresDimensiones.clone();
    }

    public boolean tienePonderacion() {
        for (double peso : pesos) {
            if (Math.abs(peso - 1.0) > 1e-9) {
                return true;
            }
        }
        return false;
    }

    public void imprimir() {
        System.out.println("=== Ponderador ===");
        System.out.println("Dimensiones: " + pesos.length);
        System.out.println("Ponderación activa: " + tienePonderacion());
        System.out.println();

        for (int i = 0; i < pesos.length; i++) {
            System.out.printf("%3d. %-30s → peso: %.4f%n",
                    i, nombresDimensiones[i], pesos[i]);
        }

        double suma = 0;
        for (double p : pesos) suma += p;
        System.out.printf("%nSuma de pesos: %.4f%n", suma);
    }

    @Override
    public String toString() {
        return "Ponderador [dimensiones=" + pesos.length +
                ", ponderacionActiva=" + tienePonderacion() + "]";
    }
}

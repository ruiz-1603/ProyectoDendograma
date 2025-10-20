package modelo.clustering;

import modelo.estructuras.Vector;

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

    // sin ponderacion, todos = 1.0
    public static Ponderador crearSinPesos(int dimension) {
        double[] pesos = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            pesos[i] = 1.0;
        }
        return new Ponderador(pesos);
    }

    public Vector[] aplicarPesos(Vector[] vectores) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        Vector[] resultado = new Vector[vectores.length];

        for (int v = 0; v < vectores.length; v++) {
            Vector original = vectores[v];

            // validar dimension
            if (original.dimension() != pesos.length) {
                throw new IllegalArgumentException(
                        "Dimensión del vector (" + original.dimension() +
                                ") no coincide con número de pesos (" + pesos.length + ")"
                );
            }

            // nuevo vector ponderado
            double[] datosPonderados = new double[pesos.length];

            for (int i = 0; i < pesos.length; i++) {
                datosPonderados[i] = pesos[i] * original.getPosicion(i);
            }

            resultado[v] = new Vector(datosPonderados, original.getEtiqueta());
        }

        return resultado;
    }

    // aplicar peso a un solo vector
    // ver si se usa, si no eliminar
    public Vector aplicarPesos(Vector vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector no puede ser null");
        }

        if (vector.dimension() != pesos.length) {
            throw new IllegalArgumentException(
                    "Dimensión del vector (" + vector.dimension() +
                            ") no coincide con número de pesos (" + pesos.length + ")"
            );
        }

        double[] datosPonderados = new double[pesos.length];

        for (int i = 0; i < pesos.length; i++) {
            datosPonderados[i] = pesos[i] * vector.getPosicion(i);
        }

        return new Vector(datosPonderados, vector.getEtiqueta());
    }

    // aplicar peso especifico
    public void establecerPeso(int indice, double peso) {
        if (peso < 0) {
            throw new IllegalArgumentException("El peso no puede ser negativo");
        }

        if (indice >= 0 && indice < pesos.length) {
            pesos[indice] = peso;
        } else {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
    }

    public double getPeso(int indice) {
        if (indice >= 0 && indice < pesos.length) {
            return pesos[indice];
        }
        throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
    }

    public void setNombreDimension(int indice, String nombre) {
        if (indice >= 0 && indice < nombresDimensiones.length) {
            nombresDimensiones[indice] = nombre;
        }
    }

    public double[] getPesos() {
        return pesos.clone();
    }

    public String[] getNombresDimensiones() {
        return nombresDimensiones.clone();
    }

    public int getDimension() {
        return pesos.length;
    }

    public boolean tienePonderacion() {
        for (double peso : pesos) {
            if (Math.abs(peso - 1.0) > 1e-9) {
                return true;
            }
        }
        return false;
    }

    // normaliza pesos para que sumen 1.0
    public void normalizarPesos() {
        double suma = 0.0;
        for (double peso : pesos) {
            suma += peso;
        }

        if (suma > 0) {
            for (int i = 0; i < pesos.length; i++) {
                pesos[i] /= suma;
            }
        }
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

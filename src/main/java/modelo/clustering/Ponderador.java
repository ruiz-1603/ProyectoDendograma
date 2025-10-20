package modelo.clustering;

import modelo.estructuras.Vector;

public class Ponderador {

    private double[] pesos;
    private String[] nombresDimensiones;

    /**
     * Constructor con array de pesos
     * Complejidad: O(m)
     */
    public Ponderador(double[] pesos) {
        if (pesos == null || pesos.length == 0) {
            throw new IllegalArgumentException("Array de pesos no puede estar vacío");
        }

        // Validar que todos los pesos sean no-negativos
        for (double peso : pesos) {
            if (peso < 0) {
                throw new IllegalArgumentException("Los pesos no pueden ser negativos");
            }
        }

        this.pesos = pesos.clone();
        this.nombresDimensiones = new String[pesos.length];

        // Nombres por defecto
        for (int i = 0; i < pesos.length; i++) {
            this.nombresDimensiones[i] = "Dimensión_" + i;
        }
    }

    /**
     * Constructor con pesos y nombres de dimensiones
     * Complejidad: O(m)
     */
    public Ponderador(double[] pesos, String[] nombresDimensiones) {
        this(pesos);

        if (nombresDimensiones != null && nombresDimensiones.length == pesos.length) {
            this.nombresDimensiones = nombresDimensiones.clone();
        }
    }

    /**
     * Crea un ponderador con todos los pesos iguales a 1.0 (sin ponderación)
     * Complejidad: O(m)
     */
    public static Ponderador crearSinPesos(int dimension) {
        double[] pesos = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            pesos[i] = 1.0;
        }
        return new Ponderador(pesos);
    }

    /**
     * Aplica pesos a un conjunto de vectores
     * Cada componente se multiplica por su peso correspondiente
     *
     * Fórmula: v'_i = w_i * v_i
     *
     * Complejidad: O(n*m)
     */
    public Vector[] aplicarPesos(Vector[] vectores) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        Vector[] resultado = new Vector[vectores.length];

        for (int v = 0; v < vectores.length; v++) {
            Vector original = vectores[v];

            // Validar dimensión
            if (original.dimension() != pesos.length) {
                throw new IllegalArgumentException(
                        "Dimensión del vector (" + original.dimension() +
                                ") no coincide con número de pesos (" + pesos.length + ")"
                );
            }

            // Crear nuevo vector ponderado
            double[] datosPonderados = new double[pesos.length];

            for (int i = 0; i < pesos.length; i++) {
                datosPonderados[i] = pesos[i] * original.getPosicion(i);
            }

            resultado[v] = new Vector(datosPonderados, original.getEtiqueta());
        }

        return resultado;
    }

    /**
     * Aplica pesos a un único vector
     * Complejidad: O(m)
     */
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

    /**
     * Establece el peso de una dimensión específica
     * Complejidad: O(1)
     */
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

    /**
     * Obtiene el peso de una dimensión
     * Complejidad: O(1)
     */
    public double obtenerPeso(int indice) {
        if (indice >= 0 && indice < pesos.length) {
            return pesos[indice];
        }
        throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
    }

    /**
     * Establece el nombre de una dimensión
     * Complejidad: O(1)
     */
    public void establecerNombreDimension(int indice, String nombre) {
        if (indice >= 0 && indice < nombresDimensiones.length) {
            nombresDimensiones[indice] = nombre;
        }
    }

    /**
     * Obtiene todos los pesos
     * Complejidad: O(m)
     */
    public double[] obtenerPesos() {
        return pesos.clone();
    }

    /**
     * Obtiene todos los nombres de dimensiones
     * Complejidad: O(m)
     */
    public String[] obtenerNombresDimensiones() {
        return nombresDimensiones.clone();
    }

    /**
     * Obtiene el número de dimensiones
     * Complejidad: O(1)
     */
    public int obtenerDimension() {
        return pesos.length;
    }

    /**
     * Verifica si hay ponderación activa (algún peso != 1.0)
     * Complejidad: O(m)
     */
    public boolean tienePonderacion() {
        for (double peso : pesos) {
            if (Math.abs(peso - 1.0) > 1e-9) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normaliza los pesos para que sumen 1.0
     * Complejidad: O(m)
     */
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

    /**
     * Imprime información del ponderador
     * Complejidad: O(m)
     */
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

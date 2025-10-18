package modelo.estructuras;

public class Matriz {
    private double[][] datos;
    private int dimension;

    /**
     * Constructor de matriz cuadrada
     * Complejidad: O(n²)
     */
    public Matriz(int dimension) {
        this.dimension = dimension;
        this.datos = new double[dimension][dimension];

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                datos[i][j] = 0.0;
            }
        }
    }

    /**
     * Constructor con datos existentes
     * Complejidad: O(n²)
     */
    public Matriz(double[][] datos) {
        this.dimension = datos.length;
        this.datos = new double[dimension][dimension];

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                this.datos[i][j] = datos[i][j];
            }
        }
    }

    /**
     * Establece un valor en la posición [i][j]
     * Complejidad: O(1)
     */
    public void setPosicion(int i, int j, double valor) {
        if (i >= 0 && i < dimension && j >= 0 && j < dimension) {
            datos[i][j] = valor;
        }
    }

    /**
     * Obtiene el valor en la posición [i][j]
     * Complejidad: O(1)
     */
    public double getPosicion(int i, int j) {
        if (i >= 0 && i < dimension && j >= 0 && j < dimension) {
            return datos[i][j];
        }
        return 0.0;
    }

    /**
     * Retorna la dimensión de la matriz
     * Complejidad: O(1)
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Encuentra el par de elementos con distancia mínima
     * Usado en clustering jerárquico
     * Complejidad: O(n²)
     */
    public int[] encontrarMin(boolean[] activos) {
        double minimo = Double.MAX_VALUE;
        int[] resultado = new int[]{-1, -1};

        for (int i = 0; i < dimension; i++) {
            if (!activos[i]) continue;

            for (int j = i + 1; j < dimension; j++) {
                if (!activos[j]) continue;

                if (datos[i][j] < minimo) {
                    minimo = datos[i][j];
                    resultado[0] = i;
                    resultado[1] = j;
                }
            }
        }

        return resultado;
    }

    /**
     * Encuentra el par de elementos con distancia máxima
     * Complejidad: O(n²)
     */
    public int[] encontrarMax(boolean[] activos) {
        double maximo = Double.MIN_VALUE;
        int[] resultado = new int[]{-1, -1};

        for (int i = 0; i < dimension; i++) {
            if (!activos[i]) continue;

            for (int j = i + 1; j < dimension; j++) {
                if (!activos[j]) continue;

                if (datos[i][j] > maximo) {
                    maximo = datos[i][j];
                    resultado[0] = i;
                    resultado[1] = j;
                }
            }
        }

        return resultado;
    }

    /**
     * Obtiene una fila completa
     * Complejidad: O(n)
     */
    public double[] getFila(int i) {
        if (i >= 0 && i < dimension) {
            return datos[i].clone();
        }
        return new double[0];
    }

    /**
     * Obtiene una columna completa
     * Complejidad: O(n)
     */
    public double[] getColumna(int j) {
        if (j >= 0 && j < dimension) {
            double[] columna = new double[dimension];
            for (int i = 0; i < dimension; i++) {
                columna[i] = datos[i][j];
            }
            return columna;
        }
        return new double[0];
    }

    /**
     * Verifica si la matriz es simétrica
     * Complejidad: O(n²)
     */
    public boolean esSimetrica() {
        for (int i = 0; i < dimension; i++) {
            for (int j = i + 1; j < dimension; j++) {
                if (Math.abs(datos[i][j] - datos[j][i]) > 1e-9) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Copia la matriz
     * Complejidad: O(n²)
     */
    public Matriz clonar() {
        return new Matriz(this.datos);
    }

    /**
     * Imprime la matriz en consola
     * Útil para debugging
     * Complejidad: O(n²)
     */
    public void imprimir() {
        System.out.println("Matriz " + dimension + "x" + dimension + ":");
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                System.out.printf("%8.2f ", datos[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Imprime solo la parte triangular superior
     * Útil para matrices simétricas de distancias
     * Complejidad: O(n²)
     */
    public void imprimirTriangularSuperior() {
        System.out.println("Matriz triangular superior:");
        for (int i = 0; i < dimension; i++) {
            // Espacios para alinear
            for (int k = 0; k < i; k++) {
                System.out.print("         ");
            }
            // Valores de la diagonal y superiores
            for (int j = i; j < dimension; j++) {
                System.out.printf("%8.2f ", datos[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Retorna una copia de todos los datos
     * Complejidad: O(n²)
     */
    public double[][] getDatos() {
        double[][] copia = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                copia[i][j] = datos[i][j];
            }
        }
        return copia;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matriz ").append(dimension).append("x").append(dimension).append("\n");
        for (int i = 0; i < Math.min(5, dimension); i++) {
            for (int j = 0; j < Math.min(5, dimension); j++) {
                sb.append(String.format("%8.2f ", datos[i][j]));
            }
            if (dimension > 5) sb.append("...");
            sb.append("\n");
        }
        if (dimension > 5) sb.append("...\n");
        return sb.toString();
    }
}
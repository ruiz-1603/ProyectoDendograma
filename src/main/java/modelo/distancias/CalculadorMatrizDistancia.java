package modelo.distancias;

import modelo.estructuras.Vector;
import modelo.estructuras.Matriz;

public class CalculadorMatrizDistancia {

    private Vector[] vectores;
    private Matriz matrizDistancias;
    private ICalculadorDistancia calculador;
    private String[] etiquetas;

    /**
     * Constructor
     * Complejidad: O(1)
     */
    public CalculadorMatrizDistancia() {
        this.vectores = new Vector[0];
        this.matrizDistancias = null;
        this.calculador = null;
        this.etiquetas = new String[0];
    }

    /**
     * Calcula la matriz de distancias
     * Complejidad: O(n²*m)
     *
     * @param vectores Array de vectores
     * @param tipoDistancia Tipo de distancia a usar
     * @return Matriz de distancias (simétrica)
     */
    public Matriz calcular(Vector[] vectores, FactoryDistancia.TipoDistancia tipoDistancia) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        this.vectores = vectores;
        this.calculador = FactoryDistancia.crear(tipoDistancia);
        this.etiquetas = extraerEtiquetas(vectores);

        // Crear matriz cuadrada n x n
        int n = vectores.length;
        this.matrizDistancias = new Matriz(n);

        // Calcular distancias
        for (int i = 0; i < n; i++) {
            // Diagonal: distancia de un elemento consigo mismo = 0
            matrizDistancias.setPosicion(i, i, 0.0);

            // Parte superior de la matriz
            for (int j = i + 1; j < n; j++) {
                double distancia = calculador.calcular(vectores[i], vectores[j]);

                // Matriz simétrica
                matrizDistancias.setPosicion(i, j, distancia);
                matrizDistancias.setPosicion(j, i, distancia);
            }
        }

        return this.matrizDistancias;
    }

    /**
     * Calcula la matriz usando un nombre de distancia en string
     * Complejidad: O(n²*m)
     */
    public Matriz calcular(Vector[] vectores, String nombreDistancia) {
        FactoryDistancia.TipoDistancia tipo = FactoryDistancia.TipoDistancia.valueOf(
                nombreDistancia.toUpperCase()
        );
        return calcular(vectores, tipo);
    }

    /**
     * Extrae etiquetas de los vectores
     * Complejidad: O(n)
     */
    private String[] extraerEtiquetas(Vector[] vectores) {
        String[] labels = new String[vectores.length];
        for (int i = 0; i < vectores.length; i++) {
            labels[i] = vectores[i].getEtiqueta();
        }
        return labels;
    }

    /**
     * Obtiene la matriz de distancias
     * Complejidad: O(1)
     */
    public Matriz obtenerMatriz() {
        if (matrizDistancias == null) {
            throw new IllegalStateException("No hay matriz calculada. Llama a calcular() primero");
        }
        return matrizDistancias;
    }

    /**
     * Obtiene la distancia entre dos vectores por índice
     * Complejidad: O(1)
     */
    public double obtenerDistancia(int i, int j) {
        if (matrizDistancias == null) {
            throw new IllegalStateException("No hay matriz calculada");
        }
        return matrizDistancias.getPosicion(i, j);
    }

    /**
     * Obtiene la distancia entre dos vectores por etiqueta
     * Complejidad: O(n)
     */
    public double obtenerDistancia(String etiqueta1, String etiqueta2) {
        int i = obtenerIndiceEtiqueta(etiqueta1);
        int j = obtenerIndiceEtiqueta(etiqueta2);

        if (i == -1 || j == -1) {
            throw new IllegalArgumentException("Etiqueta no encontrada");
        }

        return obtenerDistancia(i, j);
    }

    /**
     * Encuentra el índice de una etiqueta
     * Complejidad: O(n)
     */
    private int obtenerIndiceEtiqueta(String etiqueta) {
        for (int i = 0; i < etiquetas.length; i++) {
            if (etiquetas[i].equals(etiqueta)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Obtiene el par con distancia mínima (más cercanos)
     * Complejidad: O(n²)
     */
    public int[] obtenerParMasProximo() {
        if (matrizDistancias == null) {
            throw new IllegalStateException("No hay matriz calculada");
        }

        double minimo = Double.MAX_VALUE;
        int[] resultado = new int[]{-1, -1};

        for (int i = 0; i < matrizDistancias.getDimension(); i++) {
            for (int j = i + 1; j < matrizDistancias.getDimension(); j++) {
                double distancia = matrizDistancias.getPosicion(i, j);
                if (distancia < minimo) {
                    minimo = distancia;
                    resultado[0] = i;
                    resultado[1] = j;
                }
            }
        }

        return resultado;
    }

    /**
     * Obtiene el par con distancia máxima (más lejanos)
     * Complejidad: O(n²)
     */
    public int[] obtenerParMasLejano() {
        if (matrizDistancias == null) {
            throw new IllegalStateException("No hay matriz calculada");
        }

        double maximo = Double.MIN_VALUE;
        int[] resultado = new int[]{-1, -1};

        for (int i = 0; i < matrizDistancias.getDimension(); i++) {
            for (int j = i + 1; j < matrizDistancias.getDimension(); j++) {
                double distancia = matrizDistancias.getPosicion(i, j);
                if (distancia > maximo) {
                    maximo = distancia;
                    resultado[0] = i;
                    resultado[1] = j;
                }
            }
        }

        return resultado;
    }

    /**
     * Obtiene estadísticas de la matriz
     * Complejidad: O(n²)
     */
    public EstadisticasDistancia obtenerEstadisticas() {
        if (matrizDistancias == null) {
            throw new IllegalStateException("No hay matriz calculada");
        }

        double minimo = Double.MAX_VALUE;
        double maximo = Double.MIN_VALUE;
        double suma = 0.0;
        int contador = 0;

        for (int i = 0; i < matrizDistancias.getDimension(); i++) {
            for (int j = i + 1; j < matrizDistancias.getDimension(); j++) {
                double distancia = matrizDistancias.getPosicion(i, j);
                minimo = Math.min(minimo, distancia);
                maximo = Math.max(maximo, distancia);
                suma += distancia;
                contador++;
            }
        }

        double promedio = suma / contador;

        // Calcular desviación estándar
        double sumaCuadrados = 0.0;
        for (int i = 0; i < matrizDistancias.getDimension(); i++) {
            for (int j = i + 1; j < matrizDistancias.getDimension(); j++) {
                double distancia = matrizDistancias.getPosicion(i, j);
                double diferencia = distancia - promedio;
                sumaCuadrados += diferencia * diferencia;
            }
        }
        double desviacionEstandar = Math.sqrt(sumaCuadrados / contador);

        return new EstadisticasDistancia(minimo, maximo, promedio, desviacionEstandar);
    }

    /**
     * Obtiene etiqueta en posición i
     * Complejidad: O(1)
     */
    public String obtenerEtiqueta(int i) {
        if (i >= 0 && i < etiquetas.length) {
            return etiquetas[i];
        }
        return "";
    }

    /**
     * Obtiene todas las etiquetas
     * Complejidad: O(n)
     */
    public String[] obtenerEtiquetas() {
        return etiquetas.clone();
    }

    /**
     * Obtiene número de vectores
     * Complejidad: O(1)
     */
    public int obtenerNumeroVectores() {
        return vectores.length;
    }

    /**
     * Imprime la matriz de distancias
     * Complejidad: O(n²)
     */
    public void imprimir() {
        if (matrizDistancias == null) {
            System.out.println("No hay matriz calculada");
            return;
        }

        System.out.println("=== Matriz de Distancias ===");
        System.out.println("Tipo de distancia: " + calculador.getNombre());
        System.out.println("Dimensión: " + matrizDistancias.getDimension());
        System.out.println();

        // Encabezado
        System.out.print("       ");
        for (int i = 0; i < Math.min(etiquetas.length, 10); i++) {
            System.out.printf("%10s ", etiquetas[i].substring(0, Math.min(8, etiquetas[i].length())));
        }
        System.out.println();

        // Datos
        for (int i = 0; i < Math.min(matrizDistancias.getDimension(), 10); i++) {
            System.out.printf("%7s ", etiquetas[i].substring(0, Math.min(6, etiquetas[i].length())));
            for (int j = 0; j < Math.min(matrizDistancias.getDimension(), 10); j++) {
                System.out.printf("%10.4f ", matrizDistancias.getPosicion(i, j));
            }
            System.out.println();
        }

        if (matrizDistancias.getDimension() > 10) {
            System.out.println("... (matriz truncada para visualización)");
        }
    }

    /**
     * Imprime estadísticas
     * Complejidad: O(n²)
     */
    public void imprimirEstadisticas() {
        if (matrizDistancias == null) {
            System.out.println("No hay matriz calculada");
            return;
        }

        EstadisticasDistancia stats = obtenerEstadisticas();
        System.out.println("=== Estadísticas de Distancias ===");
        System.out.println("Mínimo:            " + String.format("%.6f", stats.getMinimo()));
        System.out.println("Máximo:            " + String.format("%.6f", stats.getMaximo()));
        System.out.println("Promedio:          " + String.format("%.6f", stats.getPromedio()));
        System.out.println("Desv. Estándar:    " + String.format("%.6f", stats.getDesviacionEstandar()));
    }

    /**
     * Clase interna para almacenar estadísticas
     */
    public static class EstadisticasDistancia {
        private double minimo;
        private double maximo;
        private double promedio;
        private double desviacionEstandar;

        public EstadisticasDistancia(double minimo, double maximo, double promedio, double desviacionEstandar) {
            this.minimo = minimo;
            this.maximo = maximo;
            this.promedio = promedio;
            this.desviacionEstandar = desviacionEstandar;
        }

        public double getMinimo() { return minimo; }
        public double getMaximo() { return maximo; }
        public double getPromedio() { return promedio; }
        public double getDesviacionEstandar() { return desviacionEstandar; }
    }

    @Override
    public String toString() {
        if (matrizDistancias == null) {
            return "CalculadorMatrizDistancias [sin matriz calculada]";
        }
        return "CalculadorMatrizDistancias [" + matrizDistancias.getDimension() + "x" +
                matrizDistancias.getDimension() + "] - " + calculador.getNombre();
    }
}

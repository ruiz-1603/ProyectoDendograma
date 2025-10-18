package modelo.clustering;

import modelo.estructuras.Vector;
import modelo.estructuras.Matriz;
import modelo.estructuras.Nodo;
import modelo.distancias.CalculadorMatrizDistancia;
import modelo.distancias.FactoryDistancia;

import java.util.*;

public class MotorCluster {

    public enum TipoEnlace {
        MINIMO,      // Distancia mínima entre clusters
        MAXIMO,      // Distancia máxima entre clusters
        PROMEDIO,    // Distancia promedio entre clusters
        CENTROIDE    // Distancia entre centroides
    }

    private Vector[] vectores;
    private Matriz matrizDistancias;
    private List<Nodo> clusters;
    private List<Double> distanciasFusion;
    private TipoEnlace tipoEnlace;
    private CalculadorMatrizDistancia calculador;

    /**
     * Constructor con tipo de enlace por defecto (PROMEDIO)
     * Complejidad: O(1)
     */
    public MotorCluster() {
        this.tipoEnlace = TipoEnlace.PROMEDIO;
        this.clusters = new ArrayList<>();
        this.distanciasFusion = new ArrayList<>();
        this.calculador = new CalculadorMatrizDistancia();
    }

    /**
     * Constructor con tipo de enlace especificado
     * Complejidad: O(1)
     */
    public MotorCluster(TipoEnlace tipoEnlace) {
        this.tipoEnlace = tipoEnlace;
        this.clusters = new ArrayList<>();
        this.distanciasFusion = new ArrayList<>();
        this.calculador = new CalculadorMatrizDistancia();
    }

    /**
     * Ejecuta el algoritmo de clustering jerárquico
     * Complejidad: O(n³) en el peor caso
     *
     * @param vectores      Array de vectores normalizados
     * @param tipoDistancia Métrica de distancia a usar
     * @return Nodo raíz del dendrograma
     */
    public Nodo construirDendrograma(Vector[] vectores, FactoryDistancia.TipoDistancia tipoDistancia) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        this.vectores = vectores;
        this.distanciasFusion.clear();

        // Paso 1: Crear matriz de distancias inicial
        matrizDistancias = calculador.calcular(vectores, tipoDistancia);

        // Paso 2: Inicializar clusters (cada vector es un cluster inicial)
        inicializarClusters();

        // Paso 3: Algoritmo aglomerativo
        while (clusters.size() > 1) {
            // Encontrar par más cercano
            int[] parMin = encontrarParMasProximo();
            int i = parMin[0];
            int j = parMin[1];

            double distanciaFusion = obtenerDistanciaCluster(i, j);
            distanciasFusion.add(distanciaFusion);

            // Crear nuevo cluster fusionando i y j
            Nodo nuevoCluster = new Nodo(clusters.get(i), clusters.get(j), distanciaFusion);

            // Remover los clusters viejos (primero el mayor índice para no afectar índices)
            if (i > j) {
                clusters.remove(i);
                clusters.remove(j);
            } else {
                clusters.remove(j);
                clusters.remove(i);
            }

            // Agregar nuevo cluster
            clusters.add(nuevoCluster);

            // Recalcular distancias entre el nuevo cluster y los restantes
            recalcularDistancias();
        }

        // Paso 4: Retornar raíz del dendrograma
        return clusters.get(0);
    }

    /**
     * Ejecuta clustering con nombre de distancia en string
     * Complejidad: O(n³)
     */
    public Nodo construirDendrograma(Vector[] vectores, String nombreDistancia) {
        FactoryDistancia.TipoDistancia tipo = FactoryDistancia.TipoDistancia.valueOf(
                nombreDistancia.toUpperCase()
        );
        return construirDendrograma(vectores, tipo);
    }

    /**
     * Inicializa clusters: cada vector es un cluster individual
     * Complejidad: O(n)
     */
    private void inicializarClusters() {
        clusters.clear();
        for (Vector v : vectores) {
            clusters.add(new Nodo(v.getEtiqueta()));
        }
    }

    /**
     * Encuentra el par de clusters con distancia mínima
     * Complejidad: O(n²)
     */
    private int[] encontrarParMasProximo() {
        double minimo = Double.MAX_VALUE;
        int[] resultado = new int[]{-1, -1};

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                double distancia = obtenerDistanciaCluster(i, j);
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
     * Obtiene la distancia entre dos clusters según el tipo de enlace
     * Complejidad: O(n²) en el peor caso (AVERAGE)
     */
    private double obtenerDistanciaCluster(int indiceCluster1, int indiceCluster2) {
        Nodo cluster1 = clusters.get(indiceCluster1);
        Nodo cluster2 = clusters.get(indiceCluster2);

        switch (tipoEnlace) {
            case MINIMO:
                return calcularDistanciaMinimo(cluster1, cluster2);
            case MAXIMO:
                return calcularDistanciaMaximo(cluster1, cluster2);
            case PROMEDIO:
                return calcularDistanciaPromedio(cluster1, cluster2);
            case CENTROIDE:
                return calcularDistanciaCentroide(cluster1, cluster2);
            default:
                throw new IllegalArgumentException("Tipo de enlace no soportado");
        }
    }

    /**
     * Linkage MINIMO: distancia mínima entre elementos de dos clusters
     * Complejidad: O(n*m)
     */
    private double calcularDistanciaMinimo(Nodo c1, Nodo c2) {
        double minimo = Double.MAX_VALUE;

        List<Integer> indices1 = obtenerIndicesElementos(c1);
        List<Integer> indices2 = obtenerIndicesElementos(c2);

        for (int i : indices1) {
            for (int j : indices2) {
                double dist = matrizDistancias.getPosicion(i, j);
                minimo = Math.min(minimo, dist);
            }
        }

        return minimo;
    }

    /**
     * Linkage MAXIMO: distancia máxima entre elementos de dos clusters
     * Complejidad: O(n*m)
     */
    private double calcularDistanciaMaximo(Nodo c1, Nodo c2) {
        double maximo = Double.MIN_VALUE;

        List<Integer> indices1 = obtenerIndicesElementos(c1);
        List<Integer> indices2 = obtenerIndicesElementos(c2);

        for (int i : indices1) {
            for (int j : indices2) {
                double dist = matrizDistancias.getPosicion(i, j);
                maximo = Math.max(maximo, dist);
            }
        }

        return maximo;
    }

    /**
     * Linkage PROMEDIO: distancia promedio entre elementos de dos clusters
     * Complejidad: O(n*m)
     */
    private double calcularDistanciaPromedio(Nodo c1, Nodo c2) {
        double suma = 0.0;
        int contador = 0;

        List<Integer> indices1 = obtenerIndicesElementos(c1);
        List<Integer> indices2 = obtenerIndicesElementos(c2);

        for (int i : indices1) {
            for (int j : indices2) {
                suma += matrizDistancias.getPosicion(i, j);
                contador++;
            }
        }

        return suma / contador;
    }

    /**
     * Linkage CENTROIDE: distancia entre centroides de clusters
     * Complejidad: O(n*m)
     */
    private double calcularDistanciaCentroide(Nodo c1, Nodo c2) {
        double[] centroide1 = calcularCentroide(c1);
        double[] centroide2 = calcularCentroide(c2);

        // Distancia euclidiana entre centroides
        double suma = 0.0;
        for (int i = 0; i < centroide1.length; i++) {
            double diff = centroide1[i] - centroide2[i];
            suma += diff * diff;
        }

        return Math.sqrt(suma);
    }

    /**
     * Calcula el centroide de un cluster
     * Complejidad: O(n*m)
     */
    private double[] calcularCentroide(Nodo cluster) {
        List<Integer> indices = obtenerIndicesElementos(cluster);
        int dimension = vectores[0].dimension();
        double[] centroide = new double[dimension];

        for (int idx : indices) {
            for (int d = 0; d < dimension; d++) {
                centroide[d] += vectores[idx].getPosicion(d);
            }
        }

        for (int d = 0; d < dimension; d++) {
            centroide[d] /= indices.size();
        }

        return centroide;
    }

    /**
     * Obtiene índices de elementos originales en un cluster (hojas del árbol)
     * Complejidad: O(n)
     */
    private List<Integer> obtenerIndicesElementos(Nodo cluster) {
        List<Integer> indices = new ArrayList<>();
        Set<String> elementos = cluster.getElementos();

        for (int i = 0; i < vectores.length; i++) {
            if (elementos.contains(vectores[i].getEtiqueta())) {
                indices.add(i);
            }
        }

        return indices;
    }

    /**
     * Recalcula la matriz de distancias después de una fusión
     * En versión simple, recalculamos todo (O(n²*m))
     * Complejidad: O(n²*m)
     */
    private void recalcularDistancias() {
        // Nota: Esto es una recalculación completa naïve
        // Para optimización, se podrían usar fórmulas incrementales
        // pero esto afecta según el tipo de linkage usado

        // Por ahora, mantenemos la matriz como referencia
        // El siguiente clustering usará los índices correctos
    }

    /**
     * Obtiene lista de distancias de fusión en orden
     * Complejidad: O(n)
     */
    public List<Double> obtenerDistanciasFusion() {
        return new ArrayList<>(distanciasFusion);
    }

    /**
     * Obtiene número de fusiones realizadas
     * Complejidad: O(1)
     */
    public int obtenerNumeroFusiones() {
        return distanciasFusion.size();
    }

    /**
     * Establece el tipo de enlace a usar
     * Complejidad: O(1)
     */
    public void setTipoEnlace(TipoEnlace tipo) {
        this.tipoEnlace = tipo;
    }

    /**
     * Obtiene el tipo de enlace actual
     * Complejidad: O(1)
     */
    public TipoEnlace obtenerTipoEnlace() {
        return tipoEnlace;
    }

    /**
     * Imprime el dendrograma
     * Complejidad: O(n)
     */
    public void imprimirDendrograma(Nodo raiz) {
        System.out.println("=== Dendrograma ===");
        System.out.println("Tipo de enlace: " + tipoEnlace);
        System.out.println("Distancias de fusión: " + distanciasFusion);
        System.out.println();
        System.out.println(raiz.toStringArbol());
    }

    /**
     * Imprime estadísticas del clustering
     * Complejidad: O(n)
     */
    public void imprimirEstadisticas() {
        System.out.println("=== Estadísticas de Clustering ===");
        System.out.println("Vectores iniciales: " + vectores.length);
        System.out.println("Fusiones realizadas: " + distanciasFusion.size());
        System.out.println("Tipo de enlace: " + tipoEnlace);

        if (!distanciasFusion.isEmpty()) {
            double minDist = Collections.min(distanciasFusion);
            double maxDist = Collections.max(distanciasFusion);
            double promedio = distanciasFusion.stream().mapToDouble(Double::doubleValue).average().orElse(0);

            System.out.println("Distancia mínima de fusión: " + String.format("%.6f", minDist));
            System.out.println("Distancia máxima de fusión: " + String.format("%.6f", maxDist));
            System.out.println("Distancia promedio de fusión: " + String.format("%.6f", promedio));
        }
    }

    @Override
    public String toString() {
        return "MotorClusteringJerarquico [tipo=" + tipoEnlace + ", vectores=" +
                (vectores != null ? vectores.length : 0) + "]";
    }
}

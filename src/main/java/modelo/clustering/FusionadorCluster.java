package modelo.clustering;

import modelo.estructuras.ListaDoble;
import modelo.estructuras.Matriz;
import modelo.estructuras.Nodo;

/**
 * Responsabilidad: Gestionar la fusión de clusters (buscar pares, fusionar, gestionar tamaños)
 */
public class FusionadorCluster {

    private ListaDoble<Nodo> clusters;
    private int[] tamanosClusters;

    public FusionadorCluster() {
        this.clusters = new ListaDoble<>();
    }

    /**
     * Inicializa los clusters con un nodo hoja por cada etiqueta
     */
    public void inicializar(String[] etiquetas) {
        clusters.limpiar();
        tamanosClusters = new int[etiquetas.length];

        for (int i = 0; i < etiquetas.length; i++) {
            clusters.agregar(new Nodo(etiquetas[i]));
            tamanosClusters[i] = 1;
        }
    }

    /**
     * Encuentra el par de clusters con menor distancia
     * @return Array [i, j] con los índices, o [-1, -1] si no hay par válido
     */
    public int[] encontrarParMasProximo(Matriz matrizDistancias) {
        double minimo = Double.MAX_VALUE;
        int[] resultado = new int[]{-1, -1};
        int numClusters = clusters.tamanio();

        for (int i = 0; i < numClusters; i++) {
            for (int j = i + 1; j < numClusters; j++) {
                double distancia = matrizDistancias.getPosicion(i, j);

                if (esDistanciaInvalida(distancia)) {
                    System.err.println("  [WARN] Distancia inválida en [" + i + "][" + j + "]: " + distancia);
                    continue;
                }

                if (distancia < minimo) {
                    minimo = distancia;
                    resultado[0] = i;
                    resultado[1] = j;
                }
            }
        }

        if (resultado[0] == -1 && numClusters > 1) {
            System.err.println("  [ERROR] No se encontró ningún par válido!");
            System.err.println("  Mínima distancia encontrada: " + minimo);
        }

        return resultado;
    }

    /**
     * Fusiona dos clusters en uno nuevo
     */
    public Nodo fusionar(int i, int j, double distanciaFusion) {
        // Crear nuevo cluster fusionado
        Nodo clusterI = clusters.obtener(i);
        Nodo clusterJ = clusters.obtener(j);
        Nodo nuevoCluster = new Nodo(clusterI, clusterJ, distanciaFusion);

        // Calcular nuevo tamaño
        int nuevoTamano = tamanosClusters[i] + tamanosClusters[j];

        // Remover clusters antiguos (mayor primero para no cambiar índices)
        if (i > j) {
            clusters.eliminarElemento(clusterI);
            tamanosClusters = removerIndice(tamanosClusters, i);
            clusters.eliminarElemento(clusterJ);
            tamanosClusters = removerIndice(tamanosClusters, j);
        } else {
            clusters.eliminarElemento(clusterJ);
            tamanosClusters = removerIndice(tamanosClusters, j);
            clusters.eliminarElemento(clusterI);
            tamanosClusters = removerIndice(tamanosClusters, i);
        }

        // Agregar nuevo cluster
        clusters.agregar(nuevoCluster);
        tamanosClusters = agregarElemento(tamanosClusters, nuevoTamano);

        return nuevoCluster;
    }

    /**
     * Obtiene el tamaño de un cluster específico
     */
    public int getTamanoCluster(int indice) {
        if (indice >= 0 && indice < tamanosClusters.length) {
            return tamanosClusters[indice];
        }
        return 0;
    }

    /**
     * Obtiene todos los tamaños (para Lance-Williams)
     */
    public int[] getTamanosClusters() {
        return tamanosClusters.clone();
    }

    /**
     * Obtiene el número actual de clusters
     */
    public int getNumeroClusters() {
        return clusters.tamanio();
    }

    /**
     * Obtiene la lista de clusters actual
     */
    public ListaDoble<Nodo> getClusters() {
        return clusters;
    }

    /**
     * Obtiene el cluster raíz final (cuando solo queda uno)
     */
    public Nodo getClusterRaiz() {
        if (clusters.tamanio() == 1) {
            return clusters.obtener(0);
        }
        return null;
    }

    /**
     * Verifica si hay más de un cluster (continuar clustering)
     */
    public boolean tieneMasDeUnCluster() {
        return clusters.tamanio() > 1;
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Verifica si una distancia es válida (no NaN, no infinito)
     */
    private boolean esDistanciaInvalida(double distancia) {
        return Double.isNaN(distancia) || Double.isInfinite(distancia);
    }

    /**
     * Remueve un elemento del array en la posición especificada
     */
    private int[] removerIndice(int[] array, int indice) {
        int[] resultado = new int[array.length - 1];
        int pos = 0;
        for (int i = 0; i < array.length; i++) {
            if (i != indice) {
                resultado[pos++] = array[i];
            }
        }
        return resultado;
    }

    /**
     * Agrega un elemento al final del array
     */
    private int[] agregarElemento(int[] array, int elemento) {
        int[] resultado = new int[array.length + 1];
        System.arraycopy(array, 0, resultado, 0, array.length);
        resultado[array.length] = elemento;
        return resultado;
    }

    @Override
    public String toString() {
        return "FusionadorClusters [clusters=" + clusters.tamanio() + "]";
    }
}

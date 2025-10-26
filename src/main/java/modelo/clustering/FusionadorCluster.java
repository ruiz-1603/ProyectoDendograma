package modelo.clustering;

import modelo.estructuras.ListaDoble;
import modelo.estructuras.Matriz;
import modelo.estructuras.Nodo;

public class FusionadorCluster {

    private ListaDoble<Nodo> clusters;
    private int[] tamanosClusters;

    public FusionadorCluster() {
        this.clusters = new ListaDoble<>();
    }

    public void inicializar(String[] etiquetas) {
        clusters.limpiar();
        tamanosClusters = new int[etiquetas.length];

        for (int i = 0; i < etiquetas.length; i++) {
            clusters.agregar(new Nodo(etiquetas[i]));
            tamanosClusters[i] = 1;
        }
    }

    // retorna Array [i, j] con los indices, o [-1, -1] si no hay par valido
    public int[] encontrarParMasProximo(Matriz matrizDistancias) {
        double minimo = Double.MAX_VALUE;
        int[] resultado = new int[]{-1, -1};
        int numClusters = clusters.tamanio();

        for (int i = 0; i < numClusters; i++) {
            for (int j = i + 1; j < numClusters; j++) {
                double distancia = matrizDistancias.getPosicion(i, j);

                if (esDistanciaInvalida(distancia)) {
                    System.err.println("Distancia inválida en [" + i + "][" + j + "]: " + distancia);
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
            System.err.println("No se encontró ningún par válido!");
            System.err.println("Mínima distancia encontrada: " + minimo);
        }

        return resultado;
    }

    public Nodo fusionar(int i, int j, double distanciaFusion) {
        Nodo clusterI = clusters.obtener(i);
        Nodo clusterJ = clusters.obtener(j);
        Nodo nuevoCluster = new Nodo(clusterI, clusterJ, distanciaFusion);

        int nuevoTamano = tamanosClusters[i] + tamanosClusters[j];

        // remover clusters antiguos (mayor primero para no cambiar indices)
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

        clusters.agregar(nuevoCluster);
        tamanosClusters = agregarElemento(tamanosClusters, nuevoTamano);

        return nuevoCluster;
    }

    public int getTamanoCluster(int indice) {
        if (indice >= 0 && indice < tamanosClusters.length) {
            return tamanosClusters[indice];
        }
        return 0;
    }

    public int[] getTamanosClusters() {
        return tamanosClusters.clone();
    }

    public int getNumeroClusters() {
        return clusters.tamanio();
    }

    public ListaDoble<Nodo> getClusters() {
        return clusters;
    }

    public Nodo getClusterRaiz() {
        if (clusters.tamanio() == 1) {
            return clusters.obtener(0);
        }
        return null;
    }

    public boolean tieneMasDeUnCluster() {
        return clusters.tamanio() > 1;
    }

    // HELPERS

    private boolean esDistanciaInvalida(double distancia) {
        return Double.isNaN(distancia) || Double.isInfinite(distancia);
    }

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

package modelo.clustering;

import modelo.estructuras.*;
import modelo.distancias.CalculadorMatrizDistancia;
import modelo.distancias.FactoryDistancia;
import modelo.estructuras.Vector;

import java.util.*;

public class MotorCluster {

    public enum TipoEnlace {
        MINIMO,
        MAXIMO,
        PROMEDIO,
        CENTROIDE
    }

    private Vector[] vectores;
    private Matriz matrizDistancias;
    private List<Nodo> clusters;
    private List<Double> distanciasFusion;
    private TipoEnlace tipoEnlace;
    private CalculadorMatrizDistancia calculador;
    private int[] tamanoClusters;

    public MotorCluster() {
        this.tipoEnlace = TipoEnlace.PROMEDIO;
        this.clusters = new ArrayList<>();
        this.distanciasFusion = new ArrayList<>();
        this.calculador = new CalculadorMatrizDistancia();
        this.tamanoClusters = new int[0];
    }

    public MotorCluster(TipoEnlace tipoEnlace) {
        this.tipoEnlace = tipoEnlace;
        this.clusters = new ArrayList<>();
        this.distanciasFusion = new ArrayList<>();
        this.calculador = new CalculadorMatrizDistancia();
        this.tamanoClusters = new int[0];
    }

    public Nodo construirDendrograma(Vector[] vectores, FactoryDistancia.TipoDistancia tipoDistancia) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        this.vectores = vectores;
        this.distanciasFusion.clear();

        System.out.println("  [Clustering] Calculando matriz de distancias inicial...");
        matrizDistancias = calculador.calcular(vectores, tipoDistancia);

        inicializarClusters();

        int iteracion = 0;
        long inicio = System.currentTimeMillis();

        while (clusters.size() > 1) {
            iteracion++;

            int[] parMin = encontrarParMasProximo();
            int i = parMin[0];
            int j = parMin[1];

            if (i == -1 || j == -1) {
                System.err.println("  No se encontró par válido");
                System.err.println("  Clusters restantes: " + clusters.size());
                break;
            }

            double distanciaFusion = matrizDistancias.getPosicion(i, j);
            distanciasFusion.add(distanciaFusion);

            Nodo nuevoCluster = new Nodo(clusters.get(i), clusters.get(j), distanciaFusion);
            int nuevoTamano = tamanoClusters[i] + tamanoClusters[j];

            actualizarMatrizLanceWilliams(i, j, distanciaFusion);

            if (i > j) {
                clusters.remove(i);
                tamanoClusters = removerIndice(tamanoClusters, i);
                clusters.remove(j);
                tamanoClusters = removerIndice(tamanoClusters, j);
            } else {
                clusters.remove(j);
                tamanoClusters = removerIndice(tamanoClusters, j);
                clusters.remove(i);
                tamanoClusters = removerIndice(tamanoClusters, i);
            }

            clusters.add(nuevoCluster);
            tamanoClusters = agregarElemento(tamanoClusters, nuevoTamano);
        }

        long duracion = System.currentTimeMillis() - inicio;
        System.out.println("  Clustering - Completado en " + (duracion / 1000.0) + " segundos");
        System.out.println("  Clustering -  Total de fusiones: " + distanciasFusion.size());

        return clusters.get(0);
    }

    public Nodo construirDendrograma(Vector[] vectores, String nombreDistancia) {
        FactoryDistancia.TipoDistancia tipo = FactoryDistancia.TipoDistancia.valueOf(
                nombreDistancia.toUpperCase()
        );
        return construirDendrograma(vectores, tipo);
    }

    private void inicializarClusters() {
        clusters.clear();
        tamanoClusters = new int[vectores.length];

        for (int i = 0; i < vectores.length; i++) {
            clusters.add(new Nodo(vectores[i].getEtiqueta()));
            tamanoClusters[i] = 1;
        }
    }

    private int[] encontrarParMasProximo() {
        double minimo = Double.MAX_VALUE;
        int[] resultado = new int[]{-1, -1};

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                double distancia = matrizDistancias.getPosicion(i, j);
                if (Double.isNaN(distancia) || Double.isInfinite(distancia)) {
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
        if (resultado[0] == -1 && clusters.size() > 1) {
            System.err.println("  [ERROR] No se encontró ningún par válido!");
            System.err.println("  Mínima distancia encontrada: " + minimo);
        }
        return resultado;
    }

    private void actualizarMatrizLanceWilliams(int i, int j, double distanciaIJ) {
        int ni = tamanoClusters[i];
        int nj = tamanoClusters[j];

        double alpha_i, alpha_j, gamma;

        switch (tipoEnlace) {
            case MINIMO:
                alpha_i = 0.5;
                alpha_j = 0.5;
                gamma = -0.5;
                break;
            case MAXIMO:
                alpha_i = 0.5;
                alpha_j = 0.5;
                gamma = 0.5;
                break;
            case PROMEDIO:
                alpha_i = (double) ni / (ni + nj);
                alpha_j = (double) nj / (ni + nj);
                gamma = 0.0;
                break;
            case CENTROIDE:
                alpha_i = (double) ni / (ni + nj);
                alpha_j = (double) nj / (ni + nj);
                gamma = -((double) ni * nj) / ((ni + nj) * (ni + nj));
                break;
            default:
                throw new IllegalArgumentException("Tipo de enlace no soportado");
        }

        for (int k = 0; k < clusters.size(); k++) {
            if (k == i || k == j) continue;

            double distanciaIK = matrizDistancias.getPosicion(i, k);
            double distanciaJK = matrizDistancias.getPosicion(j, k);

            double nuevaDistancia = alpha_i * distanciaIK +
                    alpha_j * distanciaJK +
                    gamma * distanciaIJ;

            matrizDistancias.setPosicion(i, k, nuevaDistancia);
            matrizDistancias.setPosicion(k, i, nuevaDistancia);
        }
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

    public List<Double> obtenerDistanciasFusion() {
        return new ArrayList<>(distanciasFusion);
    }

    public int obtenerNumeroFusiones() {
        return distanciasFusion.size();
    }

    public void setTipoEnlace(TipoEnlace tipo) {
        this.tipoEnlace = tipo;
    }

    public TipoEnlace getTipoEnlace() {
        return tipoEnlace;
    }

    public void imprimirDendrograma(Nodo raiz) {
        Dendograma dendo = new Dendograma();
        System.out.println("=== Dendrograma ===");
        System.out.println("Tipo de enlace: " + tipoEnlace);
        System.out.println("Distancias de fusión: " + distanciasFusion.size());
        System.out.println();
        System.out.println(dendo.toStringArbol(raiz));
    }

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
        return "MotorCluster [tipo=" + tipoEnlace + ", vectores=" +
                (vectores != null ? vectores.length : 0) + "]";
    }
}
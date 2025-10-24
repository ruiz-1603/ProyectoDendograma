package modelo.clustering;

import modelo.estructuras.ListaDoble;

/**
 * Responsabilidad: Calcular y mostrar estadísticas del clustering
 */
public class EstadisticasCluster {

    private int vectoresIniciales;
    private ListaDoble<Double> distanciasFusion;
    private CalculadorLanceWilliams.TipoEnlace tipoEnlace;

    public EstadisticasCluster(int vectoresIniciales, CalculadorLanceWilliams.TipoEnlace tipoEnlace) {
        this.vectoresIniciales = vectoresIniciales;
        this.tipoEnlace = tipoEnlace;
        this.distanciasFusion = new ListaDoble<>();
    }

    /**
     * Registra una nueva fusión
     */
    public void registrarFusion(double distancia) {
        distanciasFusion.agregar(distancia);
    }

    /**
     * Obtiene el número total de fusiones realizadas
     */
    public int getNumeroFusiones() {
        return distanciasFusion.tamanio();
    }

    /**
     * Obtiene todas las distancias de fusión
     */
    public ListaDoble<Double> getDistanciasFusion() {
        return distanciasFusion;
    }

    /**
     * Calcula la distancia mínima de fusión
     */
    public double getDistanciaMinima() {
        if (distanciasFusion.tamanio() == 0) return 0.0;

        double min = distanciasFusion.obtener(0);
        for (int i = 1; i < distanciasFusion.tamanio(); i++) {
            double actual = distanciasFusion.obtener(i);
            if (actual < min) {
                min = actual;
            }
        }
        return min;
    }

    /**
     * Calcula la distancia máxima de fusión
     */
    public double getDistanciaMaxima() {
        if (distanciasFusion.tamanio() == 0) return 0.0;

        double max = distanciasFusion.obtener(0);
        for (int i = 1; i < distanciasFusion.tamanio(); i++) {
            double actual = distanciasFusion.obtener(i);
            if (actual > max) {
                max = actual;
            }
        }
        return max;
    }

    /**
     * Calcula la distancia promedio de fusión
     */
    public double getDistanciaPromedio() {
        if (distanciasFusion.tamanio() == 0) return 0.0;

        double suma = 0.0;
        for (int i = 0; i < distanciasFusion.tamanio(); i++) {
            suma += distanciasFusion.obtener(i);
        }
        return suma / distanciasFusion.tamanio();
    }

    /**
     * Imprime estadísticas del clustering
     */
    public void imprimir() {
        System.out.println("=== Estadísticas de Clustering ===");
        System.out.println("Vectores iniciales: " + vectoresIniciales);
        System.out.println("Fusiones realizadas: " + getNumeroFusiones());
        System.out.println("Tipo de enlace: " + tipoEnlace);

        if (distanciasFusion.tamanio() > 0) {
            System.out.println("Distancia mínima de fusión: " + String.format("%.6f", getDistanciaMinima()));
            System.out.println("Distancia máxima de fusión: " + String.format("%.6f", getDistanciaMaxima()));
            System.out.println("Distancia promedio de fusión: " + String.format("%.6f", getDistanciaPromedio()));
        }
    }

    @Override
    public String toString() {
        return "EstadisticasCluster [vectores=" + vectoresIniciales +
                ", fusiones=" + getNumeroFusiones() +
                ", enlace=" + tipoEnlace + "]";
    }
}

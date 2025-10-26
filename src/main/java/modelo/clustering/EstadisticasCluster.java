package modelo.clustering;

import modelo.estructuras.ListaDoble;

public class EstadisticasCluster {

    private int vectoresIniciales;
    private ListaDoble<Double> distanciasFusion;
    private CalculadorLanceWilliams.TipoEnlace tipoEnlace;

    public EstadisticasCluster(int vectoresIniciales, CalculadorLanceWilliams.TipoEnlace tipoEnlace) {
        this.vectoresIniciales = vectoresIniciales;
        this.tipoEnlace = tipoEnlace;
        this.distanciasFusion = new ListaDoble<>();
    }

    public void registrarFusion(double distancia) {
        distanciasFusion.agregar(distancia);
    }

    public int getNumeroFusiones() {
        return distanciasFusion.tamanio();
    }

    public ListaDoble<Double> getDistanciasFusion() {
        return distanciasFusion;
    }

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

    public double getDistanciaPromedio() {
        if (distanciasFusion.tamanio() == 0) return 0.0;

        double suma = 0.0;
        for (int i = 0; i < distanciasFusion.tamanio(); i++) {
            suma += distanciasFusion.obtener(i);
        }
        return suma / distanciasFusion.tamanio();
    }

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

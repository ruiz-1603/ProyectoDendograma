package modelo.distancias;

import modelo.estructuras.Vector;

public class DistanciaEuclidiana implements ICalculadorDistancia {

    @Override
    public double calcular(Vector v1, Vector v2) {
        if (v1.dimension() != v2.dimension()) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimensión. " +
                            "v1: " + v1.dimension() + ", v2: " + v2.dimension()
            );
        }

        double suma = 0.0;

        // Σ(xi - yi)^2
        for (int i = 0; i < v1.dimension(); i++) {
            double diferencia = v1.getPosicion(i) - v2.getPosicion(i);
            suma += diferencia * diferencia;
        }

        return Math.sqrt(suma);
    }

    @Override
    public String getNombre() {
        return "Euclidiana";
    }

    @Override
    public String toString() {
        return "Distancia Euclidiana: d(x,y) = √(Σ(xi - yi)²)";
    }
}
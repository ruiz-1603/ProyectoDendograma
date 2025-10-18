package modelo.distancias;

import modelo.estructuras.Vector;

/**
 * Distancia Euclidiana (L2)
 * Mide la distancia en línea recta entre dos puntos
 * Fórmula: d(x,y) = √(Σ(xi - yi)²)
 *
 * Complejidad: O(n) donde n es la dimensión del vector
 */
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

        // Σ(xi - yi)²
        for (int i = 0; i < v1.dimension(); i++) {
            double diferencia = v1.getPosicion(i) - v2.getPosicion(i);
            suma += diferencia * diferencia;
        }

        // √(suma)
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
package modelo.distancias;

import modelo.estructuras.Vector;

/**
 * Distancia Manhattan (L1 o de bloque)
 * Suma las diferencias absolutas en cada dimensión
 * Fórmula: d(x,y) = Σ|xi - yi|
 *
 * Usada en modelos lineales y redes neuronales
 * También conocida como distancia de taxicab o city block
 *
 * Complejidad: O(n) donde n es la dimensión del vector
 */
public class DistanciaManhattan implements ICalculadorDistancia {

    @Override
    public double calcular(Vector v1, Vector v2) {
        if (v1.dimension() != v2.dimension()) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimension. " +
                            "v1: " + v1.dimension() + ", v2: " + v2.dimension()
            );
        }

        double suma = 0.0;

        // Σ|xi - yi|
        for (int i = 0; i < v1.dimension(); i++) {
            double diferencia = v1.getPosicion(i) - v2.getPosicion(i);
            suma += Math.abs(diferencia);
        }

        return suma;
    }

    @Override
    public String getNombre() {
        return "Manhattan";
    }

    @Override
    public String toString() {
        return "Distancia Manhattan: d(x,y) = Σ|xi - yi|";
    }
}
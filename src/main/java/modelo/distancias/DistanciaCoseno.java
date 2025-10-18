package modelo.distancias;

import modelo.estructuras.Vector;

/**
 * Distancia Coseno
 * Mide el ángulo entre dos vectores, no su magnitud
 * Fórmula: d(x,y) = 1 - (x·y)/(||x|| * ||y||)
 *
 * Donde:
 * - x·y es el producto punto
 * - ||x|| es la norma (magnitud) del vector x
 *
 * Nota: En el proyecto anterior era similaridad (sin el 1-)
 * pero aquí se solicita distancia (restando de 1)
 *
 * Complejidad: O(n) donde n es la dimensión del vector
 */
public class DistanciaCoseno implements ICalculadorDistancia {

    @Override
    public double calcular(Vector v1, Vector v2) {
        if (v1.dimension() != v2.dimension()) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimensión. " +
                            "v1: " + v1.dimension() + ", v2: " + v2.dimension()
            );
        }

        // Calcular producto punto: x·y
        double productoPunto = v1.productoPunto(v2);

        // Calcular normas: ||x|| y ||y||
        double normaV1 = v1.norma();
        double normaV2 = v2.norma();

        // Evitar división por cero
        if (normaV1 == 0.0 || normaV2 == 0.0) {
            return 1.0; // Máxima distancia si algún vector es cero
        }

        // Similaridad coseno: (x·y)/(||x|| * ||y||)
        double similaridad = productoPunto / (normaV1 * normaV2);

        // Distancia coseno: 1 - similaridad
        // Nota: se resta de 1 según el enunciado del proyecto
        return 1.0 - similaridad;
    }

    @Override
    public String getNombre() {
        return "Coseno";
    }

    @Override
    public String toString() {
        return "Distancia Coseno: d(x,y) = 1 - (x·y)/(||x|| * ||y||)";
    }
}
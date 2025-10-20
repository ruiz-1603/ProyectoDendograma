package modelo.distancias;

import modelo.estructuras.Vector;

public class DistanciaCoseno implements ICalculadorDistancia {

    @Override
    public double calcular(Vector v1, Vector v2) {
        if (v1.dimension() != v2.dimension()) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimensión. " +
                            "v1: " + v1.dimension() + ", v2: " + v2.dimension()
            );
        }

        // calcular producto punto: x·y
        double productoPunto = v1.productoPunto(v2);

        // calcular normas: ||x|| y ||y||
        double normaV1 = v1.norma();
        double normaV2 = v2.norma();

        // evitar division por cero
        if (normaV1 == 0.0 || normaV2 == 0.0) {
            return 1.0; // max distancia si algun vector es cero
        }

        // similitud coseno: (x·y)/(||x|| * ||y||)
        double similaridad = productoPunto / (normaV1 * normaV2);

        // distancia coseno: 1 - similaridad
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
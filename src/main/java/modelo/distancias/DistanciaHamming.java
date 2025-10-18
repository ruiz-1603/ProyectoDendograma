package modelo.distancias;

import modelo.estructuras.Vector;

public class DistanciaHamming implements ICalculadorDistancia {

    private static final double TOLERANCIA = 1e-9;

    @Override
    public double calcular(Vector v1, Vector v2) {
        if (v1.dimension() != v2.dimension()) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimensión. " +
                            "v1: " + v1.dimension() + ", v2: " + v2.dimension()
            );
        }

        int diferencias = 0;

        // Contar posiciones donde xi ≠ yi
        for (int i = 0; i < v1.dimension(); i++) {
            double val1 = v1.getPosicion(i);
            double val2 = v2.getPosicion(i);

            // Comparación con tolerancia para valores de punto flotante
            if (Math.abs(val1 - val2) > TOLERANCIA) {
                diferencias++;
            }
        }

        return diferencias;
    }

    /**
     * Versión alternativa para vectores estrictamente binarios
     * Más eficiente si se sabe que los valores son 0 o 1
     */
    public double calcularBinario(Vector v1, Vector v2) {
        if (v1.dimension() != v2.dimension()) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimensión"
            );
        }

        int diferencias = 0;

        for (int i = 0; i < v1.dimension(); i++) {
            // Comparación directa para valores binarios
            if (v1.getPosicion(i) != v2.getPosicion(i)) {
                diferencias++;
            }
        }

        return diferencias;
    }

    @Override
    public String getNombre() {
        return "Hamming";
    }

    @Override
    public String toString() {
        return "Distancia Hamming: d(x,y) = Σ(xi ≠ yi)";
    }
}
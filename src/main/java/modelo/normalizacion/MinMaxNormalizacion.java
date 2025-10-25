package modelo.normalizacion;

import modelo.estructuras.Vector;

public class MinMaxNormalizacion implements INormalizacion {

    @Override
    public Vector[] normalizar(Vector[] vectores) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        int dimension = vectores[0].dimension();
        Vector[] resultado = new Vector[vectores.length];

        // Calcular mín y máx para cada dimensión
        double[] minimos = new double[dimension];
        double[] maximos = new double[dimension];

        for (int i = 0; i < dimension; i++) {
            minimos[i] = Double.MAX_VALUE;
            maximos[i] = Double.MIN_VALUE;
        }

        // Encontrar mín y máx
        for (Vector v : vectores) {
            for (int i = 0; i < dimension; i++) {
                double valor = v.getPosicion(i);
                minimos[i] = Math.min(minimos[i], valor);
                maximos[i] = Math.max(maximos[i], valor);
            }
        }

        // Normalizar cada vector
        for (int v = 0; v < vectores.length; v++) {
            double[] datosNormalizados = new double[dimension];

            for (int i = 0; i < dimension; i++) {
                double valor = vectores[v].getPosicion(i);
                double rango = maximos[i] - minimos[i];

                // Evitar división por cero
                if (rango == 0.0) {
                    datosNormalizados[i] = 0.0;
                } else {
                    datosNormalizados[i] = (valor - minimos[i]) / rango;
                }
            }

            resultado[v] = new Vector(datosNormalizados, vectores[v].getEtiqueta());
        }

        return resultado;
    }

    @Override
    public String getNombre() {
        return "Min-Max";
    }

    @Override
    public String toString() {
        return "Estrategia Min-Max: f(x) = (x - min) / (max - min)";
    }
}

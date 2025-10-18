package modelo.normalizacion;

import modelo.estructuras.*;

public class ZScoreNormalizacion implements INormalizacion {

    @Override
    public Vector[] normalizar(Vector[] vectores) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        int dimension = vectores[0].dimension();
        Vector[] resultado = new Vector[vectores.length];

        // Calcular media para cada dimensión
        double[] medias = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            double suma = 0.0;
            for (Vector v : vectores) {
                suma += v.getPosicion(i);
            }
            medias[i] = suma / vectores.length;
        }

        // Calcular desviación estándar para cada dimensión
        double[] desviacionesEstandar = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            double sumaCuadrados = 0.0;
            for (Vector v : vectores) {
                double diferencia = v.getPosicion(i) - medias[i];
                sumaCuadrados += diferencia * diferencia;
            }
            double varianza = sumaCuadrados / vectores.length;
            desviacionesEstandar[i] = Math.sqrt(varianza);
        }

        // Normalizar cada vector
        for (int v = 0; v < vectores.length; v++) {
            double[] datosNormalizados = new double[dimension];

            for (int i = 0; i < dimension; i++) {
                double valor = vectores[v].getPosicion(i);
                double sigma = desviacionesEstandar[i];

                // Evitar división por cero
                if (sigma == 0.0) {
                    datosNormalizados[i] = 0.0;
                } else {
                    datosNormalizados[i] = (valor - medias[i]) / sigma;
                }
            }

            resultado[v] = new Vector(datosNormalizados, vectores[v].getEtiqueta());
        }

        return resultado;
    }

    @Override
    public String getNombre() {
        return "Z-Score";
    }

    @Override
    public String toString() {
        return "Estrategia Z-Score: f(x) = (x - μ) / σ";
    }
}

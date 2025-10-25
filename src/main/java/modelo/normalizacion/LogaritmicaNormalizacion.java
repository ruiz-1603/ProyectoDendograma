package modelo.normalizacion;

import modelo.estructuras.Vector;

public class LogaritmicaNormalizacion implements INormalizacion {

    @Override
    public Vector[] normalizar(Vector[] vectores) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        int dimension = vectores[0].dimension();
        Vector[] resultado = new Vector[vectores.length];

        // Normalizar cada vector
        for (int v = 0; v < vectores.length; v++) {
            double[] datosNormalizados = new double[dimension];

            for (int i = 0; i < dimension; i++) {
                double valor = vectores[v].getPosicion(i);

                // Aplicar log(x+1)
                // El +1 evita log(0) = -infinito
                datosNormalizados[i] = Math.log(valor + 1.0);
            }

            resultado[v] = new Vector(datosNormalizados, vectores[v].getEtiqueta());
        }

        return resultado;
    }

    @Override
    public String getNombre() {
        return "Logarítmica";
    }

    @Override
    public String toString() {
        return "Estrategia Logarítmica: f(x) = log(x + 1)";
    }
}

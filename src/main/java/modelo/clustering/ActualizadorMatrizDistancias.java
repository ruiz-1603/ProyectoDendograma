package modelo.clustering;

import modelo.estructuras.Matriz;

public class ActualizadorMatrizDistancias {

    public enum TipoEnlace {
        MINIMO,
        MAXIMO,
        PROMEDIO,
        CENTROIDE
    }

    private TipoEnlace tipoEnlace;

    public ActualizadorMatrizDistancias(TipoEnlace tipoEnlace) {
        this.tipoEnlace = tipoEnlace;
    }

    public void actualizarMatriz(Matriz matrizDistancias, int i, int j,
                                 double distanciaIJ, int[] tamanoClusters,
                                 int numeroClusters) {
        int ni = tamanoClusters[i];
        int nj = tamanoClusters[j];

        ParametrosActualizacion params = calcularParametros(ni, nj);

        for (int k = 0; k < numeroClusters; k++) {
            if (k == i || k == j) continue;

            double distanciaIK = matrizDistancias.getPosicion(i, k);
            double distanciaJK = matrizDistancias.getPosicion(j, k);

            double nuevaDistancia = params.alphaI * distanciaIK +
                    params.alphaJ * distanciaJK +
                    params.gamma * distanciaIJ;

            matrizDistancias.setPosicion(i, k, nuevaDistancia);
            matrizDistancias.setPosicion(k, i, nuevaDistancia);
        }
    }

    private ParametrosActualizacion calcularParametros(int ni, int nj) {
        double alphaI, alphaJ, gamma;

        switch (tipoEnlace) {
            case MINIMO:
                alphaI = 0.5;
                alphaJ = 0.5;
                gamma = -0.5;
                break;

            case MAXIMO:
                alphaI = 0.5;
                alphaJ = 0.5;
                gamma = 0.5;
                break;

            case PROMEDIO:
                alphaI = (double) ni / (ni + nj);
                alphaJ = (double) nj / (ni + nj);
                gamma = 0.0;
                break;

            case CENTROIDE:
                alphaI = (double) ni / (ni + nj);
                alphaJ = (double) nj / (ni + nj);
                gamma = -((double) ni * nj) / ((ni + nj) * (ni + nj));
                break;

            default:
                throw new IllegalArgumentException("Tipo de enlace no soportado");
        }

        return new ParametrosActualizacion(alphaI, alphaJ, gamma);
    }

    public TipoEnlace getTipoEnlace() {
        return tipoEnlace;
    }

    public void setTipoEnlace(TipoEnlace tipoEnlace) {
        this.tipoEnlace = tipoEnlace;
    }

    // clase interna para almacenar parametros de Lance-Williams
    private static class ParametrosActualizacion {
        final double alphaI;
        final double alphaJ;
        final double gamma;

        ParametrosActualizacion(double alphaI, double alphaJ, double gamma) {
            this.alphaI = alphaI;
            this.alphaJ = alphaJ;
            this.gamma = gamma;
        }
    }

    @Override
    public String toString() {
        return "ActualizadorMatrizDistancias [tipo=" + tipoEnlace + "]";
    }
}

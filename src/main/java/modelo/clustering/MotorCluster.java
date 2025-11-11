package modelo.clustering;

import modelo.estructuras.*;
import modelo.distancias.CalculadorMatrizDistancia;
import modelo.distancias.FactoryDistancia;

public class MotorCluster {

    public enum TipoEnlace {
        MINIMO,
        MAXIMO,
        PROMEDIO,
        CENTROIDE
    }

    private Vector[] vectores;
    private Matriz matrizDistancias;
    private CalculadorMatrizDistancia calculadorMatriz;

    private ActualizadorMatrizDistancias actualizadorMatriz;
    private FusionadorCluster fusionador;

    public MotorCluster() {
        this(TipoEnlace.PROMEDIO);
    }

    public MotorCluster(TipoEnlace tipoEnlace) {
        this.calculadorMatriz = new CalculadorMatrizDistancia();
        this.actualizadorMatriz = new ActualizadorMatrizDistancias(
                convertirTipoEnlace(tipoEnlace)
        );
        this.fusionador = new FusionadorCluster();
    }

    public Nodo construirDendrograma(Vector[] vectores, FactoryDistancia.TipoDistancia tipoDistancia) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        this.vectores = vectores;

        // calcular matriz de distancias inicial
        System.out.println("  [Clustering] Calculando matriz de distancias inicial...");
        matrizDistancias = calculadorMatriz.calcular(vectores, tipoDistancia);

        // inicializar clusters (uno por vector)
        String[] etiquetas = extraerEtiquetas(vectores);
        fusionador.inicializar(etiquetas);

        // ejecutar algoritmo de clustering
        long inicio = System.currentTimeMillis();
        ejecutarAlgoritmo();
        long duracion = System.currentTimeMillis() - inicio;

        System.out.println("  Clustering - Completado en " + (duracion / 1000.0) + " segundos");

        return fusionador.getClusterRaiz();
    }

    public Nodo construirDendrograma(Vector[] vectores, String nombreDistancia) {
        FactoryDistancia.TipoDistancia tipo = FactoryDistancia.TipoDistancia.valueOf(
                nombreDistancia.toUpperCase()
        );
        return construirDendrograma(vectores, tipo);
    }

    private void ejecutarAlgoritmo() {
        int iteracion = 0;

        while (fusionador.tieneMasDeUnCluster()) {
            iteracion++;

            int[] parMin = fusionador.encontrarParMasProximo(matrizDistancias);
            int i = parMin[0];
            int j = parMin[1];

            if (i == -1 || j == -1) {
                System.err.println("  Error: No se encontró par válido en iteración " + iteracion);
                System.err.println("  Clusters restantes: " + fusionador.getNumeroClusters());
                break;
            }

            // obtener distancia de fusion
            double distanciaFusion = matrizDistancias.getPosicion(i, j);

            // actualizar matriz de distancias usando Lance-Williams
            actualizadorMatriz.actualizarMatriz(
                    matrizDistancias,
                    i, j,
                    distanciaFusion,
                    fusionador.getTamanosClusters(),
                    fusionador.getNumeroClusters()
            );

            fusionador.fusionar(i, j, distanciaFusion);
        }
    }

    private String[] extraerEtiquetas(Vector[] vectores) {
        String[] etiquetas = new String[vectores.length];
        for (int i = 0; i < vectores.length; i++) {
            etiquetas[i] = vectores[i].getEtiqueta();
        }
        return etiquetas;
    }

    private ActualizadorMatrizDistancias.TipoEnlace convertirTipoEnlace(TipoEnlace tipo) {
        switch (tipo) {
            case MINIMO: return ActualizadorMatrizDistancias.TipoEnlace.MINIMO;
            case MAXIMO: return ActualizadorMatrizDistancias.TipoEnlace.MAXIMO;
            case PROMEDIO: return ActualizadorMatrizDistancias.TipoEnlace.PROMEDIO;
            case CENTROIDE: return ActualizadorMatrizDistancias.TipoEnlace.CENTROIDE;
            default: throw new IllegalArgumentException("Tipo de enlace no soportado");
        }
    }

    private TipoEnlace convertirDesdeActualizador(ActualizadorMatrizDistancias.TipoEnlace tipoLW) {
        switch (tipoLW) {
            case MINIMO: return TipoEnlace.MINIMO;
            case MAXIMO: return TipoEnlace.MAXIMO;
            case PROMEDIO: return TipoEnlace.PROMEDIO;
            case CENTROIDE: return TipoEnlace.CENTROIDE;
            default: return TipoEnlace.PROMEDIO;
        }
    }

    public void setTipoEnlace(TipoEnlace tipo) {
        actualizadorMatriz.setTipoEnlace(convertirTipoEnlace(tipo));
    }

    public TipoEnlace getTipoEnlace() {
        return convertirDesdeActualizador(actualizadorMatriz.getTipoEnlace());
    }

    public void imprimirDendrograma(Nodo raiz) {
        Dendrograma dendo = new Dendrograma();
        System.out.println("Dendrograma");
        System.out.println("Tipo de enlace: " + getTipoEnlace());
        System.out.println();
        System.out.println(dendo.toStringArbol(raiz));
    }

    @Override
    public String toString() {
        return "MotorCluster [tipo=" + getTipoEnlace() + ", vectores=" +
                (vectores != null ? vectores.length : 0) + "]";
    }
}

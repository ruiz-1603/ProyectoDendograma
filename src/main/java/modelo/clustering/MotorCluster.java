package modelo.clustering;

import modelo.estructuras.*;
import modelo.distancias.CalculadorMatrizDistancia;
import modelo.distancias.FactoryDistancia;

/**
 * Responsabilidad: LÓGICA DEL ALGORITMO de clustering jerárquico aglomerativo
 */
public class MotorCluster {

    // Para mantener compatibilidad con código existente
    public enum TipoEnlace {
        MINIMO,
        MAXIMO,
        PROMEDIO,
        CENTROIDE
    }

    private Vector[] vectores;
    private Matriz matrizDistancias;
    private CalculadorMatrizDistancia calculadorMatriz;

    // Componentes especializados
    private CalculadorLanceWilliams calculadorLanceWilliams;
    private FusionadorCluster fusionador;
    private EstadisticasCluster estadisticas;

    public MotorCluster() {
        this(TipoEnlace.PROMEDIO);
    }

    public MotorCluster(TipoEnlace tipoEnlace) {
        this.calculadorMatriz = new CalculadorMatrizDistancia();
        this.calculadorLanceWilliams = new CalculadorLanceWilliams(
                convertirTipoEnlace(tipoEnlace)
        );
        this.fusionador = new FusionadorCluster();
    }

    /**
     * Construye el dendrograma usando clustering jerárquico aglomerativo
     */
    public Nodo construirDendrograma(Vector[] vectores, FactoryDistancia.TipoDistancia tipoDistancia) {
        if (vectores == null || vectores.length == 0) {
            throw new IllegalArgumentException("Array de vectores no puede estar vacío");
        }

        this.vectores = vectores;
        this.estadisticas = new EstadisticasCluster(
                vectores.length,
                calculadorLanceWilliams.getTipoEnlace()
        );

        // Fase 1: Calcular matriz de distancias inicial
        System.out.println("  [Clustering] Calculando matriz de distancias inicial...");
        matrizDistancias = calculadorMatriz.calcular(vectores, tipoDistancia);

        // Fase 2: Inicializar clusters (uno por vector)
        String[] etiquetas = extraerEtiquetas(vectores);
        fusionador.inicializar(etiquetas);

        // Fase 3: Ejecutar algoritmo de clustering
        long inicio = System.currentTimeMillis();
        ejecutarAlgoritmo();
        long duracion = System.currentTimeMillis() - inicio;

        System.out.println("  Clustering - Completado en " + (duracion / 1000.0) + " segundos");
        System.out.println("  Clustering - Total de fusiones: " + estadisticas.getNumeroFusiones());

        return fusionador.getClusterRaiz();
    }

    public Nodo construirDendrograma(Vector[] vectores, String nombreDistancia) {
        FactoryDistancia.TipoDistancia tipo = FactoryDistancia.TipoDistancia.valueOf(
                nombreDistancia.toUpperCase()
        );
        return construirDendrograma(vectores, tipo);
    }

    /**
     * ALGORITMO PRINCIPAL: Clustering jerárquico aglomerativo
     *
     * Repite hasta tener un solo cluster:
     * 1. Encuentra el par de clusters más cercano
     * 2. Fusiona ese par en un nuevo cluster
     * 3. Actualiza la matriz de distancias
     */
    private void ejecutarAlgoritmo() {
        int iteracion = 0;

        while (fusionador.tieneMasDeUnCluster()) {
            iteracion++;

            // Paso 1: Encontrar el par de clusters más próximo
            int[] parMin = fusionador.encontrarParMasProximo(matrizDistancias);
            int i = parMin[0];
            int j = parMin[1];

            if (i == -1 || j == -1) {
                System.err.println("  Error: No se encontró par válido en iteración " + iteracion);
                System.err.println("  Clusters restantes: " + fusionador.getNumeroClusters());
                break;
            }

            // Paso 2: Obtener distancia de fusión
            double distanciaFusion = matrizDistancias.getPosicion(i, j);
            estadisticas.registrarFusion(distanciaFusion);

            // Paso 3: Actualizar matriz de distancias usando Lance-Williams
            // (ANTES de fusionar, porque necesitamos los índices i, j originales)
            calculadorLanceWilliams.actualizarMatriz(
                    matrizDistancias,
                    i, j,
                    distanciaFusion,
                    fusionador.getTamanosClusters(),
                    fusionador.getNumeroClusters()
            );

            // Paso 4: Fusionar los clusters i y j
            fusionador.fusionar(i, j, distanciaFusion);
        }
    }

    /**
     * Extrae las etiquetas de los vectores
     */
    private String[] extraerEtiquetas(Vector[] vectores) {
        String[] etiquetas = new String[vectores.length];
        for (int i = 0; i < vectores.length; i++) {
            etiquetas[i] = vectores[i].getEtiqueta();
        }
        return etiquetas;
    }

    /**
     * Convierte TipoEnlace del MotorCluster a TipoEnlace del CalculadorLanceWilliams
     */
    private CalculadorLanceWilliams.TipoEnlace convertirTipoEnlace(TipoEnlace tipo) {
        switch (tipo) {
            case MINIMO: return CalculadorLanceWilliams.TipoEnlace.MINIMO;
            case MAXIMO: return CalculadorLanceWilliams.TipoEnlace.MAXIMO;
            case PROMEDIO: return CalculadorLanceWilliams.TipoEnlace.PROMEDIO;
            case CENTROIDE: return CalculadorLanceWilliams.TipoEnlace.CENTROIDE;
            default: throw new IllegalArgumentException("Tipo de enlace no soportado");
        }
    }

    private TipoEnlace convertirDesdeLanceWilliams(CalculadorLanceWilliams.TipoEnlace tipoLW) {
        switch (tipoLW) {
            case MINIMO: return TipoEnlace.MINIMO;
            case MAXIMO: return TipoEnlace.MAXIMO;
            case PROMEDIO: return TipoEnlace.PROMEDIO;
            case CENTROIDE: return TipoEnlace.CENTROIDE;
            default: return TipoEnlace.PROMEDIO;
        }
    }

    // ========== API PÚBLICA (mantener compatibilidad) ==========

    public ListaDoble<Double> obtenerDistanciasFusion() {
        return estadisticas != null ? estadisticas.getDistanciasFusion() : new ListaDoble<>();
    }

    public int obtenerNumeroFusiones() {
        return estadisticas != null ? estadisticas.getNumeroFusiones() : 0;
    }

    public void setTipoEnlace(TipoEnlace tipo) {
        calculadorLanceWilliams.setTipoEnlace(convertirTipoEnlace(tipo));
    }

    public TipoEnlace getTipoEnlace() {
        return convertirDesdeLanceWilliams(calculadorLanceWilliams.getTipoEnlace());
    }

    public void imprimirDendrograma(Nodo raiz) {
        Dendograma dendo = new Dendograma();
        System.out.println("=== Dendrograma ===");
        System.out.println("Tipo de enlace: " + getTipoEnlace());
        System.out.println("Distancias de fusión: " + obtenerNumeroFusiones());
        System.out.println();
        System.out.println(dendo.toStringArbol(raiz));
    }

    public void imprimirEstadisticas() {
        if (estadisticas != null) {
            estadisticas.imprimir();
        } else {
            System.out.println("No hay estadísticas disponibles. Ejecute el clustering primero.");
        }
    }

    @Override
    public String toString() {
        return "MotorCluster [tipo=" + getTipoEnlace() + ", vectores=" +
                (vectores != null ? vectores.length : 0) + "]";
    }
}
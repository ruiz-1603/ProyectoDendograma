package modelo;

import modelo.datos.CargadorCSV;
import modelo.estructuras.Vector;
import modelo.estructuras.Nodo;
import modelo.normalizacion.Normalizador;
import modelo.normalizacion.FactoryNormalizacion;
import modelo.distancias.CalculadorMatrizDistancia;
import modelo.distancias.FactoryDistancia;
import modelo.clustering.MotorCluster;

public class PruebaCompleta {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║          PRUEBA COMPLETA: Sistema de Dendrograma y Clustering         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            // PASO 1: Cargar CSV
            System.out.println("PASO 1: Cargando datos desde CSV...");
            System.out.println("─".repeat(70));
            CargadorCSV cargador = new CargadorCSV();
            cargador.cargar("src/main/resources/movie_dataset.csv"); // Cambiar ruta según tu setup
            cargador.imprimirEstadisticas();
            System.out.println();

            // PASO 2: Obtener vectores
            System.out.println("PASO 2: Convirtiendo a vectores...");
            System.out.println("─".repeat(70));
            Vector[] vectoresOriginales = cargador.obtenerVectores();

            // TOMAR SOLO LAS PRIMERAS 100 PELÍCULAS PARA PRUEBA RÁPIDA
            Vector[] vectoresMuestra = new Vector[Math.min(100, vectoresOriginales.length)];
            System.arraycopy(vectoresOriginales, 0, vectoresMuestra, 0, vectoresMuestra.length);
            vectoresOriginales = vectoresMuestra;

            System.out.println("Vectores cargados: " + vectoresOriginales.length + " (primeros 100 para prueba rápida)");
            if (vectoresOriginales.length > 0) {
                System.out.println("Primeras 3 películas:");
                for (int i = 0; i < Math.min(3, vectoresOriginales.length); i++) {
                    System.out.println("  " + vectoresOriginales[i].getEtiqueta() +
                            " → " + vectoresOriginales[i].dimension() + " dimensiones");
                }
            }
            System.out.println();

            // PASO 3: Normalizar
            System.out.println("PASO 3: Normalizando vectores (Min-Max)...");
            System.out.println("─".repeat(70));
            Normalizador normalizador = new Normalizador(FactoryNormalizacion.TipoNormalizacion.MIN_MAX);
            Vector[] vectoresNormalizados = normalizador.normalizar(vectoresOriginales);
            System.out.println("✓ " + vectoresNormalizados.length + " vectores normalizados");
            System.out.println();

            // PASO 4: Calcular matriz de distancias
            System.out.println("PASO 4: Calculando matriz de distancias (Euclidiana)...");
            System.out.println("─".repeat(70));
            CalculadorMatrizDistancia calculador = new CalculadorMatrizDistancia();
            calculador.calcular(vectoresNormalizados, FactoryDistancia.TipoDistancia.EUCLIDIANA);
            calculador.imprimirEstadisticas();
            System.out.println();

            // PASO 5: Construir dendrograma
            System.out.println("PASO 5: Construyendo dendrograma (Clustering jerárquico)...");
            System.out.println("─".repeat(70));
            MotorCluster motor = new MotorCluster(MotorCluster.TipoEnlace.PROMEDIO);
            Nodo raiz = motor.construirDendrograma(vectoresNormalizados,
                    FactoryDistancia.TipoDistancia.EUCLIDIANA);

            motor.imprimirEstadisticas();
            System.out.println();

            // PASO 6: Información del dendrograma
            System.out.println("PASO 6: Análisis del dendrograma...");
            System.out.println("─".repeat(70));
            System.out.println("Altura del árbol: " + raiz.altura());
            System.out.println("Número de hojas: " + raiz.contarHojas());
            System.out.println("Número de fusiones: " + motor.obtenerNumeroFusiones());
            System.out.println();

            // PASO 7: Exportar a JSON
            System.out.println("PASO 7: Generando JSON del dendrograma...");
            System.out.println("─".repeat(70));
            String json = raiz.toJSON();
            System.out.println("JSON (primeros 500 caracteres):");
            System.out.println(json.substring(0, Math.min(500, json.length())) + "...");
            System.out.println();

            // PASO 8: Probar diferentes configuraciones
            System.out.println("PASO 8: Probando diferentes distancias...");
            System.out.println("─".repeat(70));
            probarDiferentesDistancias(vectoresNormalizados);
            System.out.println();

            System.out.println("PASO 9: Probando diferentes tipos de enlace...");
            System.out.println("─".repeat(70));
            probarDiferentesEnlaces(vectoresNormalizados);
            System.out.println();

            // PASO 10: Resumen
            System.out.println("╔════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                   ✓ PRUEBA COMPLETADA EXITOSAMENTE                   ║");
            System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("Resumen del pipeline:");
            System.out.println("  1. CSV → " + vectoresOriginales.length + " películas cargadas");
            System.out.println("  2. Vectores → " + vectoresNormalizados[0].dimension() + " dimensiones");
            System.out.println("  3. Normalización → Min-Max aplicado");
            System.out.println("  4. Distancia → Euclidiana con " + calculador.obtenerNumeroVectores() + " elementos");
            System.out.println("  5. Clustering → Dendrograma con altura " + raiz.altura());
            System.out.println("  6. JSON → Exportado y listo para visualizar");

        } catch (Exception e) {
            System.err.println("\n✗ Error durante la prueba:");
            e.printStackTrace();
        }
    }

    /**
     * Prueba el sistema con diferentes métricas de distancia
     */
    private static void probarDiferentesDistancias(Vector[] vectores) {
        FactoryDistancia.TipoDistancia[] distancias = {
                FactoryDistancia.TipoDistancia.EUCLIDIANA,
                FactoryDistancia.TipoDistancia.MANHATTAN,
                FactoryDistancia.TipoDistancia.COSENO,
                FactoryDistancia.TipoDistancia.HAMMING
        };

        for (FactoryDistancia.TipoDistancia distancia : distancias) {
            try {
                MotorCluster motor = new MotorCluster();
                Nodo raiz = motor.construirDendrograma(vectores, distancia);

                double distanciaMax = motor.obtenerDistanciasFusion().stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0);

                System.out.printf("  %-12s → Altura: %2d | Dist.Máx: %8.4f | ✓ OK%n",
                        distancia.toString(), raiz.altura(), distanciaMax);
            } catch (Exception e) {
                System.out.printf("  %-12s → ✗ Error: %s%n",
                        distancia.toString(), e.getMessage());
            }
        }
    }

    /**
     * Prueba el sistema con diferentes tipos de enlace
     */
    private static void probarDiferentesEnlaces(Vector[] vectores) {
        MotorCluster.TipoEnlace[] enlaces = {
                MotorCluster.TipoEnlace.MINIMO,
                MotorCluster.TipoEnlace.MAXIMO,
                MotorCluster.TipoEnlace.PROMEDIO,
                MotorCluster.TipoEnlace.CENTROIDE
        };

        for (MotorCluster.TipoEnlace enlace : enlaces) {
            try {
                MotorCluster motor = new MotorCluster(enlace);
                Nodo raiz = motor.construirDendrograma(vectores, FactoryDistancia.TipoDistancia.EUCLIDIANA);

                double distanciaMax = motor.obtenerDistanciasFusion().stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0);

                System.out.printf("  %-10s → Altura: %2d | Dist.Máx: %8.4f | ✓ OK%n",
                        enlace.toString(), raiz.altura(), distanciaMax);
            } catch (Exception e) {
                System.out.printf("  %-10s → ✗ Error: %s%n",
                        enlace.toString(), e.getMessage());
            }
        }
    }
}

package modelo;

import modelo.estructuras.Vector;
import modelo.estructuras.Nodo;
import modelo.normalizacion.Normalizador;
import modelo.normalizacion.FactoryNormalizacion;
import modelo.distancias.CalculadorMatrizDistancia;
import modelo.distancias.FactoryDistancia;
import modelo.clustering.MotorCluster;

public class PruebaClusteringDendrograma {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     PRUEBA: Sistema de Clustering Jerárquico y Dendrograma     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            // PASO 1: Crear datos de prueba
            System.out.println("PASO 1: Creando datos de prueba...");
            Vector[] vectoresOriginales = crearDatosEjemplo();
            imprimirVectores("Vectores originales", vectoresOriginales);

            // PASO 2: Normalizar
            System.out.println("\n" + "=".repeat(70));
            System.out.println("PASO 2: Normalizando con Min-Max...");
            Normalizador normalizador = new Normalizador(FactoryNormalizacion.TipoNormalizacion.MIN_MAX);
            Vector[] vectoresNormalizados = normalizador.normalizar(vectoresOriginales);
            imprimirVectores("Vectores normalizados", vectoresNormalizados);

            // PASO 3: Calcular matriz de distancias
            System.out.println("\n" + "=".repeat(70));
            System.out.println("PASO 3: Calculando matriz de distancias (Euclidiana)...");
            CalculadorMatrizDistancia calculador = new CalculadorMatrizDistancia();
            calculador.calcular(vectoresNormalizados, FactoryDistancia.TipoDistancia.EUCLIDIANA);
            calculador.imprimir();
            calculador.imprimirEstadisticas();

            // PASO 4: Construir dendrograma
            System.out.println("\n" + "=".repeat(70));
            System.out.println("PASO 4: Construyendo dendrograma con clustering jerárquico...");
            MotorCluster motor = new MotorCluster(
                    MotorCluster.TipoEnlace.PROMEDIO
            );
            Nodo raiz = motor.construirDendrograma(vectoresNormalizados, FactoryDistancia.TipoDistancia.EUCLIDIANA);

            motor.imprimirDendrograma(raiz);
            motor.imprimirEstadisticas();

            // PASO 5: Exportar a JSON
            System.out.println("\n" + "=".repeat(70));
            System.out.println("PASO 5: Exportando dendrograma a JSON...");
            String json = raiz.toJSON();
            System.out.println(json);

            // PASO 6: Información del árbol
            System.out.println("\n" + "=".repeat(70));
            System.out.println("PASO 6: Información del dendrograma...");
            System.out.println("Raíz: " + raiz);
            System.out.println("Altura del árbol: " + raiz.altura());
            System.out.println("Número de hojas: " + raiz.contarHojas());
            System.out.println("Elementos: " + raiz.getElementos());

            // PASO 7: Probar con diferentes distancias
            System.out.println("\n" + "=".repeat(70));
            System.out.println("PASO 7: Probando con diferentes distancias...");
            probarDiferentesDistancias(vectoresNormalizados);

            // PASO 8: Probar con diferentes tipos de enlace
            System.out.println("\n" + "=".repeat(70));
            System.out.println("PASO 8: Probando con diferentes tipos de enlace...");
            probarDiferentesEnlaces(vectoresNormalizados);

            System.out.println("\n" + "✓ ¡Todas las pruebas completadas exitosamente!");

        } catch (Exception e) {
            System.err.println("\n✗ Error durante la prueba:");
            e.printStackTrace();
        }
    }

    /**
     * Crea datos de ejemplo para la prueba
     * 8 películas con características: presupuesto, popularidad, calificación
     */
    private static Vector[] crearDatosEjemplo() {
        Vector[] vectores = new Vector[8];

        // Película 1: Avatar (presupuesto alto, muy popular, buena calificación)
        vectores[0] = new Vector(new double[]{237.0, 150.4, 7.2}, "Avatar");

        // Película 2: Titanic (presupuesto alto, muy popular, buena calificación)
        vectores[1] = new Vector(new double[]{200.0, 123.5, 7.9}, "Titanic");

        // Película 3: Inception (presupuesto alto, popular, muy buena calificación)
        vectores[2] = new Vector(new double[]{160.0, 145.2, 8.8}, "Inception");

        // Película 4: Interstellar (presupuesto alto, popular, muy buena calificación)
        vectores[3] = new Vector(new double[]{165.0, 142.6, 8.6}, "Interstellar");

        // Película 5: The Room (presupuesto bajo, poco popular, baja calificación)
        vectores[4] = new Vector(new double[]{6.0, 8.2, 3.5}, "TheRoom");

        // Película 6: Disaster Movie (presupuesto bajo, poco popular, baja calificación)
        vectores[5] = new Vector(new double[]{8.0, 7.5, 2.1}, "DisasterMovie");

        // Película 6: Birdemic (presupuesto muy bajo, poco popular, muy baja calificación)
        vectores[6] = new Vector(new double[]{10.0, 5.1, 1.7}, "Birdemic");

        // Película 7: Plan 9 from Outer Space (presupuesto bajo, poco popular, baja calificación)
        vectores[7] = new Vector(new double[]{15.0, 12.3, 4.5}, "Plan9");

        return vectores;
    }

    /**
     * Imprime un array de vectores de forma legible
     */
    private static void imprimirVectores(String titulo, Vector[] vectores) {
        System.out.println(titulo + ":");
        for (Vector v : vectores) {
            System.out.println("  " + v);
        }
    }

    /**
     * Prueba el sistema con diferentes distancias
     */
    private static void probarDiferentesDistancias(Vector[] vectores) {
        FactoryDistancia.TipoDistancia[] distancias = {
                FactoryDistancia.TipoDistancia.EUCLIDIANA,
                FactoryDistancia.TipoDistancia.MANHATTAN,
                FactoryDistancia.TipoDistancia.COSENO,
                FactoryDistancia.TipoDistancia.HAMMING
        };

        for (FactoryDistancia.TipoDistancia distancia : distancias) {
            System.out.println("\n  Distancia: " + distancia);
            try {
                MotorCluster motor = new MotorCluster();
                Nodo raiz = motor.construirDendrograma(vectores, distancia);
                System.out.println("    ✓ Dendrograma construido | Altura: " + raiz.altura() +
                        " | Hojas: " + raiz.contarHojas());
            } catch (Exception e) {
                System.out.println("    ✗ Error: " + e.getMessage());
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
            System.out.println("\n  Tipo de enlace: " + enlace);
            try {
                MotorCluster motor = new MotorCluster(enlace);
                Nodo raiz = motor.construirDendrograma(vectores, FactoryDistancia.TipoDistancia.EUCLIDIANA);

                double distanciaMax = motor.obtenerDistanciasFusion().stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0);

                System.out.println("    ✓ Dendrograma construido | Altura: " + raiz.altura() +
                        " | Dist. máxima: " + String.format("%.4f", distanciaMax));
            } catch (Exception e) {
                System.out.println("    ✗ Error: " + e.getMessage());
            }
        }
    }
}

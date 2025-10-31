package modelo;

import modelo.datos.CargadorCSV;
import modelo.datos.SelectorColumnas;
import modelo.estructuras.Vector;
import modelo.estructuras.Nodo;
import modelo.estructuras.Dendrograma;
import modelo.normalizacion.Normalizador;
import modelo.normalizacion.FactoryNormalizacion;
import modelo.distancias.CalculadorMatrizDistancia;
import modelo.distancias.FactoryDistancia;
import modelo.clustering.MotorCluster;
import modelo.clustering.Ponderador;
import modelo.estructuras.ListaDoble;

// Pipeline: CSV -> Vectores -> Ponderación -> Normalización -> Distancias -> Clustering -> Dendrograma -> JSON
public class PruebaCompleta {

    public static void main(String[] args) {
        System.out.println("Prueba");
        System.out.println();

        CargadorCSV cargador = null;
        SelectorColumnas selector = null;
        Ponderador ponderador = null;
        CalculadorMatrizDistancia calculador = null;
        Nodo raiz = null;
        MotorCluster motor = null;
        Dendrograma dendrograma = new Dendrograma();

        try {
            // cargar CSV
            System.out.println("PASO 1: Cargando datos desde CSV...");
            System.out.println("─".repeat(70));
            cargador = new CargadorCSV();
            cargador.cargar("src/main/resources/movie_dataset.csv", 40);
            cargador.imprimirEstadisticas();
            System.out.println();

            // obtener vectores
            System.out.println("PASO 2: Convirtiendo a vectores...");
            System.out.println("─".repeat(70));
            Vector[] vectoresOriginales = cargador.getVectores();
            System.out.println("Vectores cargados: " + vectoresOriginales.length);
            if (vectoresOriginales.length > 0) {
                System.out.println("Primeras 3 películas:");
                for (int i = 0; i < Math.min(3, vectoresOriginales.length); i++) {
                    System.out.println("  " + vectoresOriginales[i].getEtiqueta() +
                            " → " + vectoresOriginales[i].dimension() + " dimensiones");
                }
            }
            System.out.println();

            // selector de columnas
            System.out.println("PASO 2.5: Configurando selector de columnas...");
            System.out.println("─".repeat(70));
            selector = new SelectorColumnas(cargador.getNombresDimensiones());
            selector.imprimir();
            System.out.println();

            // aplicar pesos
            System.out.println("PASO 2.6: Configurando pesos de dimensiones...");
            System.out.println("─".repeat(70));
            double[] pesos = new double[cargador.getDimensiones()];
            for (int i = 0; i < pesos.length; i++) {
                pesos[i] = 1.0;
            }

            ponderador = new Ponderador(pesos, cargador.getNombresDimensiones());
            ponderador.imprimir();
            System.out.println();

            // aplicar pesos a vectores
            System.out.println("PASO 2.7: Aplicando pesos a vectores...");
            System.out.println("─".repeat(70));
            Vector[] vectoresPonderados = ponderador.aplicarPesos(vectoresOriginales);
            System.out.println("✓ " + vectoresPonderados.length + " vectores ponderados");
            System.out.println();

            // normalizar
            System.out.println("PASO 3: Normalizando vectores (Min-Max)...");
            System.out.println("─".repeat(70));
            Normalizador normalizador = new Normalizador(FactoryNormalizacion.TipoNormalizacion.MIN_MAX);
            Vector[] vectoresNormalizados = normalizador.normalizar(vectoresPonderados);
            System.out.println("✓ " + vectoresNormalizados.length + " vectores normalizados");
            System.out.println();

            // calcular matriz de distancias
            System.out.println("Calculando matriz de distancias (Euclidiana)...");
            System.out.println("─".repeat(70));
            calculador = new CalculadorMatrizDistancia();
            calculador.calcular(vectoresNormalizados, FactoryDistancia.TipoDistancia.EUCLIDIANA);
            calculador.imprimirEstadisticas();
            System.out.println();

            // construir dendrograma
            System.out.println("Construyendo dendrograma (Clustering jerárquico)...");
            System.out.println("─".repeat(70));
            motor = new MotorCluster(MotorCluster.TipoEnlace.PROMEDIO);
            raiz = motor.construirDendrograma(vectoresNormalizados,
                    FactoryDistancia.TipoDistancia.EUCLIDIANA);

            motor.imprimirEstadisticas();
            System.out.println();

            // info del dendrograma
            System.out.println("Análisis del dendrograma...");
            System.out.println("─".repeat(70));
            System.out.println("Altura del árbol: " + dendrograma.altura(raiz));
            System.out.println("Número de hojas: " + dendrograma.contarHojas(raiz));
            System.out.println("Número de fusiones: " + motor.obtenerNumeroFusiones());
            System.out.println();

            // exportar JSON
            System.out.println("Generando JSON del dendrograma...");
            System.out.println("─".repeat(70));
            String json = dendrograma.toJSON(raiz);
            System.out.println("JSON (primeros 500 caracteres):");
            System.out.println(json.substring(0, Math.min(500, json.length())) + "...");

            guardarJsonAArchivo(json, "dendrograma.json");
            System.out.println();

            // diferentes distancias
            System.out.println("Probando diferentes distancias...");
            System.out.println("─".repeat(70));
            probarDiferentesDistancias(vectoresNormalizados, dendrograma);
            System.out.println();

            // PASO 9: Probar diferentes tipos de enlace
            System.out.println("Probando diferentes tipos de enlace...");
            System.out.println("─".repeat(70));
            probarDiferentesEnlaces(vectoresNormalizados, dendrograma);
            System.out.println();

            // resumen
            System.out.println("Prueba completada");
            System.out.println();
            System.out.println("Resumen del pipeline:");
            System.out.println("  1. CSV: " + cargador.getNumeroFilas() + " películas cargadas");
            System.out.println("  2. Vectores: " + cargador.getDimensiones() + " dimensiones originales");
            System.out.println("  3. Selector: " + selector.getNumeroSeleccionadas() + " dimensiones seleccionadas");
            System.out.println("  4. Ponderación: " + (ponderador.tienePonderacion() ? "Activa" : "Sin ponderación"));
            System.out.println("  5. Normalización: Min-Max aplicado");
            System.out.println("  6. Distancia: Euclidiana con " + calculador.getNumeroVectores() + " elementos");
            System.out.println("  7. Clustering: Dendrograma con altura " + dendrograma.altura(raiz));
            System.out.println("  8. JSON: Exportado a dendrograma.json");

        } catch (Exception e) {
            System.err.println("\nError durante la prueba:");
            e.printStackTrace();
        }
    }

    private static void guardarJsonAArchivo(String json, String nombreArchivo) {
        try {
            java.io.File archivo = new java.io.File(nombreArchivo);
            java.io.FileWriter writer = new java.io.FileWriter(archivo);
            writer.write(json);
            writer.close();
            System.out.println("✓ JSON guardado en: " + archivo.getAbsolutePath());
        } catch (java.io.IOException e) {
            System.err.println("✗ Error al guardar JSON: " + e.getMessage());
        }
    }

    private static void probarDiferentesDistancias(Vector[] vectores, Dendrograma dendrograma) {
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

                // encontrar distancia max
                ListaDoble<Double> distanciasFusion = motor.obtenerDistanciasFusion();
                double distanciaMax = encontrarMaximo(distanciasFusion);

                System.out.printf("  %-12s → Altura: %2d | Dist.Máx: %8.4f | ✓ OK%n",
                        distancia.toString(), dendrograma.altura(raiz), distanciaMax);
            } catch (Exception e) {
                System.out.printf("  %-12s → ✗ Error: %s%n",
                        distancia.toString(), e.getMessage());
            }
        }
    }

    private static void probarDiferentesEnlaces(Vector[] vectores, Dendrograma dendrograma) {
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

                // encontrar distancia max
                ListaDoble<Double> distanciasFusion = motor.obtenerDistanciasFusion();
                double distanciaMax = encontrarMaximo(distanciasFusion);

                System.out.printf("  %-10s → Altura: %2d | Dist.Máx: %8.4f | ✓ OK%n",
                        enlace.toString(), dendrograma.altura(raiz), distanciaMax);
            } catch (Exception e) {
                System.out.printf("  %-10s → ✗ Error: %s%n",
                        enlace.toString(), e.getMessage());
            }
        }
    }

    private static double encontrarMaximo(ListaDoble<Double> lista) {
        if (lista.tamanio() == 0) {
            return 0.0;
        }

        double max = lista.obtener(0);
        for (int i = 1; i < lista.tamanio(); i++) {
            double actual = lista.obtener(i);
            if (actual > max) {
                max = actual;
            }
        }
        return max;
    }
}
package modelo.datos;

import modelo.estructuras.Vector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CargadorCSV {

    private static final String[] COLUMNAS_NUMERICAS = {
            "budget", "popularity", "revenue", "runtime", "vote_average", "vote_count"
    };

    private static final String[] COLUMNAS_CATEGORICAS = {
            "original_language", "status"
    };

    private static final String[] COLUMNAS_CONTEO = {
            "genres", "keywords", "cast"
    };

    private static final String[] COLUMNAS_JSON_ARRAY = {
            "production_companies", "production_countries", "spoken_languages"
    };

    // director, crew, revisar cuales sí
    private static final String[] COLUMNAS_IGNORAR = {
            "index", "homepage", "id", "original_title", "overview", "tagline", "crew", "director"
    };

    private static final String COLUMNA_IDENTIFICADOR = "title";
    private static final String COLUMNA_FECHA = "release_date";

    private String[] headers;
    private List<Map<String, String>> datos;
    private String rutaArchivo;
    private Map<String, Integer> indicesColumnasNumericas;
    private Map<String, Integer> indicesColumnasCategoricas;
    private Map<String, Integer> indicesColumnasConteo;
    private Map<String, Integer> indicesColumnasJsonArray;
    private int indiceIdentificador;
    private int indiceFecha;

    private Map<String, List<String>> categoriasUnicas;

    private LocalDate fechaMinima;
    private LocalDate fechaMaxima;

    public CargadorCSV() {
        this.headers = new String[0];
        this.datos = new ArrayList<>();
        this.rutaArchivo = "";
        this.indicesColumnasNumericas = new HashMap<>();
        this.indicesColumnasCategoricas = new HashMap<>();
        this.indicesColumnasConteo = new HashMap<>();
        this.indicesColumnasJsonArray = new HashMap<>();
        this.categoriasUnicas = new HashMap<>();
        this.indiceIdentificador = -1;
        this.indiceFecha = -1;
        this.fechaMinima = null;
        this.fechaMaxima = null;
    }

    public void cargar(String ruta) throws IOException {
        this.rutaArchivo = ruta;
        this.datos.clear();
        this.categoriasUnicas.clear();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8))) {

            String primeraLinea = reader.readLine();
            if (primeraLinea == null) {
                throw new IOException("Archivo vacío");
            }

            this.headers = parsearLinea(primeraLinea);
            construirIndicesColumnas();

            if (indiceIdentificador == -1) {
                throw new IOException("Columna '" + COLUMNA_IDENTIFICADOR + "' no encontrada");
            }

            String linea;
            int numeroLinea = 2;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                try {
                    String[] valores = parsearLinea(linea);
                    Map<String, String> fila = construirFila(valores);
                    datos.add(fila);
                } catch (Exception e) {
                    System.err.println("Advertencia línea " + numeroLinea + ": " + e.getMessage());
                }
                numeroLinea++;
            }
        }

        // procesar datos
        extraerCategoriasUnicas();
        extraerRangoFechas();

        int totalDimensiones = COLUMNAS_NUMERICAS.length + contarDimensionesOneHot() +
                COLUMNAS_CONTEO.length + COLUMNAS_JSON_ARRAY.length + 1; // +1 para fecha

        System.out.println("✓ CSV cargado: " + datos.size() + " películas");
        System.out.println("  - Dimensiones numéricas: " + COLUMNAS_NUMERICAS.length);
        System.out.println("  - Dimensiones categóricas (one-hot): " + contarDimensionesOneHot());
        System.out.println("  - Dimensiones de conteo: " + (COLUMNAS_CONTEO.length + COLUMNAS_JSON_ARRAY.length));
        System.out.println("  - Dimensión temporal: 1");
        System.out.println("  - Total dimensiones: " + totalDimensiones);
    }

    private void construirIndicesColumnas() {
        indicesColumnasNumericas.clear();
        indicesColumnasCategoricas.clear();
        indicesColumnasConteo.clear();
        indicesColumnasJsonArray.clear();

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();

            if (header.equals(COLUMNA_IDENTIFICADOR)) {
                indiceIdentificador = i;
            }
            if (header.equals(COLUMNA_FECHA)) {
                indiceFecha = i;
            }

            for (String col : COLUMNAS_NUMERICAS) {
                if (header.equals(col)) indicesColumnasNumericas.put(col, i);
            }

            for (String col : COLUMNAS_CATEGORICAS) {
                if (header.equals(col)) indicesColumnasCategoricas.put(col, i);
            }

            for (String col : COLUMNAS_CONTEO) {
                if (header.equals(col)) indicesColumnasConteo.put(col, i);
            }

            for (String col : COLUMNAS_JSON_ARRAY) {
                if (header.equals(col)) indicesColumnasJsonArray.put(col, i);
            }
        }
    }

    private void extraerCategoriasUnicas() {
        for (String columna : COLUMNAS_CATEGORICAS) {
            Set<String> unicos = new TreeSet<>();

            for (Map<String, String> fila : datos) {
                String valor = fila.get(columna);
                if (valor != null && !valor.isEmpty() && !valor.equals("null")) {
                    unicos.add(valor.trim());
                }
            }

            if (unicos.isEmpty()) {
                unicos.add("desconocido");
            }

            categoriasUnicas.put(columna, new ArrayList<>(unicos));
        }
    }

    private void extraerRangoFechas() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<LocalDate> fechas = new ArrayList<>();

        for (Map<String, String> fila : datos) {
            String fechaStr = fila.get(COLUMNA_FECHA);
            if (fechaStr != null && !fechaStr.isEmpty() && !fechaStr.equals("null")) {
                try {
                    LocalDate fecha = LocalDate.parse(fechaStr.trim(), formatter);
                    fechas.add(fecha);
                } catch (Exception e) {
                    // Ignorar fechas inválidas
                }
            }
        }

        if (!fechas.isEmpty()) {
            fechaMinima = Collections.min(fechas);
            fechaMaxima = Collections.max(fechas);
        } else {
            fechaMinima = LocalDate.of(1900, 1, 1);
            fechaMaxima = LocalDate.now();
        }
    }

    private int contarElementos(String texto) {
        if (texto == null || texto.isEmpty() || texto.equals("null")) {
            return 0;
        }

        // Dividir por espacios o comas
        String[] elementos = texto.split("[,\\s]+");
        return elementos.length;
    }

    private int contarElementosJson(String json) {
        if (json == null || json.isEmpty() || json.equals("null")) {
            return 0;
        }

        // contar llaves abiertas (cada objeto es un elemento)
        return json.split("\\{").length - 1;
    }

    private double normalizarFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty() || fechaStr.equals("null")) {
            return 0.5; // Valor medio si es nula
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fecha = LocalDate.parse(fechaStr.trim(), formatter);

            long diasDesdeMinimo = java.time.temporal.ChronoUnit.DAYS.between(fechaMinima, fecha);
            long diasTotales = java.time.temporal.ChronoUnit.DAYS.between(fechaMinima, fechaMaxima);

            if (diasTotales == 0) {
                return 0.5;
            }

            return (double) diasDesdeMinimo / diasTotales;
        } catch (Exception e) {
            return 0.5;
        }
    }

    private int contarDimensionesOneHot() {
        int total = 0;
        for (String columna : COLUMNAS_CATEGORICAS) {
            total += categoriasUnicas.getOrDefault(columna, new ArrayList<>()).size();
        }
        return total;
    }

    // parsea una línea CSV respetando comillas (RFC 4180)
    private String[] parsearLinea(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder campoActual = new StringBuilder();
        boolean dentroComillas = false;
        int i = 0;

        while (i < linea.length()) {
            char c = linea.charAt(i);

            if (c == '"') {
                if (dentroComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    campoActual.append('"');
                    i++;
                } else {
                    dentroComillas = !dentroComillas;
                }
            } else if (c == ',' && !dentroComillas) {
                campos.add(campoActual.toString());
                campoActual = new StringBuilder();
            } else {
                campoActual.append(c);
            }

            i++;
        }

        campos.add(campoActual.toString());
        return campos.toArray(new String[0]);
    }

    private Map<String, String> construirFila(String[] valores) {
        Map<String, String> fila = new LinkedHashMap<>();

        for (int i = 0; i < headers.length && i < valores.length; i++) {
            fila.put(headers[i].trim(), valores[i].trim());
        }

        for (int i = valores.length; i < headers.length; i++) {
            fila.put(headers[i].trim(), "");
        }

        return fila;
    }

    // de datos a vectores
    public Vector[] getVectores() {
        List<Vector> vectores = new ArrayList<>();

        for (Map<String, String> fila : datos) {
            String identificador = fila.get(COLUMNA_IDENTIFICADOR);
            if (identificador == null || identificador.isEmpty()) {
                continue;
            }

            List<Double> datosVector = new ArrayList<>();

            // columnas numericas directas
            for (String columna : COLUMNAS_NUMERICAS) {
                String valor = fila.get(columna);
                if (valor == null || valor.isEmpty() || valor.equals("null")) {
                    datosVector.add(0.0);
                } else {
                    try {
                        double num = Double.parseDouble(valor);
                        datosVector.add(num < 0 ? 0.0 : num);
                    } catch (NumberFormatException e) {
                        datosVector.add(0.0);
                    }
                }
            }

            // columnas categoricas (one-hot)
            for (String columna : COLUMNAS_CATEGORICAS) {
                String valor = fila.get(columna);
                if (valor == null || valor.isEmpty() || valor.equals("null")) {
                    valor = "desconocido";
                }

                List<String> categorias = categoriasUnicas.get(columna);
                if (categorias != null) {
                    for (String categoria : categorias) {
                        datosVector.add(valor.equals(categoria) ? 1.0 : 0.0);
                    }
                }
            }

            // columnas de conteo (texto)
            for (String columna : COLUMNAS_CONTEO) {
                String valor = fila.get(columna);
                int conteo = contarElementos(valor);
                datosVector.add((double) conteo);
            }

            // columnas de JSON array
            for (String columna : COLUMNAS_JSON_ARRAY) {
                String valor = fila.get(columna);
                int conteo = contarElementosJson(valor);
                datosVector.add((double) conteo);
            }

            // columna de fecha (normalizada)
            String fechaStr = fila.get(COLUMNA_FECHA);
            double fechaNormalizada = normalizarFecha(fechaStr);
            datosVector.add(fechaNormalizada);

            // crear vector
            double[] datos = datosVector.stream().mapToDouble(Double::doubleValue).toArray();
            Vector v = new Vector(datos, identificador);
            vectores.add(v);
        }

        System.out.println("✓ Vectores creados: " + vectores.size());
        if (vectores.size() > 0) {
            System.out.println("  - Dimensión de cada vector: " + vectores.get(0).dimension());
        }
        return vectores.toArray(new Vector[0]);
    }

    public int getDimensiones() {
        return COLUMNAS_NUMERICAS.length + contarDimensionesOneHot() +
                COLUMNAS_CONTEO.length + COLUMNAS_JSON_ARRAY.length + 1;
    }

    public int getNumeroFilas() {
        return datos.size();
    }

    public String[] getNombresDimensiones() {
        List<String> nombres = new ArrayList<>();

        for (String col : COLUMNAS_NUMERICAS) {
            nombres.add(col);
        }

        for (String columna : COLUMNAS_CATEGORICAS) {
            List<String> categorias = categoriasUnicas.get(columna);
            if (categorias != null) {
                for (String cat : categorias) {
                    nombres.add(columna + "_" + cat);
                }
            }
        }

        for (String col : COLUMNAS_CONTEO) {
            nombres.add(col + "_conteo");
        }

        for (String col : COLUMNAS_JSON_ARRAY) {
            nombres.add(col + "_conteo");
        }

        nombres.add("release_date_normalizada");

        return nombres.toArray(new String[0]);
    }

    public void imprimirEstadisticas() {
        System.out.println("=== Estadísticas del CSV ===");
        System.out.println("Archivo: " + rutaArchivo);
        System.out.println("Películas: " + datos.size());
        System.out.println("Total dimensiones: " + getDimensiones());
        System.out.println();
        System.out.println("Rango de fechas: " + fechaMinima + " a " + fechaMaxima);
    }

    public void imprimirMuestras(int n) {
        System.out.println("=== Primeras " + Math.min(n, datos.size()) + " películas ===");
        Vector[] vectores = getVectores();

        for (int i = 0; i < Math.min(n, vectores.length); i++) {
            System.out.println(vectores[i]);
        }
    }

    @Override
    public String toString() {
        return "CargadorCSV [archivo=" + rutaArchivo + ", películas=" + datos.size() +
                ", dimensiones=" + getDimensiones() + "]";
    }
}

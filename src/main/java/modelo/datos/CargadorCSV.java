package modelo.datos;

import modelo.estructuras.IDiccionario;
import modelo.estructuras.ListaDoble;
import modelo.estructuras.Vector;

import java.io.IOException;

// facade que delega a clases especializadas
public class CargadorCSV {

    private ParserCSV parser;
    private ConfiguradorColumnas configurador;
    private ExtractorCategorias extractorCategorias;
    private NormalizadorFecha normalizadorFechas;
    private TransformadorDatos transformador;
    private NombreDimGen generadorNombres;

    private String rutaArchivo;
    private ListaDoble<IDiccionario<String, String>> datos;

    public CargadorCSV() {
        this.parser = new ParserCSV();
        this.configurador = new ConfiguradorColumnas();
        this.extractorCategorias = new ExtractorCategorias();
        this.normalizadorFechas = new NormalizadorFecha();
        this.rutaArchivo = "";
    }

    public void cargar(String ruta) throws IOException {
        this.rutaArchivo = ruta;

        // 1. Parsear archivo CSV
        parser.parsear(ruta);
        datos = parser.getFilas();

        // 2. Configurar índices de columnas
        configurador.construirIndices(parser.getEncabezados());

        // 3. Extraer categorías únicas para one-hot encoding
        extractorCategorias.extraer(datos, configurador.getColumnasCategoricas());

        // 4. Extraer rango de fechas
        normalizadorFechas.extraerRango(datos, configurador.getColumnaFecha());

        // 5. Crear transformador y generador de nombres
        transformador = new TransformadorDatos(configurador, extractorCategorias, normalizadorFechas);
        generadorNombres = new NombreDimGen(configurador, extractorCategorias);

        // Log del resultado
        int totalDimensiones = generadorNombres.calcularTotalDimensiones();
        System.out.println("✓ CSV cargado: " + datos.tamanio() + " películas");
        System.out.println("  - Dimensiones numéricas: " + configurador.getColumnasNumericas().length);
        System.out.println("  - Dimensiones categóricas (one-hot): " + extractorCategorias.contarDimensionesOneHot());
        System.out.println("  - Dimensiones de conteo: " +
                (configurador.getColumnasConteo().length + configurador.getColumnasJsonArray().length));
        System.out.println("  - Dimensión temporal: 1");
        System.out.println("  - Total dimensiones: " + totalDimensiones);
    }

    public Vector[] getVectores() {
        if (transformador == null) {
            throw new IllegalStateException("Debe cargar un archivo CSV primero");
        }

        Vector[] vectores = transformador.transformar(datos);
        System.out.println("✓ Vectores creados: " + vectores.length);
        if (vectores.length > 0) {
            System.out.println("  - Dimensión de cada vector: " + vectores[0].dimension());
        }
        return vectores;
    }

    public int getDimensiones() {
        if (generadorNombres == null) {
            return 0;
        }
        return generadorNombres.calcularTotalDimensiones();
    }

    public int getNumeroFilas() {
        return datos != null ? datos.tamanio() : 0;
    }

    public String[] getNombresDimensiones() {
        if (generadorNombres == null) {
            return new String[0];
        }
        return generadorNombres.generar();
    }

    public void imprimirEstadisticas() {
        System.out.println("=== Estadísticas del CSV ===");
        System.out.println("Archivo: " + rutaArchivo);
        System.out.println("Películas: " + getNumeroFilas());
        System.out.println("Total dimensiones: " + getDimensiones());
        System.out.println();
        System.out.println("Rango de fechas: " + normalizadorFechas.getFechaMinima() +
                " a " + normalizadorFechas.getFechaMaxima());
    }

    public void imprimirMuestras(int n) {
        System.out.println("=== Primeras " + Math.min(n, getNumeroFilas()) + " películas ===");
        Vector[] vectores = getVectores();

        for (int i = 0; i < Math.min(n, vectores.length); i++) {
            System.out.println(vectores[i]);
        }
    }

    @Override
    public String toString() {
        return "CargadorCSV [archivo=" + rutaArchivo + ", películas=" + getNumeroFilas() +
                ", dimensiones=" + getDimensiones() + "]";
    }
}
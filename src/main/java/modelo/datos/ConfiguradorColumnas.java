package modelo.datos;

import modelo.estructuras.Diccionario;
import modelo.estructuras.IDiccionario;
import java.io.IOException;

// Configurar y mantener información sobre tipos de columnas
public class ConfiguradorColumnas {

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

    private static final String[] COLUMNAS_IGNORAR = {
            "index", "homepage", "id", "original_title", "overview", "tagline", "crew", "director"
    };

    private static final String COLUMNA_IDENTIFICADOR = "title";
    private static final String COLUMNA_FECHA = "release_date";

    private IDiccionario<String, Integer> indicesColumnasNumericas;
    private IDiccionario<String, Integer> indicesColumnasCategoricas;
    private IDiccionario<String, Integer> indicesColumnasConteo;
    private IDiccionario<String, Integer> indicesColumnasJsonArray;
    private int indiceIdentificador;
    private int indiceFecha;

    public ConfiguradorColumnas() {
        this.indicesColumnasNumericas = new Diccionario<>();
        this.indicesColumnasCategoricas = new Diccionario<>();
        this.indicesColumnasConteo = new Diccionario<>();
        this.indicesColumnasJsonArray = new Diccionario<>();
        this.indiceIdentificador = -1;
        this.indiceFecha = -1;
    }

    public void construirIndices(String[] encabezados) throws IOException {
        indicesColumnasNumericas.limpiar();
        indicesColumnasCategoricas.limpiar();
        indicesColumnasConteo.limpiar();
        indicesColumnasJsonArray.limpiar();

        for (int i = 0; i < encabezados.length; i++) {
            String encabezado = encabezados[i].trim();

            if (encabezado.equals(COLUMNA_IDENTIFICADOR)) {
                indiceIdentificador = i;
            }
            if (encabezado.equals(COLUMNA_FECHA)) {
                indiceFecha = i;
            }

            for (String col : COLUMNAS_NUMERICAS) {
                if (encabezado.equals(col)) indicesColumnasNumericas.poner(col, i);
            }

            for (String col : COLUMNAS_CATEGORICAS) {
                if (encabezado.equals(col)) indicesColumnasCategoricas.poner(col, i);
            }

            for (String col : COLUMNAS_CONTEO) {
                if (encabezado.equals(col)) indicesColumnasConteo.poner(col, i);
            }

            for (String col : COLUMNAS_JSON_ARRAY) {
                if (encabezado.equals(col)) indicesColumnasJsonArray.poner(col, i);
            }
        }

        if (indiceIdentificador == -1) {
            throw new IOException("Columna '" + COLUMNA_IDENTIFICADOR + "' no encontrada");
        }
    }

    // getters
    public String[] getColumnasNumericas() {
        return COLUMNAS_NUMERICAS.clone();
    }

    public String[] getColumnasCategoricas() {
        return COLUMNAS_CATEGORICAS.clone();
    }

    public String[] getColumnasConteo() {
        return COLUMNAS_CONTEO.clone();
    }

    public String[] getColumnasJsonArray() {
        return COLUMNAS_JSON_ARRAY.clone();
    }

    public String getColumnaIdentificador() {
        return COLUMNA_IDENTIFICADOR;
    }

    public String getColumnaFecha() {
        return COLUMNA_FECHA;
    }

    public int getIndiceIdentificador() {
        return indiceIdentificador;
    }

    public int getIndiceFecha() {
        return indiceFecha;
    }

    public IDiccionario<String, Integer> getIndicesColumnasNumericas() {
        return indicesColumnasNumericas;
    }

    public IDiccionario<String, Integer> getIndicesColumnasCategoricas() {
        return indicesColumnasCategoricas;
    }

    public IDiccionario<String, Integer> getIndicesColumnasConteo() {
        return indicesColumnasConteo;
    }

    public IDiccionario<String, Integer> getIndicesColumnasJsonArray() {
        return indicesColumnasJsonArray;
    }
}
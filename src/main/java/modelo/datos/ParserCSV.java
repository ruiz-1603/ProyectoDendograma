package modelo.datos;

import modelo.estructuras.Diccionario;
import modelo.estructuras.IDiccionario;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ParserCSV {

    private String[] encabezados;
    private List<IDiccionario<String, String>> filas;

    public ParserCSV() {
        this.encabezados = new String[0];
        this.filas = new ArrayList<>();
    }

    public void parsear(String rutaArchivo) throws IOException {
        this.filas.clear();

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaArchivo), StandardCharsets.UTF_8))) {

            String primeraLinea = lector.readLine();
            if (primeraLinea == null) {
                throw new IOException("Archivo vacío");
            }

            this.encabezados = parsearLinea(primeraLinea);

            String linea;
            int numeroLinea = 2;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                try {
                    String[] valores = parsearLinea(linea);
                    IDiccionario<String, String> fila = construirFila(valores);
                    filas.add(fila);
                } catch (Exception e) {
                    System.err.println("Advertencia línea " + numeroLinea + ": " + e.getMessage());
                }
                numeroLinea++;
            }
        }
    }

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

    private IDiccionario<String, String> construirFila(String[] valores) {
        IDiccionario<String, String> fila = new Diccionario<>();

        for (int i = 0; i < encabezados.length && i < valores.length; i++) {
            fila.poner(encabezados[i].trim(), valores[i].trim());
        }

        // rellenar con vacios si faltan columnas
        for (int i = valores.length; i < encabezados.length; i++) {
            fila.poner(encabezados[i].trim(), "");
        }

        return fila;
    }

    public String[] getEncabezados() {
        return encabezados.clone();
    }

    public List<IDiccionario<String, String>> getFilas() {
        return new ArrayList<>(filas);
    }

    public int getNumeroFilas() {
        return filas.size();
    }
}

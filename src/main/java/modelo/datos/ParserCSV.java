package modelo.datos;

import modelo.estructuras.Diccionario;
import modelo.estructuras.IDiccionario;
import modelo.estructuras.ListaDoble;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Responsabilidad: Leer y parsear archivos CSV
 */
public class ParserCSV {

    private String[] encabezados;
    private ListaDoble<IDiccionario<String, String>> filas;

    public ParserCSV() {
        this.encabezados = new String[0];
        this.filas = new ListaDoble<>();
    }

    public void parsear(String rutaArchivo) throws IOException {
        this.filas.limpiar();

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
                    filas.agregar(fila);
                } catch (Exception e) {
                    System.err.println("Advertencia línea " + numeroLinea + ": " + e.getMessage());
                }
                numeroLinea++;
            }
        }
    }

    private String[] parsearLinea(String linea) {
        ListaDoble<String> campos = new ListaDoble<>();
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
                campos.agregar(campoActual.toString());
                campoActual = new StringBuilder();
            } else {
                campoActual.append(c);
            }

            i++;
        }

        campos.agregar(campoActual.toString());

        // Convertir ListaDoble a array
        String[] resultado = new String[campos.tamanio()];
        for (int j = 0; j < campos.tamanio(); j++) {
            resultado[j] = campos.obtener(j);
        }
        return resultado;
    }

    private IDiccionario<String, String> construirFila(String[] valores) {
        IDiccionario<String, String> fila = new Diccionario<>();

        for (int i = 0; i < encabezados.length && i < valores.length; i++) {
            fila.poner(encabezados[i].trim(), valores[i].trim());
        }

        // Rellenar con vacíos si faltan columnas
        for (int i = valores.length; i < encabezados.length; i++) {
            fila.poner(encabezados[i].trim(), "");
        }

        return fila;
    }

    public String[] getEncabezados() {
        return encabezados.clone();
    }

    public ListaDoble<IDiccionario<String, String>> getFilas() {
        return filas;
    }

    public int getNumeroFilas() {
        return filas.tamanio();
    }
}

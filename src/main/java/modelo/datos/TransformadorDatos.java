package modelo.datos;

import modelo.estructuras.IDiccionario;
import modelo.estructuras.Vector;
import modelo.estructuras.ListaDoble;

public class TransformadorDatos {

    private ConfiguradorColumnas configurador;
    private ExtractorCategorias extractorCategorias;
    private NormalizadorFecha normalizadorFechas;
    private CodificadorCaracteristicas codificador;

    public TransformadorDatos(ConfiguradorColumnas configurador,
                              ExtractorCategorias extractorCategorias,
                              NormalizadorFecha normalizadorFechas) {
        this.configurador = configurador;
        this.extractorCategorias = extractorCategorias;
        this.normalizadorFechas = normalizadorFechas;
        this.codificador = new CodificadorCaracteristicas();
    }

    public Vector[] transformar(ListaDoble<IDiccionario<String, String>> filas) {
        ListaDoble<Vector> vectores = new ListaDoble<>();

        for (int k = 0; k < filas.tamanio(); k++) { // Changed to indexed loop for ListaDoble
            IDiccionario<String, String> fila = filas.obtener(k);
            String identificador = fila.obtener(configurador.getColumnaIdentificador());
            if (identificador == null || identificador.isEmpty()) {
                continue;
            }

            ListaDoble<Double> datosVector = new ListaDoble<>();

            // columnas numéricas directas
            agregarColumnasNumericas(fila, datosVector);

            // columnas categóricas (one-hot)
            agregarColumnasCategoricas(fila, datosVector);

            // columnas de conteo (texto)
            agregarColumnasConteo(fila, datosVector);

            // columnas de JSON array
            agregarColumnasJsonArray(fila, datosVector);

            // columna de fecha (normalizada)
            agregarColumnaFecha(fila, datosVector);

            // crear vector
            double[] datos = new double[datosVector.tamanio()];
            for (int i = 0; i < datosVector.tamanio(); i++) {
                datos[i] = datosVector.obtener(i);
            }
            Vector v = new Vector(datos, identificador);
            vectores.agregar(v);
        }

        Vector[] resultado = new Vector[vectores.tamanio()];
        for (int i = 0; i < vectores.tamanio(); i++) {
            resultado[i] = vectores.obtener(i);
        }
        return resultado;
    }

    private void agregarColumnasNumericas(IDiccionario<String, String> fila, ListaDoble<Double> datosVector) {
        for (String columna : configurador.getColumnasNumericas()) {
            String valor = fila.obtener(columna);
            datosVector.agregar(codificador.parsearNumerico(valor));
        }
    }

    private void agregarColumnasCategoricas(IDiccionario<String, String> fila, ListaDoble<Double> datosVector) {
        for (String columna : configurador.getColumnasCategoricas()) {
            String valor = fila.obtener(columna);
            ListaDoble<String> categorias = extractorCategorias.obtenerCategorias(columna);

            if (categorias != null) {
                double[] oneHot = codificador.codificarOneHot(valor, categorias);
                for (double val : oneHot) {
                    datosVector.agregar(val);
                }
            }
        }
    }

    private void agregarColumnasConteo(IDiccionario<String, String> fila, ListaDoble<Double> datosVector) {
        for (String columna : configurador.getColumnasConteo()) {
            String valor = fila.obtener(columna);
            int conteo = codificador.contarElementos(valor);
            datosVector.agregar((double) conteo);
        }
    }

    private void agregarColumnasJsonArray(IDiccionario<String, String> fila, ListaDoble<Double> datosVector) {
        for (String columna : configurador.getColumnasJsonArray()) {
            String valor = fila.obtener(columna);
            int conteo = codificador.contarElementosJson(valor);
            datosVector.agregar((double) conteo);
        }
    }

    private void agregarColumnaFecha(IDiccionario<String, String> fila, ListaDoble<Double> datosVector) {
        String fechaStr = fila.obtener(configurador.getColumnaFecha());
        double fechaNormalizada = normalizadorFechas.normalizar(fechaStr);
        datosVector.agregar(fechaNormalizada);
    }
}
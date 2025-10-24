package modelo.datos;

import modelo.estructuras.IDiccionario;
import modelo.estructuras.Vector;

import java.util.ArrayList;
import java.util.List;

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

    public Vector[] transformar(List<IDiccionario<String, String>> filas) {
        List<Vector> vectores = new ArrayList<>();

        for (IDiccionario<String, String> fila : filas) {
            String identificador = fila.obtener(configurador.getColumnaIdentificador());
            if (identificador == null || identificador.isEmpty()) {
                continue;
            }

            List<Double> datosVector = new ArrayList<>();

            // 1. Columnas numéricas directas
            agregarColumnasNumericas(fila, datosVector);

            // 2. Columnas categóricas (one-hot)
            agregarColumnasCategoricas(fila, datosVector);

            // 3. Columnas de conteo (texto)
            agregarColumnasConteo(fila, datosVector);

            // 4. Columnas de JSON array
            agregarColumnasJsonArray(fila, datosVector);

            // 5. Columna de fecha (normalizada)
            agregarColumnaFecha(fila, datosVector);

            // Crear vector
            double[] datos = datosVector.stream().mapToDouble(Double::doubleValue).toArray();
            Vector v = new Vector(datos, identificador);
            vectores.add(v);
        }

        return vectores.toArray(new Vector[0]);
    }

    private void agregarColumnasNumericas(IDiccionario<String, String> fila, List<Double> datosVector) {
        for (String columna : configurador.getColumnasNumericas()) {
            String valor = fila.obtener(columna);
            datosVector.add(codificador.parsearNumerico(valor));
        }
    }

    private void agregarColumnasCategoricas(IDiccionario<String, String> fila, List<Double> datosVector) {
        for (String columna : configurador.getColumnasCategoricas()) {
            String valor = fila.obtener(columna);
            List<String> categorias = extractorCategorias.obtenerCategorias(columna);

            if (categorias != null) {
                double[] oneHot = codificador.codificarOneHot(valor, categorias);
                for (double val : oneHot) {
                    datosVector.add(val);
                }
            }
        }
    }

    private void agregarColumnasConteo(IDiccionario<String, String> fila, List<Double> datosVector) {
        for (String columna : configurador.getColumnasConteo()) {
            String valor = fila.obtener(columna);
            int conteo = codificador.contarElementos(valor);
            datosVector.add((double) conteo);
        }
    }

    private void agregarColumnasJsonArray(IDiccionario<String, String> fila, List<Double> datosVector) {
        for (String columna : configurador.getColumnasJsonArray()) {
            String valor = fila.obtener(columna);
            int conteo = codificador.contarElementosJson(valor);
            datosVector.add((double) conteo);
        }
    }

    private void agregarColumnaFecha(IDiccionario<String, String> fila, List<Double> datosVector) {
        String fechaStr = fila.obtener(configurador.getColumnaFecha());
        double fechaNormalizada = normalizadorFechas.normalizar(fechaStr);
        datosVector.add(fechaNormalizada);
    }
}

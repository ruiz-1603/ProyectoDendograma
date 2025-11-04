package modelo.datos;

import modelo.estructuras.IDiccionario;
import modelo.estructuras.Vector;
import modelo.estructuras.ListaDoble;
import modelo.estructuras.Diccionario;
import modelo.normalizacion.FactoryNormalizacion;
import modelo.normalizacion.INormalizacion;

public class TransformadorDatos {

    private ConfiguradorColumnas configurador;
    private ExtractorCategorias extractorCategorias;
    private NormalizadorFecha normalizadorFechas;
    private CodificadorCaracteristicas codificador;

    private ListaDoble<VariableConfig> configs;
    private String[] nombresColumnas;

    public TransformadorDatos(ConfiguradorColumnas configurador,
                              ExtractorCategorias extractorCategorias,
                              NormalizadorFecha normalizadorFechas) {
        this.configurador = configurador;
        this.extractorCategorias = extractorCategorias;
        this.normalizadorFechas = normalizadorFechas;
        this.codificador = new CodificadorCaracteristicas();
    }

    public TransformadorDatos(ListaDoble<VariableConfig> configs, String[] nombresColumnas) {
        this.configs = configs;
        this.nombresColumnas = nombresColumnas;
    }

    public Vector[] normalizarPorVariable(Vector[] vectores) {
        if (vectores == null || vectores.length == 0) {
            return new Vector[0];
        }

        Vector[] vectoresNormalizados = new Vector[vectores.length];
        for (int i = 0; i < vectores.length; i++) {
            vectoresNormalizados[i] = new Vector(vectores[i]);
        }

        IDiccionario<String, Integer> mapaNombresAIndices = new Diccionario<>();
        for (int i = 0; i < nombresColumnas.length; i++) {
            mapaNombresAIndices.poner(nombresColumnas[i], i);
        }

        IDiccionario<String, ListaDoble<Integer>> indicesPorMetodo = new Diccionario<>();
        for (int i = 0; i < configs.tamanio(); i++) {
            VariableConfig config = configs.obtener(i);
            if (config.isSeleccionada() && !"Ninguno".equals(config.getMetodoNormalizacion()) && "Numérico".equals(config.getTipoDato())) {
                String metodo = config.getMetodoNormalizacion();
                if (indicesPorMetodo.obtener(metodo) == null) {
                    indicesPorMetodo.poner(metodo, new ListaDoble<>());
                }
                Integer indice = mapaNombresAIndices.obtener(config.getNombre());
                if (indice != null) {
                    indicesPorMetodo.obtener(metodo).agregar(indice);
                }
            }
        }

        ListaDoble<String> metodos = indicesPorMetodo.conjuntoClaves();
        for (int i = 0; i < metodos.tamanio(); i++) {
            String metodo = metodos.obtener(i);
            ListaDoble<Integer> indices = indicesPorMetodo.obtener(metodo);
            int numColumnas = indices.tamanio();

            if (numColumnas == 0) continue;

            Vector[] subVectores = new Vector[vectores.length];
            for (int j = 0; j < vectores.length; j++) {
                double[] subDatos = new double[numColumnas];
                for (int k = 0; k < numColumnas; k++) {
                    subDatos[k] = vectores[j].getPosicion(indices.obtener(k));
                }
                subVectores[j] = new Vector(subDatos, "");
            }

            INormalizacion estrategia = FactoryNormalizacion.crear(metodo);
            Vector[] subVectoresNormalizados = estrategia.normalizar(subVectores);

            for (int j = 0; j < vectoresNormalizados.length; j++) {
                for (int k = 0; k < numColumnas; k++) {
                    double valorNormalizado = subVectoresNormalizados[j].getPosicion(k);
                    vectoresNormalizados[j].setValor(indices.obtener(k), valorNormalizado);
                }
            }
        }

        return vectoresNormalizados;
    }

    public Vector[] transformar(ListaDoble<IDiccionario<String, String>> filas) {
        ListaDoble<Vector> vectores = new ListaDoble<>();

        for (int k = 0; k < filas.tamanio(); k++) {
            IDiccionario<String, String> fila = filas.obtener(k);
            String identificador = fila.obtener(configurador.getColumnaIdentificador());
            if (identificador == null || identificador.isEmpty()) {
                continue;
            }

            ListaDoble<Double> datosVector = new ListaDoble<>();

            agregarColumnasNumericas(fila, datosVector);
            agregarColumnasCategoricas(fila, datosVector);
            agregarColumnasConteo(fila, datosVector);
            agregarColumnasJsonArray(fila, datosVector);
            agregarColumnaFecha(fila, datosVector);

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
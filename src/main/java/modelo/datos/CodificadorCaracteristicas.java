package modelo.datos;

import modelo.estructuras.ListaDoble;

/**
 * Responsabilidad: Codificar características (one-hot encoding, conteos)
 */
public class CodificadorCaracteristicas {

    public double[] codificarOneHot(String valor, ListaDoble<String> categorias) {
        if (valor == null || valor.isEmpty() || valor.equals("null")) {
            valor = "desconocido";
        }

        double[] codificado = new double[categorias.tamanio()];
        for (int i = 0; i < categorias.tamanio(); i++) {
            codificado[i] = valor.equals(categorias.obtener(i)) ? 1.0 : 0.0;
        }
        return codificado;
    }

    public int contarElementos(String texto) {
        if (texto == null || texto.isEmpty() || texto.equals("null")) {
            return 0;
        }

        // Dividir por espacios o comas
        String[] elementos = texto.split("[,\\s]+");
        return elementos.length;
    }

    public int contarElementosJson(String json) {
        if (json == null || json.isEmpty() || json.equals("null")) {
            return 0;
        }

        // Contar llaves abiertas (cada objeto es un elemento)
        return json.split("\\{").length - 1;
    }

    public double parsearNumerico(String valor) {
        if (valor == null || valor.isEmpty() || valor.equals("null")) {
            return 0.0;
        }

        try {
            double num = Double.parseDouble(valor);
            return num < 0 ? 0.0 : num;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
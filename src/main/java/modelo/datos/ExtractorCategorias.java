package modelo.datos;

import modelo.estructuras.Diccionario;
import modelo.estructuras.IDiccionario;
import modelo.estructuras.ListaDoble;

public class ExtractorCategorias {

    private IDiccionario<String, ListaDoble<String>> categoriasUnicas;

    public ExtractorCategorias() {
        this.categoriasUnicas = new Diccionario<>();
    }

    public void extraer(ListaDoble<IDiccionario<String, String>> filas, String[] columnasCategoricas) {
        categoriasUnicas.limpiar();

        for (String columna : columnasCategoricas) {
            IDiccionario<String, Boolean> unicos = new Diccionario<>();

            for (int i = 0; i < filas.tamanio(); i++) {
                IDiccionario<String, String> fila = filas.obtener(i);
                String valor = fila.obtener(columna);
                if (valor != null && !valor.isEmpty() && !valor.equals("null")) {
                    unicos.poner(valor.trim(), true);
                }
            }

            // si no hay valores unicos
            if (unicos.tamanio() == 0) {
                unicos.poner("desconocido", true);
            }

            // convertir a ListaDoble ordenada
            ListaDoble<String> listaUnicos = new ListaDoble<>();
            ListaDoble<String> claves = unicos.conjuntoClaves();

            // ordenar las categorias
            ListaDoble<String> clavesOrdenadas = ordenarCategorias(claves);
            for (int i = 0; i < clavesOrdenadas.tamanio(); i++) {
                listaUnicos.agregar(clavesOrdenadas.obtener(i));
            }

            categoriasUnicas.poner(columna, listaUnicos);
        }
    }

    private ListaDoble<String> ordenarCategorias(ListaDoble<String> categorias) {
        ListaDoble<String> ordenadas = new ListaDoble<>();

        // copiar todas las categorias
        for (int i = 0; i < categorias.tamanio(); i++) {
            ordenadas.agregar(categorias.obtener(i));
        }

        // ordenar usando el comparador de ListaDoble
        ordenadas.ordenar(new ListaDoble.Comparador<String>() {
            @Override
            public int comparar(String a, String b) {
                return a.compareTo(b);
            }
        });

        return ordenadas;
    }

    public ListaDoble<String> obtenerCategorias(String columna) {
        return categoriasUnicas.obtener(columna);
    }

    public int contarDimensionesOneHot() {
        int total = 0;
        ListaDoble<String> claves = categoriasUnicas.conjuntoClaves();

        for (int i = 0; i < claves.tamanio(); i++) {
            String columna = claves.obtener(i);
            ListaDoble<String> categorias = categoriasUnicas.obtener(columna);
            if (categorias == null) {
                categorias = new ListaDoble<>();
            }
            total += categorias.tamanio();
        }
        return total;
    }

    public IDiccionario<String, ListaDoble<String>> getCategoriasUnicas() {
        return categoriasUnicas;
    }
}
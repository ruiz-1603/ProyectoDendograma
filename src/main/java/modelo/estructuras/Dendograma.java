package modelo.estructuras;

public class Dendograma {
    public String toJSON(Nodo raiz) {
        return toJSONRec(raiz,0);
    }

    private String toJSONRec(Nodo nodo, int nivel) {
        if (nodo == null) return "";

        String indent = repetirEspacios(nivel * 2);
        StringBuilder sb = new StringBuilder();

        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"n\": \"").append(nodo.getNombre()).append("\",\n");
        sb.append(indent).append("  \"d\": ").append(nodo.getDistancia()).append(",\n");
        sb.append(indent).append("  \"c\": ");

        if (nodo.esHoja()) {
            sb.append("[]");
        } else {
            sb.append("[\n");
            sb.append(toJSONRec(nodo.getIzquierdo(), nivel + 2)).append(",\n");
            sb.append(toJSONRec(nodo.getDerecho(), nivel + 2)).append("\n");
            sb.append(indent).append("  ]");
        }

        sb.append("\n").append(indent).append("}");
        return sb.toString();
    }

    private String repetirEspacios(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(" ");
        return sb.toString();
    }

    public int contarHojas(Nodo nodo) {
        if (nodo == null) return 0;
        if (nodo.esHoja()) return 1;
        return contarHojas(nodo.getIzquierdo()) + contarHojas(nodo.getDerecho());
    }

    public int altura(Nodo nodo) {
        if (nodo == null) return 0;
        if (nodo.esHoja()) return 0;
        return 1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho()));
    }

    public String[] obtenerEtiquetasHojas(Nodo nodo) {
        ListaDoble<String> lista = new ListaDoble<>();
        obtenerEtiquetasHojasRec(nodo, lista);

        Object[] arr = lista.aArreglo();
        String[] res = new String[arr.length];
        for (int i = 0; i < arr.length; i++) res[i] = (String) arr[i];
        return res;
    }

    private void obtenerEtiquetasHojasRec(Nodo nodo, ListaDoble<String> lista) {
        if (nodo == null) return;

        if (nodo.esHoja()) {
            ListaDoble.IteradorLista<String> it = nodo.getElementos().iterador();
            while (it.tieneSiguiente()) {
                lista.agregar(it.siguiente());
            }
        } else {
            obtenerEtiquetasHojasRec(nodo.getIzquierdo(), lista);
            obtenerEtiquetasHojasRec(nodo.getDerecho(), lista);
        }
    }

    public String toStringArbol(Nodo nodo) {
        return toStringArbolRec(nodo, 0);
    }

    private String toStringArbolRec(Nodo nodo, int nivel) {
        if (nodo == null) return "";
        StringBuilder sb = new StringBuilder();
        String indent = "  ".repeat(nivel);

        sb.append(indent)
                .append(nodo.getNombre())
                .append(" [d=")
                .append(String.format("%.2f", nodo.getDistancia()))
                .append("]\n");

        if (!nodo.esHoja()) {
            sb.append(toStringArbolRec(nodo.getIzquierdo(), nivel + 1));
            sb.append(toStringArbolRec(nodo.getDerecho(), nivel + 1));
        }

        return sb.toString();
    }


    public ListaDoble<Nodo> cortarArbol(Nodo raiz, int k) {
        if (k < 1) {
            throw new IllegalArgumentException("K debe ser al menos 1.");
        }
        if (k > contarHojas(raiz)) {
            throw new IllegalArgumentException("K no puede ser mayor que el número de elementos.");
        }

        ListaDoble<Nodo> lista = new ListaDoble<>();
        lista.agregar(raiz);

        while (lista.tamanio() < k) {
            Nodo mayor = obtenerMayorDistancia(lista);
            if (mayor == null || mayor.esHoja()) break;

            lista.eliminarElemento(mayor);
            lista.agregar(mayor.getIzquierdo());
            lista.agregar(mayor.getDerecho());
        }

        return lista;
    }

    private Nodo obtenerMayorDistancia(ListaDoble<Nodo> lista) {
        if (lista.estaVacia()) return null;

        Nodo max = lista.obtener(0);
        for (int i = 1; i < lista.tamanio(); i++) {
            Nodo actual = lista.obtener(i);
            if (actual.getDistancia() > max.getDistancia()) {
                max = actual;
            }
        }
        return max;
    }

    public ListaDoble<Nodo> cortarPorDistancia(Nodo nodo, double umbral) {
        ListaDoble<Nodo> clusters = new ListaDoble<>();
        cortarPorDistanciaRec(nodo, umbral, clusters);
        return clusters;
    }

    private void cortarPorDistanciaRec(Nodo nodo, double umbral, ListaDoble<Nodo> clusters) {
        if (nodo == null) return;

        if (nodo.getDistancia() > umbral && !nodo.esHoja()) {
            cortarPorDistanciaRec(nodo.getIzquierdo(), umbral, clusters);
            cortarPorDistanciaRec(nodo.getDerecho(), umbral, clusters);
        } else {
            clusters.agregar(nodo);
        }
    }
}
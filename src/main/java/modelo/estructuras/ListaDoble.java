package modelo.estructuras;


public class ListaDoble<T> {

    private NodoLista<T> cabeza;
    private NodoLista<T> cola;
    private int tamanio;

    public ListaDoble() {
        this.cabeza = null;
        this.cola = null;
        this.tamanio = 0;
    }

    public void agregar(T elemento) {
        NodoLista<T> nuevo = new NodoLista<>(elemento);

        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            nuevo.setAnterior(cola);
            cola = nuevo;
        }
        tamanio++;
    }

    public void agregarAlInicio(T elemento) {
        NodoLista<T> nuevo = new NodoLista<>(elemento);

        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            nuevo.setSiguiente(cabeza);
            cabeza.setAnterior(nuevo);
            cabeza = nuevo;
        }
        tamanio++;
    }

    private T obtenerRecursivo(NodoLista<T> nodo, int indice) {
        if (indice == 0) return nodo.getDato();
        return obtenerRecursivo(nodo.getSiguiente(), indice - 1);
    }

    public T obtener(int indice) {
        if (indice < 0 || indice >= tamanio)
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        return obtenerRecursivo(cabeza, indice);
    }

    public boolean eliminarElemento(T elemento) {
        if (cabeza == null) return false;
        return eliminarRecursivo(cabeza, elemento);
    }

    private boolean eliminarRecursivo(NodoLista<T> nodo, T elemento) {
        if (nodo == null) return false;

        if (nodo.getDato().equals(elemento)) {
            if (nodo == cabeza) {
                cabeza = nodo.getSiguiente();
                if (cabeza != null)
                    cabeza.setAnterior(null);
                else
                    cola = null;
            }
            else if (nodo == cola) {
                cola = nodo.getAnterior();
                if (cola != null)
                    cola.setSiguiente(null);
            }
            else {
                nodo.getAnterior().setSiguiente(nodo.getSiguiente());
                nodo.getSiguiente().setAnterior(nodo.getAnterior());
            }

            tamanio--;
            return true;
        }

        return eliminarRecursivo(nodo.getSiguiente(), elemento);
    }

    private boolean contieneRec(NodoLista<T> nodo, T elemento) {
        if (nodo == null) return false;
        if (nodo.getDato().equals(elemento)) return true;
        return contieneRec(nodo.getSiguiente(), elemento);
    }

    public boolean contiene(T elemento) {
        return contieneRec(cabeza, elemento);
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public void limpiar() {
        cabeza = null;
        cola = null;
        tamanio = 0;
    }

    public Object[] aArreglo() {
        Object[] arreglo = new Object[tamanio];
        NodoLista<T> actual = cabeza;
        int i = 0;

        while (actual != null) {
            arreglo[i++] = actual.getDato();
            actual = actual.getSiguiente();
        }

        return arreglo;
    }

    public void ordenar(Comparador<T> comparador) {
        if (tamanio <= 1) return;

        cabeza = mergeSort(cabeza, comparador);

        NodoLista<T> actual = cabeza;
        while (actual.getSiguiente() != null) {
            actual = actual.getSiguiente();
        }
        cola = actual;
    }

    private NodoLista<T> mergeSort(NodoLista<T> nodo, Comparador<T> comparador) {
        if (nodo == null || nodo.getSiguiente() == null) {
            return nodo;
        }

        NodoLista<T> medio = obtenerMedio(nodo);
        NodoLista<T> mitadDerecha = medio.getSiguiente();
        medio.setSiguiente(null);

        NodoLista<T> izquierda = mergeSort(nodo, comparador);
        NodoLista<T> derecha = mergeSort(mitadDerecha, comparador);

        return merge(izquierda, derecha, comparador);
    }

    private NodoLista<T> obtenerMedio(NodoLista<T> nodo) {
        if (nodo == null) return nodo;

        NodoLista<T> lento = nodo;
        NodoLista<T> rapido = nodo.getSiguiente();

        while (rapido != null && rapido.getSiguiente() != null) {
            lento = lento.getSiguiente();
            rapido = rapido.getSiguiente().getSiguiente();
        }

        return lento;
    }

    private NodoLista<T> merge(NodoLista<T> izq, NodoLista<T> der, Comparador<T> comparador) {
        if (izq == null) return der;
        if (der == null) return izq;

        NodoLista<T> resultado;

        if (comparador.comparar(izq.getDato(), der.getDato()) <= 0) {
            resultado = izq;
            resultado.setSiguiente(merge(izq.getSiguiente(), der, comparador));
            if (resultado.getSiguiente() != null) {
                resultado.getSiguiente().setAnterior(resultado);
            }
        } else {
            resultado = der;
            resultado.setSiguiente(merge(izq, der.getSiguiente(), comparador));
            if (resultado.getSiguiente() != null) {
                resultado.getSiguiente().setAnterior(resultado);
            }
        }

        return resultado;
    }

    public IteradorLista<T> iterador() {
        return new IteradorLista<>(cabeza);
    }

    public static class IteradorLista<T> {
        private NodoLista<T> actual;

        public IteradorLista(NodoLista<T> inicio) {
            this.actual = inicio;
        }

        public boolean tieneSiguiente() {
            return actual != null;
        }

        public T siguiente() {
            if (!tieneSiguiente()) {
                throw new IllegalStateException("No hay más elementos");
            }
            T dato = actual.getDato();
            actual = actual.getSiguiente();
            return dato;
        }
    }

    public interface Comparador<T> {
        int comparar(T a, T b);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        NodoLista<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.getDato());
            if (actual.getSiguiente() != null) {
                sb.append(", ");
            }
            actual = actual.getSiguiente();
        }

        sb.append("]");
        return sb.toString();
    }
}
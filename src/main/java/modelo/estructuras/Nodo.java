package modelo.estructuras;

public class Nodo {

    private Nodo izquierdo;
    private Nodo derecho;
    private double distancia;
    private ListaDoble<String> elementos;
    private String nombre; // Nuevo campo para el nombre

    public Nodo(String elemento) {
        this.elementos = new ListaDoble<>();
        this.elementos.agregar(elemento);
        this.distancia = 0.0;
        this.izquierdo = null;
        this.derecho = null;
        this.nombre = elemento; // Para hojas, el nombre es el elemento
    }

    public Nodo(Nodo izquierdo, Nodo derecho, double distancia) {
        this.izquierdo = izquierdo;
        this.derecho = derecho;
        this.distancia = distancia;
        this.elementos = new ListaDoble<>();
        ListaDoble.IteradorLista<String> it1 = izquierdo.getElementos().iterador();
        while (it1.tieneSiguiente()) this.elementos.agregar(it1.siguiente());

        ListaDoble.IteradorLista<String> it2 = derecho.getElementos().iterador();
        while (it2.tieneSiguiente()) this.elementos.agregar(it2.siguiente());
        this.nombre = null; // Por defecto, los nodos internos no tienen nombre explícito
    }

    public boolean esHoja() {
        return izquierdo == null && derecho == null;
    }

    public String getNombre() {
        if (nombre != null) {
            return nombre;
        }
        return "(" + elementos.toString().replace(", ", ";") + ")";
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Nodo getIzquierdo() {
        return izquierdo;
    }

    public Nodo getDerecho() {
        return derecho;
    }

    public double getDistancia() {
        return distancia;
    }

    public ListaDoble<String> getElementos() {
        return elementos;
    }

    @Override
    public String toString() {
        return getNombre() + " [d=" + String.format("%.2f", distancia)
                + ", elementos=" + elementos.tamanio() + "]";
    }
}
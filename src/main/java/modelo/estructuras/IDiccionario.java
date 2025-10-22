package modelo.estructuras;

public interface IDiccionario<K, V> {
    V poner(K clave, V valor);
    V obtener(K clave);
    V eliminar(K clave);
    boolean contieneClave(K clave);
    int tamanio();
    boolean estaVacio();
    void limpiar();
    ListaDoble<K> conjuntoClaves();
    ListaDoble<V> coleccionValores();
    ListaDoble<Entrada<K, V>> conjuntoEntradas();

    interface Entrada<K, V> {
        K obtenerClave();
        V obtenerValor();
        V establecerValor(V valor);
    }
}

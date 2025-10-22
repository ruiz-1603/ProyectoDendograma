package modelo.estructuras;

public class Diccionario<K, V> implements IDiccionario<K, V> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA = 0.75;

    private ListaDoble<Entrada<K, V>>[] tabla;
    private int tamanioActual;
    private int umbralRedimensionamiento;

    public Diccionario() {
        this(CAPACIDAD_INICIAL);
    }

    public Diccionario(int capacidadInicial) {
        if (capacidadInicial < 0) {
            throw new IllegalArgumentException("La capacidad inicial no puede ser negativa.");
        }
        this.tabla = new ListaDoble[capacidadInicial];
        for (int i = 0; i < capacidadInicial; i++) {
            tabla[i] = new ListaDoble<>();
        }
        this.tamanioActual = 0;
        this.umbralRedimensionamiento = (int) (capacidadInicial * FACTOR_CARGA);
    }

    // Implementación de la interfaz Entrada
    private static class EntradaImpl<K, V> implements Entrada<K, V> {
        private K clave;
        private V valor;

        public EntradaImpl(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }

        @Override
        public K getClave() {
            return clave;
        }

        @Override
        public V getValor() {
            return valor;
        }

        @Override
        public V setValor(V valor) {
            V valorAntiguo = this.valor;
            this.valor = valor;
            return valorAntiguo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntradaImpl<?, ?> entrada = (EntradaImpl<?, ?>) o;
            return clave.equals(entrada.clave);
        }

        @Override
        public int hashCode() {
            return clave.hashCode();
        }
    }

    private int calcularIndice(K clave) {
        return (clave == null) ? 0 : Math.abs(clave.hashCode() % tabla.length);
    }

    @Override
    public V poner(K clave, V valor) {
        int indice = calcularIndice(clave);
        ListaDoble<Entrada<K, V>> cubo = tabla[indice];

        for (int i = 0; i < cubo.tamanio(); i++) {
            Entrada<K, V> entrada = cubo.obtener(i);
            if (entrada.getClave().equals(clave)) {
                return entrada.setValor(valor);
            }
        }

        cubo.agregar(new EntradaImpl<>(clave, valor));
        tamanioActual++;

        if (tamanioActual > umbralRedimensionamiento) {
            redimensionar();
        }
        return null; // Indica que no había un valor anterior
    }

    @Override
    public V obtener(K clave) {
        int indice = calcularIndice(clave);
        ListaDoble<Entrada<K, V>> cubo = tabla[indice];

        for (int i = 0; i < cubo.tamanio(); i++) {
            Entrada<K, V> entrada = cubo.obtener(i);
            if (entrada.getClave().equals(clave)) {
                return entrada.getValor();
            }
        }
        return null;
    }

    @Override
    public V eliminar(K clave) {
        int indice = calcularIndice(clave);
        ListaDoble<Entrada<K, V>> cubo = tabla[indice];

        for (int i = 0; i < cubo.tamanio(); i++) {
            Entrada<K, V> entrada = cubo.obtener(i);
            if (entrada.getClave().equals(clave)) {
                cubo.eliminarElemento(entrada);
                tamanioActual--;
                return entrada.getValor();
            }
        }
        return null;
    }

    @Override
    public boolean contieneClave(K clave) {
        int indice = calcularIndice(clave);
        ListaDoble<Entrada<K, V>> cubo = tabla[indice];

        for (int i = 0; i < cubo.tamanio(); i++) {
            Entrada<K, V> entrada = cubo.obtener(i);
            if (entrada.getClave().equals(clave)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int tamanio() {
        return tamanioActual;
    }

    @Override
    public boolean estaVacio() {
        return tamanioActual == 0;
    }

    @Override
    public ListaDoble<K> conjuntoClaves() {
        ListaDoble<K> claves = new ListaDoble<>();
        for (ListaDoble<Entrada<K, V>> cubo : tabla) {
            for (int i = 0; i < cubo.tamanio(); i++) {
                claves.agregar(cubo.obtener(i).getClave());
            }
        }
        return claves;
    }

    @Override
    public ListaDoble<V> coleccionValores() {
        ListaDoble<V> valores = new ListaDoble<>();
        for (ListaDoble<Entrada<K, V>> cubo : tabla) {
            for (int i = 0; i < cubo.tamanio(); i++) {
                valores.agregar(cubo.obtener(i).getValor());
            }
        }
        return valores;
    }

    @Override
    public ListaDoble<Entrada<K, V>> conjuntoEntradas() {
        ListaDoble<Entrada<K, V>> entradas = new ListaDoble<>();
        for (ListaDoble<Entrada<K, V>> cubo : tabla) {
            for (int i = 0; i < cubo.tamanio(); i++) {
                entradas.agregar(cubo.obtener(i));
            }
        }
        return entradas;
    }

    private void redimensionar() {
        int nuevaCapacidad = tabla.length * 2;
        ListaDoble<Entrada<K, V>>[] tablaAntigua = tabla;
        this.tabla = new ListaDoble[nuevaCapacidad];
        for (int i = 0; i < nuevaCapacidad; i++) {
            tabla[i] = new ListaDoble<>();
        }
        this.tamanioActual = 0;
        this.umbralRedimensionamiento = (int) (nuevaCapacidad * FACTOR_CARGA);

        for (ListaDoble<Entrada<K, V>> cubo : tablaAntigua) {
            for (int i = 0; i < cubo.tamanio(); i++) {
                Entrada<K, V> entrada = cubo.obtener(i);
                poner(entrada.getClave(), entrada.getValor());
            }
        }
    }

    @Override
    public void limpiar() {
        for (int i = 0; i < tabla.length; i++) {
            tabla[i] = new ListaDoble<>(); // Clear each bucket
        }
        tamanioActual = 0;
    }
}

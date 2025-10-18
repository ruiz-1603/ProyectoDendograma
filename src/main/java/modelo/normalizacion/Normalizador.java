package modelo.normalizacion;

import modelo.estructuras.*;

public class Normalizador {

    private INormalizacion estrategia;

    /**
     * Constructor con estrategia
     * Complejidad: O(1)
     */
    public Normalizador(INormalizacion estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia no puede ser null");
        }
        this.estrategia = estrategia;
    }

    /**
     * Constructor con tipo enum
     * Complejidad: O(1)
     */
    public Normalizador(FactoryNormalizacion.TipoNormalizacion tipo) {
        this.estrategia = FactoryNormalizacion.crear(tipo);
    }

    /**
     * Constructor con nombre (string)
     * Complejidad: O(1)
     */
    public Normalizador(String nombreEstrategia) {
        this.estrategia = FactoryNormalizacion.crear(nombreEstrategia);
    }

    /**
     * Normaliza los vectores usando la estrategia actual
     * Complejidad: O(n*m)
     */
    public Vector[] normalizar(Vector[] vectores) {
        return estrategia.normalizar(vectores);
    }

    /**
     * Cambia la estrategia de normalización
     * Complejidad: O(1)
     */
    public void setEstrategia(INormalizacion nueva) {
        if (nueva == null) {
            throw new IllegalArgumentException("La estrategia no puede ser null");
        }
        this.estrategia = nueva;
    }

    /**
     * Cambia la estrategia por tipo
     * Complejidad: O(1)
     */
    public void setEstrategia(FactoryNormalizacion.TipoNormalizacion tipo) {
        this.estrategia = FactoryNormalizacion.crear(tipo);
    }

    /**
     * Cambia la estrategia por nombre
     * Complejidad: O(1)
     */
    public void setEstrategia(String nombreEstrategia) {
        this.estrategia = FactoryNormalizacion.crear(nombreEstrategia);
    }

    /**
     * Obtiene la estrategia actual
     * Complejidad: O(1)
     */
    public INormalizacion obtenerEstrategia() {
        return estrategia;
    }

    /**
     * Obtiene nombre de la estrategia actual
     * Complejidad: O(1)
     */
    public String obtenerNombreEstrategia() {
        return estrategia.getNombre();
    }

    /**
     * Imprime información de la normalización
     * Complejidad: O(1)
     */
    public void imprimir() {
        System.out.println("=== Normalizador ===");
        System.out.println("Estrategia: " + estrategia);
    }

    @Override
    public String toString() {
        return "Normalizador [" + estrategia.getNombre() + "]";
    }
}

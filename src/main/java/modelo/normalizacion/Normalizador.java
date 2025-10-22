package modelo.normalizacion;

import modelo.estructuras.*;

public class Normalizador {

    private INormalizacion estrategia;

    public Normalizador(INormalizacion estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia no puede ser null");
        }
        this.estrategia = estrategia;
    }

    public Normalizador(FactoryNormalizacion.TipoNormalizacion tipo) {
        this.estrategia = FactoryNormalizacion.crear(tipo);
    }

    public Normalizador(String nombreEstrategia) {
        this.estrategia = FactoryNormalizacion.crear(nombreEstrategia);
    }

    public Vector[] normalizar(Vector[] vectores) {
        return estrategia.normalizar(vectores);
    }

    public void setEstrategia(INormalizacion nueva) {
        if (nueva == null) {
            throw new IllegalArgumentException("La estrategia no puede ser null");
        }
        this.estrategia = nueva;
    }

    public void setEstrategia(FactoryNormalizacion.TipoNormalizacion tipo) {
        this.estrategia = FactoryNormalizacion.crear(tipo);
    }

    public void setEstrategia(String nombreEstrategia) {
        this.estrategia = FactoryNormalizacion.crear(nombreEstrategia);
    }

    public INormalizacion obtenerEstrategia() {
        return estrategia;
    }

    public String obtenerNombreEstrategia() {
        return estrategia.getNombre();
    }

    public void imprimir() {
        System.out.println("=== Normalizador ===");
        System.out.println("Estrategia: " + estrategia);
    }

    @Override
    public String toString() {
        return "Normalizador [" + estrategia.getNombre() + "]";
    }
}

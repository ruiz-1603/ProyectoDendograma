package modelo.normalizacion;

import modelo.estructuras.Vector;

public interface INormalizacion {

    Vector[] normalizar(Vector[] vectores);
    String getNombre();
}

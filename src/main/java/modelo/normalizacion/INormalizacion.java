package modelo.normalizacion;

import modelo.estructuras.*;

public interface INormalizacion {

    Vector[] normalizar(Vector[] vectores);
    String getNombre();
}

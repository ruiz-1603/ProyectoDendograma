package modelo.distancias;

import modelo.estructuras.*;

public interface ICalculadorDistancia {

    double calcular(Vector v1, Vector v2);
    String getNombre();
}
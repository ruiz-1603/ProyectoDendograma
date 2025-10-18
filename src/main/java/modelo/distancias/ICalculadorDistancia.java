package modelo.distancias;

import modelo.estructuras.Vector;

public interface ICalculadorDistancia {

    double calcular(Vector v1, Vector v2);

    /**
     * Retorna el nombre del tipo de distancia
     * @return Nombre descriptivo
     */
    String getNombre();
}
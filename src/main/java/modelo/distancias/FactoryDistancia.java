package modelo.distancias;

public class FactoryDistancia {

    public enum TipoDistancia {
        EUCLIDIANA,
        MANHATTAN,
        COSENO,
        HAMMING
    }

    public static ICalculadorDistancia crear(TipoDistancia tipo) {
        switch (tipo) {
            case EUCLIDIANA:
                return new DistanciaEuclidiana();

            case MANHATTAN:
                return new DistanciaManhattan();

            case COSENO:
                return new DistanciaCoseno();

            case HAMMING:
                return new DistanciaHamming();

            default:
                throw new IllegalArgumentException(
                        "Tipo de distancia no reconocido: " + tipo
                );
        }
    }

    public static ICalculadorDistancia crear(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre no puede ser null");
        }

        String nombreNormalizado = nombre.trim().toUpperCase();

        switch (nombreNormalizado) {
            case "EUCLIDIANA":
            case "EUCLIDIAN":
            case "L2":
                return new DistanciaEuclidiana();

            case "MANHATTAN":
            case "L1":
            case "TAXICAB":
            case "CITY BLOCK":
                return new DistanciaManhattan();

            case "COSENO":
            case "COSINE":
            case "ANGULAR":
                return new DistanciaCoseno();

            case "HAMMING":
                return new DistanciaHamming();

            default:
                throw new IllegalArgumentException(
                        "Tipo de distancia no reconocido: " + nombre +
                                ". Opciones válidas: Euclidiana, Manhattan, Coseno, Hamming"
                );
        }
    }

}
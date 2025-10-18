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

    public static TipoDistancia[] getTiposDisponibles() {
        return TipoDistancia.values();
    }

    public static String[] obtenerNombresDisponibles() {
        TipoDistancia[] tipos = TipoDistancia.values();
        String[] nombres = new String[tipos.length];

        for (int i = 0; i < tipos.length; i++) {
            ICalculadorDistancia calc = crear(tipos[i]);
            nombres[i] = calc.getNombre();
        }

        return nombres;
    }

    public static boolean esNombreValido(String nombre) {
        try {
            crear(nombre);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
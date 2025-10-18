package modelo.normalizacion;

public class FactoryNormalizacion {

    public enum TipoNormalizacion {
        MIN_MAX,
        Z_SCORE,
        LOGARITMICA
    }

    /**
     * Crea una estrategia de normalización por tipo enum
     * Complejidad: O(1)
     */
    public static INormalizacion crear(TipoNormalizacion tipo) {
        switch (tipo) {
            case MIN_MAX:
                return new MinMaxNormalizacion();
            case Z_SCORE:
                return new ZScoreNormalizacion();
            case LOGARITMICA:
                return new LogaritmicaNormalizacion();
            default:
                throw new IllegalArgumentException("Tipo de normalización no soportado: " + tipo);
        }
    }

    /**
     * Crea una estrategia desde un nombre (string)
     * Complejidad: O(1)
     */
    public static INormalizacion crear(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre no puede ser null");
        }

        String nombreNormalizado = nombre.trim().toUpperCase();

        switch (nombreNormalizado) {
            case "MIN-MAX":
            case "MINMAX":
            case "MIN_MAX":
                return new MinMaxNormalizacion();

            case "Z-SCORE":
            case "ZSCORE":
            case "Z_SCORE":
                return new ZScoreNormalizacion();

            case "LOGARITMICA":
            case "LOGARITHMIC":
            case "LOG":
                return new LogaritmicaNormalizacion();

            default:
                throw new IllegalArgumentException(
                        "Tipo de normalización no reconocido: " + nombre +
                                ". Opciones: MIN-MAX, Z-SCORE, LOGARITMICA"
                );
        }
    }

    /**
     * Retorna todos los tipos disponibles
     * Complejidad: O(1)
     */
    public static TipoNormalizacion[] getTiposDisponibles() {
        return TipoNormalizacion.values();
    }

    /**
     * Retorna nombres de todas las estrategias
     * Complejidad: O(m) donde m es número de tipos
     */
    public static String[] obtenerNombres() {
        TipoNormalizacion[] tipos = TipoNormalizacion.values();
        String[] nombres = new String[tipos.length];

        for (int i = 0; i < tipos.length; i++) {
            nombres[i] = crear(tipos[i]).getNombre();
        }

        return nombres;
    }

    /**
     * Valida si un nombre es válido
     * Complejidad: O(1)
     */
    public static boolean esNombreValido(String nombre) {
        try {
            crear(nombre);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

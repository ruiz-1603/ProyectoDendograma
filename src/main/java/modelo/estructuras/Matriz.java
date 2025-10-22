package modelo.estructuras;

public class Matriz {
    private double[][] datos;
    private int dimension;

    public Matriz(int dimension) {
        this.dimension = dimension;
        this.datos = new double[dimension][dimension];

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                datos[i][j] = 0.0;
            }
        }
    }

    public Matriz(double[][] datos) {
        this.dimension = datos.length;
        this.datos = new double[dimension][dimension];

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                this.datos[i][j] = datos[i][j];
            }
        }
    }

    public void setPosicion(int i, int j, double valor) {
        if (i >= 0 && i < dimension && j >= 0 && j < dimension) {
            datos[i][j] = valor;
        }
    }

    public double getPosicion(int i, int j) {
        if (i >= 0 && i < dimension && j >= 0 && j < dimension) {
            return datos[i][j];
        }
        return 0.0;
    }

    public int getDimension() {
        return dimension;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matriz ").append(dimension).append("x").append(dimension).append("\n");
        for (int i = 0; i < Math.min(5, dimension); i++) {
            for (int j = 0; j < Math.min(5, dimension); j++) {
                sb.append(String.format("%8.2f ", datos[i][j]));
            }
            if (dimension > 5) sb.append("...");
            sb.append("\n");
        }
        if (dimension > 5) sb.append("...\n");
        return sb.toString();
    }
}
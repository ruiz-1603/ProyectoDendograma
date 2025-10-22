package modelo.estructuras;


public class Vector {
    private double[] datos;
    private String etiqueta;

    public Vector(int dimension) {
        this.datos = new double[dimension];
        this.etiqueta = "";
    }

    public Vector(double[] datos, String etiqueta) {
        this.datos = datos.clone();
        this.etiqueta = etiqueta;
    }

    public Vector(Vector otro) {
        this.datos = otro.datos.clone();
        this.etiqueta = otro.etiqueta;
    }

    public void setValor(int indice, double valor) {
        if (indice >= 0 && indice < datos.length) {
            datos[indice] = valor;
        }
    }

    public double getPosicion(int indice) {
        if (indice >= 0 && indice < datos.length) {
            return datos[indice];
        }
        return 0.0;
    }

    public int dimension() {
        return datos.length;
    }

    public double productoPunto(Vector otro) {
        if (otro.dimension() != this.dimension()) {
            throw new IllegalArgumentException("Vectores de diferente dimensión");
        }

        double suma = 0.0;
        for (int i = 0; i < datos.length; i++) {
            suma += datos[i] * otro.datos[i];
        }
        return suma;
    }

    public double norma() {
        double suma = 0.0;
        for (double valor : datos) {
            suma += valor * valor;
        }
        return Math.sqrt(suma);
    }

    public void multiplicarPorEscalar(double escalar) {
        for (int i = 0; i < datos.length; i++) {
            datos[i] *= escalar;
        }
    }

    public void sumar(Vector otro) {
        if (otro.dimension() != this.dimension()) {
            throw new IllegalArgumentException("Vectores de diferente dimensión");
        }

        for (int i = 0; i < datos.length; i++) {
            datos[i] += otro.datos[i];
        }
    }

    public void restar(Vector otro) {
        if (otro.dimension() != this.dimension()) {
            throw new IllegalArgumentException("Vectores de diferente dimensión");
        }

        for (int i = 0; i < datos.length; i++) {
            datos[i] -= otro.datos[i];
        }
    }


    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public double[] getDatos() {
        return datos.clone();
    }

    public void setDatos(double[] nuevosDatos) {
        if (nuevosDatos.length == datos.length) {
            this.datos = nuevosDatos.clone();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(etiqueta).append(": [");
        for (int i = 0; i < datos.length; i++) {
            sb.append(String.format("%.2f", datos[i]));
            if (i < datos.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Vector otro = (Vector) obj;
        if (datos.length != otro.datos.length) return false;

        for (int i = 0; i < datos.length; i++) {
            if (Math.abs(datos[i] - otro.datos[i]) > 1e-9) {
                return false;
            }
        }
        return true;
    }
}
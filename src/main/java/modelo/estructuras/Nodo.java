package modelo.estructuras;

import java.util.*;
import java.text.DecimalFormat;

/**
 * Nodo de árbol binario para dendrograma
 * Cada nodo puede ser hoja (elemento individual) o interno (fusión de clusters)
 * Complejidad espacial: O(n) donde n es número de elementos en el cluster
 */
public class Nodo {
    private Nodo izquierdo;
    private Nodo derecho;
    private double distancia;
    private Set<String> elementos; // TreeSet mantiene orden alfabético

    /**
     * Constructor para nodo hoja
     * Complejidad: O(1)
     */
    public Nodo(String elemento) {
        this.elementos = new TreeSet<>();
        this.elementos.add(elemento);
        this.distancia = 0.0;
        this.izquierdo = null;
        this.derecho = null;
    }

    /**
     * Constructor para nodo interno (fusión de dos clusters)
     * Complejidad: O(n + m) donde n y m son tamaños de los clusters
     */
    public Nodo(Nodo izquierdo, Nodo derecho, double distancia) {
        this.izquierdo = izquierdo;
        this.derecho = derecho;
        this.distancia = distancia;

        // Combinar elementos de ambos hijos
        this.elementos = new TreeSet<>();
        this.elementos.addAll(izquierdo.elementos);
        this.elementos.addAll(derecho.elementos);
    }

    /**
     * Verifica si es un nodo hoja
     * Complejidad: O(1)
     */
    public boolean esHoja() {
        return izquierdo == null && derecho == null;
    }

    /**
     * Obtiene el nombre del cluster en formato requerido
     * Formato: (elemento1;elemento2;elemento3)
     * Complejidad: O(n) donde n es número de elementos
     */
    public String obtenerNombre() {
        return "(" + String.join(";", elementos) + ")";
    }

    /**
     * Genera representación JSON del dendrograma
     * Complejidad: O(n) donde n es número total de nodos
     */
    public String toJSON() {
        return toJSON(0);
    }

    /**
     * Método recursivo para generar JSON con indentación
     * Complejidad: O(n)
     */
    private String toJSON(int indent) {
        DecimalFormat df = new DecimalFormat("0.0#");
        df.setGroupingUsed(false);

        StringBuilder sb = new StringBuilder();
        String indentStr = "  ".repeat(indent);

        sb.append(indentStr).append("{\n");

        // Campo "n": nombre del cluster
        sb.append(indentStr).append("  \"n\": \"");
        sb.append(obtenerNombre());
        sb.append("\",\n");

        // Campo "d": distancia de fusión
        sb.append(indentStr).append("  \"d\": ");
        sb.append(df.format(distancia));
        sb.append(",\n");

        // Campo "c": array de hijos
        sb.append(indentStr).append("  \"c\": ");

        if (esHoja()) {
            // Nodo hoja: array vacío
            sb.append("[]");
        } else {
            // Nodo interno: recursión sobre hijos
            sb.append("[\n");
            sb.append(izquierdo.toJSON(indent + 2));
            sb.append(",\n");
            sb.append(derecho.toJSON(indent + 2));
            sb.append("\n");
            sb.append(indentStr).append("  ]");
        }

        sb.append("\n");
        sb.append(indentStr).append("}");

        return sb.toString();
    }

    /**
     * Cuenta el número total de hojas en el árbol
     * Complejidad: O(n)
     */
    public int contarHojas() {
        if (esHoja()) {
            return 1;
        }
        return izquierdo.contarHojas() + derecho.contarHojas();
    }

    /**
     * Calcula la altura del árbol
     * Complejidad: O(n)
     */
    public int altura() {
        if (esHoja()) {
            return 0;
        }
        return 1 + Math.max(izquierdo.altura(), derecho.altura());
    }

    /**
     * Obtiene todas las etiquetas de las hojas en orden
     * Complejidad: O(n)
     */
    public List<String> obtenerEtiquetasHojas() {
        List<String> etiquetas = new ArrayList<>();
        obtenerEtiquetasHojasRecursivo(etiquetas);
        return etiquetas;
    }

    private void obtenerEtiquetasHojasRecursivo(List<String> etiquetas) {
        if (esHoja()) {
            etiquetas.addAll(elementos);
        } else {
            izquierdo.obtenerEtiquetasHojasRecursivo(etiquetas);
            derecho.obtenerEtiquetasHojasRecursivo(etiquetas);
        }
    }

    /**
     * Representación en texto del árbol (para debugging)
     * Complejidad: O(n)
     */
    public String toStringArbol() {
        return toStringArbol(0);
    }

    private String toStringArbol(int nivel) {
        StringBuilder sb = new StringBuilder();
        String indent = "  ".repeat(nivel);

        sb.append(indent);
        sb.append(obtenerNombre());
        sb.append(" [d=").append(String.format("%.2f", distancia)).append("]");
        sb.append("\n");

        if (!esHoja()) {
            sb.append(izquierdo.toStringArbol(nivel + 1));
            sb.append(derecho.toStringArbol(nivel + 1));
        }

        return sb.toString();
    }

    public Nodo getIzquierdo() {
        return izquierdo;
    }

    public Nodo getDerecho() {
        return derecho;
    }

    public double getDistancia() {
        return distancia;
    }

    public Set<String> getElementos() {
        return new TreeSet<>(elementos);
    }

    public int getNumeroElementos() {
        return elementos.size();
    }

    @Override
    public String toString() {
        return obtenerNombre() + " [d=" + String.format("%.2f", distancia) +
                ", hojas=" + contarHojas() + "]";
    }
}
package modelo.estructuras;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

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

    /**
     * Retorna la etiqueta del nodo si es una hoja.
     * @return La etiqueta del elemento, o null si no es una hoja.
     */
    public String getEtiqueta() {
        if (esHoja() && !elementos.isEmpty()) {
            return elementos.iterator().next();
        }
        return null; // O una representación para nodos internos si se desea
    }

    @Override
    public String toString() {
        return obtenerNombre() + " [d=" + String.format("%.2f", distancia) +
                ", hojas=" + contarHojas() + "]";
    }

    /**
     * Corta el dendrograma para obtener un número k de clusters.
     * @param k El número de clusters deseado.
     * @return Una lista de Nodos que representan los k clusters.
     */
    public List<Nodo> cortarArbol(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("K debe ser al menos 1.");
        }
        if (k > contarHojas()) {
            throw new IllegalArgumentException("K no puede ser mayor que el número de elementos.");
        }
        if (k == 1) {
            return Collections.singletonList(this);
        }

        // PriorityQueue para encontrar siempre el siguiente cluster a dividir (el de mayor distancia)
        PriorityQueue<Nodo> aDividir = new PriorityQueue<>(Comparator.comparingDouble(Nodo::getDistancia).reversed());
        aDividir.add(this);

        while (aDividir.size() < k) {
            Nodo masGrande = aDividir.poll();

            if (masGrande == null || masGrande.esHoja()) {
                // No se puede dividir más, hemos alcanzado el número máximo de clusters posibles
                break;
            }

            aDividir.add(masGrande.getIzquierdo());
            aDividir.add(masGrande.getDerecho());
        }

        return new ArrayList<>(aDividir);
    }

    /**
     * Corta el dendrograma según un umbral de distancia.
     * @param umbral La distancia máxima de fusión para considerar un subárbol como un clúster.
     * @return Una lista de Nodos que representan los clusters.
     */
    public List<Nodo> cortarPorDistancia(double umbral) {
        // Wrapper para el método recursivo.
        List<Nodo> clusters = new ArrayList<>();
        cortarPorDistanciaRecursivo(umbral, clusters);
        return clusters;
    }

    private void cortarPorDistanciaRecursivo(double umbral, List<Nodo> clusters) {
        // Si la distancia de fusión de este nodo es mayor que el umbral,
        // significa que la fusión no debería haber ocurrido.
        // Por lo tanto, descendemos a sus hijos.
        // Las hojas tienen distancia 0, por lo que nunca cumplirán esta condición.
        if (this.distancia > umbral && !this.esHoja()) {
            if (izquierdo != null) {
                izquierdo.cortarPorDistanciaRecursivo(umbral, clusters);
            }
            if (derecho != null) {
                derecho.cortarPorDistanciaRecursivo(umbral, clusters);
            }
        } else {
            // Si la distancia de este nodo es <= al umbral, esta fusión es válida.
            // Este nodo y todo su subárbol se consideran un único clúster.
            clusters.add(this);
        }
    }
}
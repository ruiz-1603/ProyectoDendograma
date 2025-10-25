package vista;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import modelo.estructuras.Diccionario;
import modelo.estructuras.ListaDoble;
import modelo.estructuras.Nodo;

public class DendrogramaDrawer {

    private static final double MARGEN_SUPERIOR_INFERIOR = 50;
    private static final double MARGEN_LATERAL = 50;
    private static final double ESPACIADO_MINIMO_HOJAS = 20;
    private static final double TAMANO_FUENTE = 10;
    private static final Color[] COLORES = {
            Color.web("#e6194B"), Color.web("#3cb44b"), Color.web("#ffe119"), Color.web("#4363d8"),
            Color.web("#f58231"), Color.web("#911eb4"), Color.web("#46f0f0"), Color.web("#f032e6"),
            Color.web("#bcf60c"), Color.web("#fabebe"), Color.web("#008080"), Color.web("#e6beff"),
            Color.web("#9a6324"), Color.web("#fffac8"), Color.web("#800000"), Color.web("#aaffc3"),
            Color.web("#808000"), Color.web("#ffd8b1"), Color.web("#000075"), Color.web("#808080")
    };

    public static void draw(Pane panel, Nodo raiz, ListaDoble<Nodo> clustersParaColorear) {
        panel.getChildren().clear();
        if (raiz == null) return;

        // 1. Obtener etiquetas para calcular el diseño
        ListaDoble<String> etiquetasHojas = new ListaDoble<>();
        obtenerEtiquetasHojas(raiz, etiquetasHojas);

        // 2. Calcular espaciado requerido basado en anchos de etiquetas
        double anchoMaximoEtiqueta = 0;
        for (int i = 0; i < etiquetasHojas.tamanio(); i++) {
            String etiqueta = etiquetasHojas.obtener(i);
            Text texto = new Text(etiqueta);
            texto.setFont(Font.font(TAMANO_FUENTE));
            anchoMaximoEtiqueta = Math.max(anchoMaximoEtiqueta, texto.getLayoutBounds().getWidth());
        }
        double espaciadoHojas = Math.max(ESPACIADO_MINIMO_HOJAS, anchoMaximoEtiqueta);

        // 3. Construir mapa de colores
        Diccionario<Nodo, Color> mapaColores = construirMapaColores(raiz, clustersParaColorear);

        // 4. Calcular posiciones de nodos con nuevo espaciado
        Diccionario<Nodo, Point2D> posiciones = calcularPosicionesNodos(raiz, panel, espaciadoHojas);

        // 5. Dibujar nodos con colores
        dibujarNodo(panel, raiz, posiciones, mapaColores);
    }

    private static Diccionario<Nodo, Color> construirMapaColores(Nodo raiz, ListaDoble<Nodo> clustersParaColorear) {
        Diccionario<Nodo, Color> mapaColores = new Diccionario<>();

        if (clustersParaColorear == null || clustersParaColorear.tamanio() == 0) {
            colorearSubarbol(raiz, Color.WHITE, mapaColores);
        } else {
            colorearSubarbol(raiz, Color.WHITE, mapaColores); // Comenzar con todo negro
            for (int i = 0; i < clustersParaColorear.tamanio(); i++) {
                Nodo raizCluster = clustersParaColorear.obtener(i);
                Color color = COLORES[i % COLORES.length];
                colorearSubarbol(raizCluster, color, mapaColores); // Sobrescribir con colores específicos
            }
        }
        return mapaColores;
    }

    private static void colorearSubarbol(Nodo nodo, Color color, Diccionario<Nodo, Color> mapaColores) {
        if (nodo == null) return;
        mapaColores.poner(nodo, color);
        colorearSubarbol(nodo.getIzquierdo(), color, mapaColores);
        colorearSubarbol(nodo.getDerecho(), color, mapaColores);
    }

    private static void obtenerEtiquetasHojas(Nodo nodo, ListaDoble<String> etiquetas) {
        if (nodo.esHoja()) {
            // Una hoja puede tener uno o más elementos
            ListaDoble.IteradorLista<String> iterador = nodo.getElementos().iterador();
            while (iterador.tieneSiguiente()) {
                etiquetas.agregar(iterador.siguiente());
            }
            return;
        }
        obtenerEtiquetasHojas(nodo.getIzquierdo(), etiquetas);
        obtenerEtiquetasHojas(nodo.getDerecho(), etiquetas);
    }

    private static void dibujarNodo(Pane panel, Nodo nodo, Diccionario<Nodo, Point2D> posiciones, Diccionario<Nodo, Color> mapaColores) {
        if (nodo == null) return;

        Point2D posicion = posiciones.obtener(nodo);
        Color color = mapaColores.contieneClave(nodo) ? mapaColores.obtener(nodo) : Color.WHITE;

        if (nodo.esHoja()) {
            // Obtener las etiquetas de la hoja
            ListaDoble<String> elementos = nodo.getElementos();
            StringBuilder textoEtiqueta = new StringBuilder();

            ListaDoble.IteradorLista<String> iterador = elementos.iterador();
            boolean primero = true;
            while (iterador.tieneSiguiente()) {
                if (!primero) textoEtiqueta.append(", ");
                textoEtiqueta.append(iterador.siguiente());
                primero = false;
            }

            Text etiqueta = new Text(textoEtiqueta.toString());
            etiqueta.setFont(Font.font(TAMANO_FUENTE));
            etiqueta.setFill(color);
            etiqueta.setStyle("-fx-font-weight: bold;");

            // Centrar la etiqueta horizontalmente
            double anchoEtiqueta = etiqueta.getLayoutBounds().getWidth();
            etiqueta.setX(posicion.getX() - (anchoEtiqueta / 2));
            etiqueta.setY(posicion.getY() + TAMANO_FUENTE + 5);
            panel.getChildren().add(etiqueta);

        } else {
            Point2D posicionIzquierda = posiciones.obtener(nodo.getIzquierdo());
            Point2D posicionDerecha = posiciones.obtener(nodo.getDerecho());

            Line lineaHorizontal = new Line(posicionIzquierda.getX(), posicion.getY(), posicionDerecha.getX(), posicion.getY());
            lineaHorizontal.setStroke(color);
            lineaHorizontal.setStrokeWidth(1.5);

            Color colorIzquierdo = mapaColores.contieneClave(nodo.getIzquierdo()) ?
                    mapaColores.obtener(nodo.getIzquierdo()) : Color.WHITE;
            Color colorDerecho = mapaColores.contieneClave(nodo.getDerecho()) ?
                    mapaColores.obtener(nodo.getDerecho()) : Color.WHITE;

            Line lineaVerticalIzquierda = new Line(posicionIzquierda.getX(), posicionIzquierda.getY(), posicionIzquierda.getX(), posicion.getY());
            lineaVerticalIzquierda.setStroke(colorIzquierdo);
            lineaVerticalIzquierda.setStrokeWidth(1.5);

            Line lineaVerticalDerecha = new Line(posicionDerecha.getX(), posicionDerecha.getY(), posicionDerecha.getX(), posicion.getY());
            lineaVerticalDerecha.setStroke(colorDerecho);
            lineaVerticalDerecha.setStrokeWidth(1.5);

            panel.getChildren().addAll(lineaHorizontal, lineaVerticalIzquierda, lineaVerticalDerecha);

            dibujarNodo(panel, nodo.getIzquierdo(), posiciones, mapaColores);
            dibujarNodo(panel, nodo.getDerecho(), posiciones, mapaColores);
        }
    }

    private static Diccionario<Nodo, Point2D> calcularPosicionesNodos(Nodo raiz, Pane panel, double espaciadoHojas) {
        Diccionario<Nodo, Point2D> posiciones = new Diccionario<>();
        ContadorHojas contadorHojas = new ContadorHojas();
        double distanciaMaxima = raiz.getDistancia();

        calcularXHojas(raiz, contadorHojas, posiciones, espaciadoHojas);

        double anchoPanel = (contadorHojas.getValor() - 1) * espaciadoHojas + 2 * MARGEN_LATERAL;
        calcularXInternos(raiz, posiciones);

        // Recalcular requisitos de altura para etiquetas horizontales
        double alturaZonaEtiquetas = 50.0;
        double alturaZonaDendrograma = panel.getPrefHeight() - alturaZonaEtiquetas - MARGEN_SUPERIOR_INFERIOR;
        double alturaPanel = panel.getPrefHeight();

        double escalaY = (alturaZonaDendrograma - MARGEN_SUPERIOR_INFERIOR * 2) / distanciaMaxima;

        establecerPosicionesY(raiz, posiciones, escalaY, alturaPanel, alturaZonaDendrograma);

        panel.setPrefSize(anchoPanel, alturaPanel);
        return posiciones;
    }

    private static void calcularXHojas(Nodo nodo, ContadorHojas contador, Diccionario<Nodo, Point2D> posiciones, double espaciadoHojas) {
        if (nodo.esHoja()) {
            double x = MARGEN_LATERAL + contador.incrementarYObtener() * espaciadoHojas;
            posiciones.poner(nodo, new Point2D(x, 0));
            return;
        }
        calcularXHojas(nodo.getIzquierdo(), contador, posiciones, espaciadoHojas);
        calcularXHojas(nodo.getDerecho(), contador, posiciones, espaciadoHojas);
    }

    private static void calcularXInternos(Nodo nodo, Diccionario<Nodo, Point2D> posiciones) {
        if (nodo.esHoja()) {
            return;
        }
        calcularXInternos(nodo.getIzquierdo(), posiciones);
        calcularXInternos(nodo.getDerecho(), posiciones);

        double x = (posiciones.obtener(nodo.getIzquierdo()).getX() + posiciones.obtener(nodo.getDerecho()).getX()) / 2;
        posiciones.poner(nodo, new Point2D(x, 0));
    }

    private static void establecerPosicionesY(Nodo nodo, Diccionario<Nodo, Point2D> posiciones, double escalaY, double alturaPanel, double alturaZonaDendrograma) {
        double x = posiciones.obtener(nodo).getX();
        double y = MARGEN_SUPERIOR_INFERIOR + (alturaZonaDendrograma - nodo.getDistancia() * escalaY);
        posiciones.poner(nodo, new Point2D(x, y));

        if (!nodo.esHoja()) {
            establecerPosicionesY(nodo.getIzquierdo(), posiciones, escalaY, alturaPanel, alturaZonaDendrograma);
            establecerPosicionesY(nodo.getDerecho(), posiciones, escalaY, alturaPanel, alturaZonaDendrograma);
        }
    }

    // ========== CLASE AUXILIAR ==========

    /**
     * Reemplazo de AtomicInteger - contador simple
     */
    private static class ContadorHojas {
        private int valor;

        public ContadorHojas() {
            this.valor = 0;
        }

        public int incrementarYObtener() {
            return valor++;
        }

        public int getValor() {
            return valor;
        }
    }
}
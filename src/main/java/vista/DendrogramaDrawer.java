package vista;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import modelo.estructuras.ListaDoble;
import modelo.estructuras.Nodo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DendrogramaDrawer {

    private static final double PADDING_TOP_BOTTOM = 50;
    private static final double PADDING_SIDES = 50;
    private static final double MIN_LEAF_SPACING = 20; // Horizontal spacing
    private static final double FONT_SIZE = 10;
    private static final List<Color> COLORS = List.of(
            Color.web("#e6194B"), Color.web("#3cb44b"), Color.web("#ffe119"), Color.web("#4363d8"),
            Color.web("#f58231"), Color.web("#911eb4"), Color.web("#46f0f0"), Color.web("#f032e6"),
            Color.web("#bcf60c"), Color.web("#fabebe"), Color.web("#008080"), Color.web("#e6beff"),
            Color.web("#9a6324"), Color.web("#fffac8"), Color.web("#800000"), Color.web("#aaffc3"),
            Color.web("#808000"), Color.web("#ffd8b1"), Color.web("#000075"), Color.web("#808080")
    );

    public static void draw(Pane pane, Nodo root, List<Nodo> clustersToColor) {
        pane.getChildren().clear();
        if (root == null) return;

        // 1. Get labels to calculate layout
        List<String> leafLabels = new ArrayList<>();
        getLeafLabels(root, leafLabels);

        // 2. Calculate required spacing based on horizontal label widths
        double maxLabelWidth = 0;
        for (String label : leafLabels) {
            Text text = new Text(label);
            text.setFont(Font.font(FONT_SIZE));
            maxLabelWidth = Math.max(maxLabelWidth, text.getLayoutBounds().getWidth());
        }
        double leafSpacing = Math.max(MIN_LEAF_SPACING, maxLabelWidth);

        // 3. Build color map
        Map<Nodo, Color> colorMap = buildColorMap(root, clustersToColor);

        // 4. Calculate node positions with new spacing
        Map<Nodo, Point2D> positions = calculateNodePositions(root, pane, leafSpacing);

        // 5. Draw nodes with colors
        drawNode(pane, root, positions, colorMap);
    }

    private static Map<Nodo, Color> buildColorMap(Nodo root, List<Nodo> clustersToColor) {
        Map<Nodo, Color> colorMap = new HashMap<>();
        if (clustersToColor == null || clustersToColor.isEmpty()) {
            colorSubtree(root, Color.BLACK, colorMap);
        } else {
            colorSubtree(root, Color.BLACK, colorMap); // Start with all black
            for (int i = 0; i < clustersToColor.size(); i++) {
                Nodo clusterRoot = clustersToColor.get(i);
                Color color = COLORS.get(i % COLORS.size());
                colorSubtree(clusterRoot, color, colorMap); // Override with specific colors
            }
        }
        return colorMap;
    }

    private static void colorSubtree(Nodo node, Color color, Map<Nodo, Color> colorMap) {
        if (node == null) return;
        colorMap.put(node, color);
        colorSubtree(node.getIzquierdo(), color, colorMap);
        colorSubtree(node.getDerecho(), color, colorMap);
    }

    private static void getLeafLabels(Nodo node, List<String> labels) {
        if (node.esHoja()) {
            // Una hoja puede tener uno o más elementos
            ListaDoble.IteradorLista<String> it = node.getElementos().iterador();
            while (it.tieneSiguiente()) {
                labels.add(it.siguiente());
            }
            return;
        }
        getLeafLabels(node.getIzquierdo(), labels);
        getLeafLabels(node.getDerecho(), labels);
    }

    private static void drawNode(Pane pane, Nodo node, Map<Nodo, Point2D> positions, Map<Nodo, Color> colorMap) {
        if (node == null) return;

        Point2D pos = positions.get(node);
        Color color = colorMap.getOrDefault(node, Color.BLACK);

        if (node.esHoja()) {
            // Obtener las etiquetas de la hoja
            ListaDoble<String> elementos = node.getElementos();
            StringBuilder labelText = new StringBuilder();

            ListaDoble.IteradorLista<String> it = elementos.iterador();
            boolean primero = true;
            while (it.tieneSiguiente()) {
                if (!primero) labelText.append(", ");
                labelText.append(it.siguiente());
                primero = false;
            }

            Text label = new Text(labelText.toString());
            label.setFont(Font.font(FONT_SIZE));
            label.setFill(color);
            label.setStyle("-fx-font-weight: bold;");

            // Center the label horizontally
            double labelWidth = label.getLayoutBounds().getWidth();
            label.setX(pos.getX() - (labelWidth / 2));
            label.setY(pos.getY() + 20); // Position below the line end
            pane.getChildren().add(label);

        } else {
            Point2D leftPos = positions.get(node.getIzquierdo());
            Point2D rightPos = positions.get(node.getDerecho());

            Line hLine = new Line(leftPos.getX(), pos.getY(), rightPos.getX(), pos.getY());
            hLine.setStroke(color);
            hLine.setStrokeWidth(1.5);

            Line vLineLeft = new Line(leftPos.getX(), leftPos.getY(), leftPos.getX(), pos.getY());
            vLineLeft.setStroke(colorMap.getOrDefault(node.getIzquierdo(), Color.BLACK));
            vLineLeft.setStrokeWidth(1.5);

            Line vLineRight = new Line(rightPos.getX(), rightPos.getY(), rightPos.getX(), pos.getY());
            vLineRight.setStroke(colorMap.getOrDefault(node.getDerecho(), Color.BLACK));
            vLineRight.setStrokeWidth(1.5);

            pane.getChildren().addAll(hLine, vLineLeft, vLineRight);

            drawNode(pane, node.getIzquierdo(), positions, colorMap);
            drawNode(pane, node.getDerecho(), positions, colorMap);
        }
    }

    private static Map<Nodo, Point2D> calculateNodePositions(Nodo root, Pane pane, double leafSpacing) {
        Map<Nodo, Point2D> positions = new HashMap<>();
        AtomicInteger leafCounter = new AtomicInteger(0);
        double maxDist = root.getDistancia();

        calculateLeafX(root, leafCounter, positions, leafSpacing);

        double paneWidth = (leafCounter.get() - 1) * leafSpacing + 2 * PADDING_SIDES;
        calculateInternalX(root, positions);

        // Recalculate height requirements for horizontal labels
        double labelZoneHeight = FONT_SIZE + 30; // Font height + padding
        double dendrogramZoneHeight = 600;
        double paneHeight = dendrogramZoneHeight + labelZoneHeight;

        double scaleY = (dendrogramZoneHeight - PADDING_TOP_BOTTOM * 2) / maxDist;

        setYPositions(root, positions, scaleY, paneHeight);

        pane.setPrefSize(paneWidth, paneHeight);
        return positions;
    }

    private static void calculateLeafX(Nodo node, AtomicInteger counter, Map<Nodo, Point2D> positions, double leafSpacing) {
        if (node.esHoja()) {
            double x = PADDING_SIDES + counter.getAndIncrement() * leafSpacing;
            positions.put(node, new Point2D(x, 0));
            return;
        }
        calculateLeafX(node.getIzquierdo(), counter, positions, leafSpacing);
        calculateLeafX(node.getDerecho(), counter, positions, leafSpacing);
    }

    private static void calculateInternalX(Nodo node, Map<Nodo, Point2D> positions) {
        if (node.esHoja()) {
            return;
        }
        calculateInternalX(node.getIzquierdo(), positions);
        calculateInternalX(node.getDerecho(), positions);

        double x = (positions.get(node.getIzquierdo()).getX() + positions.get(node.getDerecho()).getX()) / 2;
        positions.put(node, new Point2D(x, 0));
    }

    private static void setYPositions(Nodo node, Map<Nodo, Point2D> positions, double scaleY, double paneHeight) {
        double x = positions.get(node).getX();
        double y = paneHeight - (PADDING_TOP_BOTTOM + node.getDistancia() * scaleY);
        positions.put(node, new Point2D(x, y));

        if (!node.esHoja()) {
            setYPositions(node.getIzquierdo(), positions, scaleY, paneHeight);
            setYPositions(node.getDerecho(), positions, scaleY, paneHeight);
        }
    }
}

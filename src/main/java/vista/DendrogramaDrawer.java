package vista;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import modelo.estructuras.Nodo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DendrogramaDrawer {

    private static final double PADDING_TOP_BOTTOM = 50;
    private static final double PADDING_SIDES = 50;
    private static final double MIN_LEAF_SPACING = 40;
    private static final double FONT_SIZE = 10;

    public static void draw(Pane pane, Nodo root) {
        pane.getChildren().clear();
        if (root == null) return;

        // Calcular espaciado dinámico basado en las etiquetas
        List<String> leafLabels = new ArrayList<>();
        getLeafLabels(root, leafLabels);
        double longestLabelLength = leafLabels.stream().mapToInt(String::length).max().orElse(0);
        double leafSpacing = Math.max(MIN_LEAF_SPACING, longestLabelLength * FONT_SIZE * 0.6);

        Map<Nodo, Point2D> positions = calculateNodePositions(root, pane, leafSpacing);
        drawNode(pane, root, positions);
    }

    private static void getLeafLabels(Nodo node, List<String> labels) {
        if (node.esHoja()) {
            labels.add(node.getEtiqueta());
            return;
        }
        getLeafLabels(node.getIzquierdo(), labels);
        getLeafLabels(node.getDerecho(), labels);
    }

    private static void drawNode(Pane pane, Nodo node, Map<Nodo, Point2D> positions) {
        if (node == null) return;

        Point2D pos = positions.get(node);

        if (node.esHoja()) {
            Text label = new Text(pos.getX(), pos.getY() + 10, node.getEtiqueta());
            label.setRotate(-90);
            pane.getChildren().add(label);
        } else {
            Point2D leftPos = positions.get(node.getIzquierdo());
            Point2D rightPos = positions.get(node.getDerecho());

            Line hLine = new Line(leftPos.getX(), pos.getY(), rightPos.getX(), pos.getY());
            Line vLineLeft = new Line(leftPos.getX(), leftPos.getY(), leftPos.getX(), pos.getY());
            Line vLineRight = new Line(rightPos.getX(), rightPos.getY(), rightPos.getX(), pos.getY());
            pane.getChildren().addAll(hLine, vLineLeft, vLineRight);

            drawNode(pane, node.getIzquierdo(), positions);
            drawNode(pane, node.getDerecho(), positions);
        }
    }

    private static Map<Nodo, Point2D> calculateNodePositions(Nodo root, Pane pane, double leafSpacing) {
        Map<Nodo, Point2D> positions = new HashMap<>();
        AtomicInteger leafCounter = new AtomicInteger(0);
        double maxDist = root.getDistancia();

        calculateLeafX(root, leafCounter, positions, leafSpacing);

        double paneWidth = (leafCounter.get() - 1) * leafSpacing + 2 * PADDING_SIDES;
        calculateInternalX(root, positions);

        List<String> leafLabels = new ArrayList<>();
        getLeafLabels(root, leafLabels);
        double longestLabelInChars = leafLabels.stream().mapToInt(String::length).max().orElse(0);

        double labelZoneHeight = longestLabelInChars * FONT_SIZE * 0.7 + 20; // Space for rotated labels
        double dendrogramZoneHeight = 600; // Fixed height for the tree itself
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

package fr.ensisa.tp.view;

import fr.ensisa.tp.model.CModel;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

import java.util.ArrayList;
import java.util.List;

public final class CEditor extends Pane {
    private static final double PADDING = 30;
    private static final double HANDLE_R = 5;
    private static final double FIXED_SIZE = 300;

    private final CModel model;

    private final Group drawing = new Group();
    private final Line axisX = new Line();
    private final Line axisY = new Line();
    private final Polyline curve = new Polyline();
    private final List<Circle> handles = new ArrayList<>();

    private int draggedIndex = -1;

    public CEditor(CModel model, Color curveColor) {
        this.model = model;

        setMinSize(FIXED_SIZE, FIXED_SIZE);
        setPrefSize(FIXED_SIZE, FIXED_SIZE);
        setMaxSize(FIXED_SIZE, FIXED_SIZE);

        getChildren().add(drawing);

        axisX.setStroke(Color.GRAY);
        axisY.setStroke(Color.GRAY);

        curve.setStroke(curveColor);
        curve.setFill(null);

        drawing.getChildren().addAll(axisX, axisY, curve);

        createHandles();

        widthProperty().addListener((obs, o, n) -> redraw());
        heightProperty().addListener((obs, o, n) -> redraw());

        redraw();
    }

    public CEditor(CModel model) {
        this(model, Color.BLACK);
    }

    private void createHandles() {
        for (int i = 0; i < model.getControlPointsCount(); i++) {
            Circle c = new Circle(HANDLE_R);
            c.setFill(Color.LIGHTGRAY);
            c.setStroke(Color.DARKGRAY);

            final int idx = i;
            c.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> onPress(idx, e));
            c.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> onDrag(idx, e));
            c.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> onRelease(idx, e));

            handles.add(c);
            drawing.getChildren().add(c);
        }
    }

    private void onPress(int idx, MouseEvent e) {
        draggedIndex = idx;
        e.consume();
    }

    private void onDrag(int idx, MouseEvent e) {
        if (draggedIndex != idx) return;

        int newY = valueYFromPixel(e.getY());
        model.setY(idx, newY);

        redraw();
        e.consume();
    }

    private void onRelease(int idx, MouseEvent e) {
        draggedIndex = -1;
        e.consume();
    }

    public void redraw() {
        double w = getWidth();
        double h = getHeight();

        double size = Math.max(50, Math.min(w, h) - 2 * PADDING);
        double left = PADDING;
        double top = PADDING;
        double right = left + size;
        double bottom = top + size;

        axisX.setStartX(left);
        axisX.setStartY(bottom);
        axisX.setEndX(right);
        axisX.setEndY(bottom);

        axisY.setStartX(left);
        axisY.setStartY(bottom);
        axisY.setEndX(left);
        axisY.setEndY(top);

        curve.getPoints().clear();
        for (int x = 0; x <= 255; x++) {
            double yd = model.eval(x);
            int y = CModel.clamp255((int) Math.round(yd));

            double px = pixelXFromValue(x, left, size);
            double py = pixelYFromValue(y, top, size);

            curve.getPoints().addAll(px, py);
        }

        for (int i = 0; i < handles.size(); i++) {
            int x = model.getX(i);
            int y = model.getY(i);

            double px = pixelXFromValue(x, left, size);
            double py = pixelYFromValue(y, top, size);

            handles.get(i).setCenterX(px);
            handles.get(i).setCenterY(py);
        }
    }

    private static double pixelXFromValue(int x, double left, double size) {
        return left + (x / 255.0) * size;
    }

    private static double pixelYFromValue(int y, double top, double size) {
        return top + (1.0 - y / 255.0) * size;
    }

    private int valueYFromPixel(double py) {
        double w = getWidth();
        double h = getHeight();

        double size = Math.max(50, Math.min(w, h) - 2 * PADDING);
        double top = PADDING;

        double t = (py - top) / size;
        double y = (1.0 - t) * 255.0;

        return CModel.clamp255((int) Math.round(y));
    }
}

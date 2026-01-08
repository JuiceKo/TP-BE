package fr.ensisa.tp.view;

import fr.ensisa.tp.model.CModel;
import javafx.beans.InvalidationListener;
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

    private static final double PADDING = 30.0;
    private static final double HANDLE_R = 5.0;
    private static final double FIXED_SIZE = 300.0;

    private final CModel model;

    private final Group drawing = new Group();
    private final Line axisX = new Line();
    private final Line axisY = new Line();
    private final Polyline curve = new Polyline();
    private final List<Circle> handles = new ArrayList<>();

    private Runnable onCurveChanged;
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
        installModelListeners();
        installMouseHandlers();

        widthProperty().addListener((obs, o, n) -> redrawAll());
        heightProperty().addListener((obs, o, n) -> redrawAll());

        redrawAll();
    }

    public CEditor(CModel model) {
        this(model, Color.BLACK);
    }

    public void setOnCurveChanged(Runnable r) {
        this.onCurveChanged = r;
    }

    private void createHandles() {
        for (int i = 0; i < model.getControlPointsCount(); i++) {
            Circle c = new Circle(HANDLE_R);
            c.setFill(Color.LIGHTGRAY);
            c.setStroke(Color.DARKGRAY);
            handles.add(c);
            drawing.getChildren().add(c);
        }
    }

    private void installModelListeners() {
        InvalidationListener l = obs -> {
            redrawCurve();
            redrawHandles();
        };

        for (int i = 0; i < model.getControlPointsCount(); i++) {
            model.yProperty(i).addListener(l);
        }
    }

    private void installMouseHandlers() {
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
    }

    private void mousePressed(MouseEvent e) {
        int idx = findHandleAt(e.getX(), e.getY());
        draggedIndex = idx;
        if (draggedIndex != -1) e.consume();
    }

    private void mouseDragged(MouseEvent e) {
        if (draggedIndex == -1) return;

        int newY = valueYFromPixel(e.getY());
        model.setY(draggedIndex, newY);

        if (onCurveChanged != null) onCurveChanged.run();

        e.consume();
    }

    private void mouseReleased(MouseEvent e) {
        draggedIndex = -1;
        e.consume();
    }

    private int findHandleAt(double x, double y) {
        for (int i = 0; i < handles.size(); i++) {
            Circle c = handles.get(i);
            double dx = x - c.getCenterX();
            double dy = y - c.getCenterY();
            if (dx * dx + dy * dy <= (HANDLE_R + 3) * (HANDLE_R + 3)) return i;
        }
        return -1;
    }

    private void redrawAll() {
        redrawAxes();
        redrawCurve();
        redrawHandles();
    }

    private void redrawAxes() {
        Frame f = frame();

        axisX.setStartX(f.left);
        axisX.setStartY(f.bottom);
        axisX.setEndX(f.right);
        axisX.setEndY(f.bottom);

        axisY.setStartX(f.left);
        axisY.setStartY(f.bottom);
        axisY.setEndX(f.left);
        axisY.setEndY(f.top);
    }

    private void redrawCurve() {
        Frame f = frame();

        curve.getPoints().clear();
        for (int x = 0; x <= 255; x++) {
            int y = CModel.clamp255((int) Math.round(model.eval(x)));
            curve.getPoints().addAll(pixelXFromValue(x, f), pixelYFromValue(y, f));
        }
    }

    private void redrawHandles() {
        Frame f = frame();

        for (int i = 0; i < handles.size(); i++) {
            int x = model.getX(i);
            int y = model.getY(i);
            Circle c = handles.get(i);
            c.setCenterX(pixelXFromValue(x, f));
            c.setCenterY(pixelYFromValue(y, f));
        }
    }

    private double pixelXFromValue(int x, Frame f) {
        return f.left + (x / 255.0) * f.size;
    }

    private double pixelYFromValue(int y, Frame f) {
        return f.top + (1.0 - y / 255.0) * f.size;
    }

    private int valueYFromPixel(double py) {
        Frame f = frame();
        double t = (py - f.top) / f.size;
        double y = (1.0 - t) * 255.0;
        return CModel.clamp255((int) Math.round(y));
    }

    private Frame frame() {
        double w = getWidth();
        double h = getHeight();
        double size = Math.max(50.0, Math.min(w, h) - 2.0 * PADDING);
        double left = PADDING;
        double top = PADDING;
        return new Frame(left, top, size);
    }

    private static final class Frame {
        final double left;
        final double top;
        final double size;
        final double right;
        final double bottom;

        Frame(double left, double top, double size) {
            this.left = left;
            this.top = top;
            this.size = size;
            this.right = left + size;
            this.bottom = top + size;
        }
    }
}

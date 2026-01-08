package fr.ensisa.tp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class CModel {

    public static final int MIN_N = 4;
    public static final int MAX_N = 8;
    private final int n;
    private final int[] xs;
    private final ObservableList<IntegerProperty> ys;

    public CModel(int controlPointsCount) {
        if (controlPointsCount < MIN_N || controlPointsCount > MAX_N) {
            throw new IllegalArgumentException(
                    "NB point requis : entre " + MIN_N + " et " + MAX_N
            );
        }
        this.n = controlPointsCount;
        this.xs = new int[n];
        this.ys = FXCollections.observableArrayList();

        for (int i = 0; i < n; i++) {
            int x = (int) Math.round(i * 255.0 / (n - 1));
            xs[i] = x;
            ys.add(new SimpleIntegerProperty(x));
        }
    }

    public int getControlPointsCount() {return n;}

    public int getX(int i) {
        checkIndex(i);
        return xs[i];
    }

    public int getY(int i) {
        checkIndex(i);
        return ys.get(i).get();
    }

    public ReadOnlyIntegerProperty yProperty(int i) {
        checkIndex(i);
        return ys.get(i);
    }

    public void setY(int i, int newY) {
        checkIndex(i);
        ys.get(i).set(clamp255(newY));
    }

    public void makeLinear() {
        for (int i = 0; i < n; i++) {
            ys.get(i).set(xs[i]);
        }
    }

    public double eval(double x) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double li = 1.0;
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                li *= (x - xs[j]) / (xs[i] - xs[j]);
            }
            sum += getY(i) * li;
        }
        return sum;
    }

    public static int clamp255(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    private void checkIndex(int i) {
        if (i < 0 || i >= n) {
            throw new IndexOutOfBoundsException("index=" + i + ", n=" + n);
        }
    }
}

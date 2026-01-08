package fr.ensisa.tp.model;

import java.util.Arrays;


public final class CModel {
    private final int n;
    private final int[] xs;
    private final int[] ys;

    public CModel(int controlPointsCount) {
        if (controlPointsCount < 4 || controlPointsCount > 8) {
            throw new IllegalArgumentException("Le nombre de points de contrôle doit être entre 4 et 8.");
        }
        this.n = controlPointsCount;
        this.xs = new int[n];
        this.ys = new int[n];

        for (int i = 0; i < n; i++) {
            xs[i] = (int) Math.round(i * 255.0 / (n - 1));
            ys[i] = xs[i];
        }
    }

    public int getControlPointsCount() { return n; }

    public int getX(int i) { return xs[i]; }
    public int getY(int i) { return ys[i]; }

    public int[] getXsCopy() { return Arrays.copyOf(xs, xs.length); }
    public int[] getYsCopy() { return Arrays.copyOf(ys, ys.length); }

    public void setY(int i, int newY) {
        ys[i] = clamp255(newY);
    }

    public void makeLinear() {
        for (int i = 0; i < n; i++) ys[i] = xs[i];
    }

    // Interpolation de Lagrange : P(x) à partir des points (xi, yi)
    public double eval(double x) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double li = 1.0;
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                li *= (x - xs[j]) / (xs[i] - xs[j]);
            }
            sum += ys[i] * li;
        }
        return sum;
    }

    public static int clamp255(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }
}

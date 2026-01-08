package fr.ensisa.tp;

import fr.ensisa.tp.model.CModel;
import fr.ensisa.tp.view.CEditor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;

public class MainController {

    @FXML private StackPane redHost;
    @FXML private StackPane greenHost;
    @FXML private StackPane blueHost;
    @FXML private ImageView imageView;

    private CModel redModel, greenModel, blueModel;
    private CEditor redEditor, greenEditor, blueEditor;

    // On garde l'originale (non filtrée) en mémoire
    private Image originalImage;

    // LUTs (lookup tables) pour accélérer : 0..255 -> 0..255
    private final int[] lutR = new int[256];
    private final int[] lutG = new int[256];
    private final int[] lutB = new int[256];

    // Throttle : éviter de relancer 1000 filtres pendant un drag
    private boolean filterScheduled = false;

    @FXML
    private void initialize() {
        int n = 6;

        redModel = new CModel(n);
        greenModel = new CModel(n);
        blueModel = new CModel(n);

        redEditor = new CEditor(redModel, Color.RED);
        greenEditor = new CEditor(greenModel, Color.GREEN);
        blueEditor = new CEditor(blueModel, Color.BLUE);

        // Important: on ne filtre pas "direct" à chaque micro-event,
        // on demande un filtrage "prochain tick" (regroupement)
        Runnable update = this::requestFilter;
        redEditor.setOnCurveChanged(update);
        greenEditor.setOnCurveChanged(update);
        blueEditor.setOnCurveChanged(update);

        redHost.getChildren().add(redEditor);
        greenHost.getChildren().add(greenEditor);
        blueHost.getChildren().add(blueEditor);
    }

    @FXML
    private void onQuit() {
        Platform.exit();
    }

    @FXML
    private void onMakeLinear() {
        redModel.makeLinear();
        greenModel.makeLinear();
        blueModel.makeLinear();

        redEditor.redraw();
        greenEditor.redraw();
        blueEditor.redraw();

        requestFilter();
    }

    @FXML
    private void onOpenImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Ouvrir une image JPEG");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images JPEG", "*.jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(imageView.getScene().getWindow());
        if (file == null) return;

        originalImage = new Image(file.toURI().toString()); // ✅ stocke l’originale
        requestFilter();                                     // ✅ affiche la filtrée
    }

    /**
     * Regroupe les demandes de filtrage pour éviter de recalculer
     * l'image des centaines de fois par seconde pendant un drag.
     */
    private void requestFilter() {
        if (originalImage == null) return;
        if (filterScheduled) return;

        filterScheduled = true;
        Platform.runLater(() -> {
            filterScheduled = false;
            applyFilter();
        });
    }

    /**
     * Pré-calcule 3 LUTs (R,G,B) : pour chaque valeur 0..255, calcule la valeur filtrée.
     * Beaucoup plus rapide que d'appeler eval() pour chaque pixel.
     */
    private void recomputeLuts() {
        for (int v = 0; v < 256; v++) {
            lutR[v] = CModel.clamp255((int) Math.round(redModel.eval(v)));
            lutG[v] = CModel.clamp255((int) Math.round(greenModel.eval(v)));
            lutB[v] = CModel.clamp255((int) Math.round(blueModel.eval(v)));
        }
    }

    /**
     * Filtre l'image originale -> nouvelle WritableImage en appliquant
     * P'(a, pr(r), pg(g), pb(b)).
     */
    private void applyFilter() {
        if (originalImage == null) return;

        recomputeLuts();

        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        WritableImage dst = new WritableImage(width, height);
        var reader = originalImage.getPixelReader();
        var writer = dst.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);

                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8)  & 0xFF;
                int b = (argb)        & 0xFF;

                int r2 = lutR[r];
                int g2 = lutG[g];
                int b2 = lutB[b];

                int newArgb = (a << 24) | (r2 << 16) | (g2 << 8) | b2;
                writer.setArgb(x, y, newArgb);
            }
        }

        imageView.setImage(dst);
    }
}

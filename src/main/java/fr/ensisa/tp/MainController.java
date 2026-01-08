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

public final class MainController {

    @FXML private StackPane redHost;
    @FXML private StackPane greenHost;
    @FXML private StackPane blueHost;
    @FXML private ImageView imageView;

    private CModel redModel;
    private CModel greenModel;
    private CModel blueModel;

    private Image originalImage;
    private Image previousImage;

    private final int[] lutR = new int[256];
    private final int[] lutG = new int[256];
    private final int[] lutB = new int[256];

    private boolean filterScheduled;

    @FXML
    private void initialize() {
        int n = 6;

        redModel = new CModel(n);
        greenModel = new CModel(n);
        blueModel = new CModel(n);

        CEditor redEditor = new CEditor(redModel, Color.RED);
        CEditor greenEditor = new CEditor(greenModel, Color.GREEN);
        CEditor blueEditor = new CEditor(blueModel, Color.BLUE);

        Runnable update = this::requestFilter;
        redEditor.setOnCurveChanged(update);
        greenEditor.setOnCurveChanged(update);
        blueEditor.setOnCurveChanged(update);

        redHost.getChildren().setAll(redEditor);
        greenHost.getChildren().setAll(greenEditor);
        blueHost.getChildren().setAll(blueEditor);
    }

    @FXML
    private void onQuit() {
        Platform.exit();
    }

//    private void Return(){    }

    @FXML
    private void onMakeReset() {
        redModel.makeLinear();
        greenModel.makeLinear();
        blueModel.makeLinear();
        requestFilter();

        originalImage = null;
        imageView.setImage(previousImage);
        originalImage = previousImage;
        previousImage = null;
    }

    @FXML
    private void onOpenImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Ouvrir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png")
        );

        File file = fc.showOpenDialog(imageView.getScene().getWindow());
        if (file == null) return;
        previousImage = originalImage;
        originalImage = new Image(file.toURI().toString());
        requestFilter();
    }

    private void requestFilter() {
        if (originalImage == null) return;
        if (filterScheduled) return;

        filterScheduled = true;
        Platform.runLater(() -> {
            filterScheduled = false;
            applyFilter();
        });
    }

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
                int g = (argb >>> 8) & 0xFF;
                int b = (argb) & 0xFF;

                int r2 = lutR[r];
                int g2 = lutG[g];
                int b2 = lutB[b];

                int newArgb = (a << 24) | (r2 << 16) | (g2 << 8) | b2;
                writer.setArgb(x, y, newArgb);
            }
        }

        imageView.setImage(dst);
    }

    private void recomputeLuts() {
        for (int v = 0; v < 256; v++) {
            lutR[v] = CModel.clamp255((int) Math.round(redModel.eval(v)));
            lutG[v] = CModel.clamp255((int) Math.round(greenModel.eval(v)));
            lutB[v] = CModel.clamp255((int) Math.round(blueModel.eval(v)));
        }
    }
}

package fr.ensisa.tp;

import fr.ensisa.tp.model.CModel;
import fr.ensisa.tp.view.CEditor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;


public class MainController {

    @FXML private StackPane redHost;
    @FXML private StackPane greenHost;
    @FXML private StackPane blueHost;
    @FXML private ImageView imageView;


    private CModel redModel, greenModel, blueModel;
    private CEditor redEditor, greenEditor, blueEditor;

    @FXML
    private void initialize() {
        int n = 6; 

        redModel = new CModel(n);
        greenModel = new CModel(n);
        blueModel = new CModel(n);

        redEditor = new CEditor(redModel, Color.RED);
        greenEditor = new CEditor(greenModel, Color.GREEN);
        blueEditor = new CEditor(blueModel, Color.BLUE);

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

        Image img = new Image(file.toURI().toString());
        imageView.setImage(img);
    }
}

//MAJ
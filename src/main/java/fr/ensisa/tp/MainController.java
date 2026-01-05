package fr.ensisa.tp;

import fr.ensisa.tp.model.CModel;
import fr.ensisa.tp.view.CEditor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane editorHost;

    private CModel model;
    private CEditor editor;

    @FXML
    private void initialize() {
        // entre 4 et 8 (ex : 6)
        model = new CModel(6);
        editor = new CEditor(model);

        editorHost.getChildren().add(editor);
    }

    @FXML
    private void onQuit() {
        Platform.exit();
    }

    @FXML
    private void onMakeLinear() {
        model.makeLinear();
        editor.redraw();
    }
}

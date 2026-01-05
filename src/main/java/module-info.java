module fr.ensisa.tp {
    requires javafx.controls;
    requires javafx.fxml;

    opens fr.ensisa.tp to javafx.fxml;

    exports fr.ensisa.tp;
}

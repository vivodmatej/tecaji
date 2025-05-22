module com.example.tecaji {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.xml;

    opens com.example.tecaji to javafx.fxml;
    exports com.example.tecaji;
}
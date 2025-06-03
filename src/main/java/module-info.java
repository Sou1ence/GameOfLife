module com.apokalist.gameoflife2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.apokalist.gameoflife2 to javafx.fxml;
    exports com.apokalist.gameoflife2;
}
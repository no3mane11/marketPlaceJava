module org.example.logintestjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    
    requires javafx.graphics;
    requires java.sql;

    requires org.controlsfx.controls;
    //requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires opencv;

    opens org.example.logintestjavafx to javafx.fxml;
    exports org.example.logintestjavafx;
}
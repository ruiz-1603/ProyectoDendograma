module org.example {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example to javafx.fxml;
    opens controlador to javafx.fxml;
    exports org.example;
}
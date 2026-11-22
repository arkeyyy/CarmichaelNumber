module org.example.carmichael_number {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.carmichael_number to javafx.fxml;
    exports org.example.carmichael_number;
}
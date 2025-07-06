module inventorymanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.inventory to javafx.fxml;
    opens com.inventory.controller to javafx.fxml;
    opens com.inventory.model to javafx.base;

    exports com.inventory;
}
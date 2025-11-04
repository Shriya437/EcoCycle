/**
 * */
/**
 * */
module Eco_cycle_3 { // Your module name is correct
	
    // --- JavaFX Modules ---
    requires javafx.controls;
    requires javafx.fxml;

    // --- 'opens' statements ---
    
    // Opens controllers to FXML loader
    opens ecocycle.controller to javafx.fxml;
    
    // Opens main app to JavaFX graphics
    opens ecocycle to javafx.graphics;
    
    // Opens util package to FXML loader
    opens ecocycle.util to javafx.fxml;
    
    // Opens service package to FXML loader
    opens ecocycle.service to javafx.fxml;
    
    // --- THIS IS THE FIX ---
    // Opens the model package to TWO modules:
    // 1. javafx.fxml (so the loader can see it)
    // 2. javafx.base (so the TableView's PropertyValueFactory can read its methods)
    opens ecocycle.model to javafx.fxml, javafx.base;
}
/**
 * 
 */
/**
 * 
 */
module Eco_cycle_3 {
	// --- JavaFX Modules ---
	requires javafx.controls;
    requires javafx.fxml;

    // 2. Add these 'opens' lines
    // Make sure they match your package names perfectly (no underscores!)
    
    opens ecocycle.controller to javafx.fxml;
    opens ecocycle.model to javafx.fxml;
    opens ecocycle to javafx.graphics;
    opens ecocycle.util to javafx.fxml;
    opens ecocycle.service to javafx.fxml;
}
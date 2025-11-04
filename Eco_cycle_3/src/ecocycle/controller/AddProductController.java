// In file: src/ecocycle/controller/AddProductController.java
package ecocycle.controller;

import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class AddProductController {

    @FXML
    private TextField nameField;
    
    // --- THIS IS THE CHANGE ---
    @FXML
    private ComboBox<String> categoryComboBox; // Changed from TextField
    
    @FXML
    private TextField typeField; // This was categoryField
    // --- END OF CHANGE ---

    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label errorLabel;

    /**
     * Initializes the controller.
     * This method is called automatically to populate the ComboBox.
     */
    @FXML
    public void initialize() {
        // Get the categories from DataService and put them in the ComboBox
        categoryComboBox.setItems(FXCollections.observableArrayList(
            DataService.getRecyclingCategories()
        ));
    }

    /**
     * Handles the "Submit" button action.
     * Validates input and adds the new product via the DataService.
     */
    @FXML
    void handleSubmit(ActionEvent event) {
        // --- THIS IS THE CHANGE ---
        String name = nameField.getText();
        String category = categoryComboBox.getValue(); // Get value from ComboBox
        String type = typeField.getText(); // Get value from the specific type field
        String priceStr = priceField.getText();
        String description = descriptionArea.getText();

        // Validation
        if (name.isEmpty() || category == null || type.isEmpty() || priceStr.isEmpty()) {
            errorLabel.setText("Please fill in all required fields.");
            return;
        }
        // --- END OF CHANGE ---

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                errorLabel.setText("Price cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Price must be a valid number (e.g., 15000.00).");
            return;
        }

        // Use the DataService to add the product
        // We pass 'category' as the main category and 'type' as the sub-category
        DataService.addProduct(name, type, category, price, description);

        // On success, go back to the product list
        SceneNavigator.navigateTo(event, "/ecocycle/view/ProductList.fxml");
    }

    /**
     * Handles the "Cancel" button action.
     * Navigates back to the main dashboard.
     */
    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
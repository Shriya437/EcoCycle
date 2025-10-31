package ecocycle.controller;

import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AddProductController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label errorLabel;

    /**
     * Handles the "Submit" button action.
     * Validates input and adds the new product via the DataService.
     */
    @FXML
    void handleSubmit(ActionEvent event) {
        String name = nameField.getText();
        String type = typeField.getText();
        String category = categoryField.getText();
        String priceStr = priceField.getText();
        String description = descriptionArea.getText();

        // Validation
        if (name.isEmpty() || type.isEmpty() || category.isEmpty() || priceStr.isEmpty()) {
            errorLabel.setText("Please fill in all required fields (Name, Type, Category, Price).");
            return;
        }

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
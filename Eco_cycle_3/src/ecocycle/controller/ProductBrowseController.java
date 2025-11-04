// In file: src/ecocycle/controller/ProductBrowseController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.model.Role;
import ecocycle.model.User;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Optional;

public class ProductBrowseController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> nameCol;
    @FXML
    private TableColumn<Product, String> categoryCol;
    @FXML
    private TableColumn<Product, String> descCol;
    @FXML
    private TableColumn<Product, Double> priceCol;
    @FXML
    private Label infoLabel;
    @FXML
    private ComboBox<String> categoryFilterBox;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private CheckBox sortCheckBox;

    // --- "addReviewButton" FXML field is now REMOVED ---

    @FXML
    public void initialize() {
        // 1. Setup Table Columns
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        // 2. Populate Category Filter Box
        categoryFilterBox.getItems().add("All");
        categoryFilterBox.getItems().addAll(DataService.getRecyclingCategories());
        categoryFilterBox.setValue("All"); 

        // 3. Load initial product list
        loadAvailableProducts();

        // 4. All Table listener logic for the review button is REMOVED.
    }
    
    private void loadAvailableProducts() {
        productTable.setItems(FXCollections.observableArrayList(
            DataService.getAvailableProducts()
        ));
        infoLabel.setText("");
    }

    @FXML
    void handleFilterAndSort(ActionEvent event) {
        String category = categoryFilterBox.getValue();
        boolean sortByPrice = sortCheckBox.isSelected();
        double minPrice = 0;
        double maxPrice = Double.MAX_VALUE;

        try {
            if (!minPriceField.getText().isEmpty()) {
                minPrice = Double.parseDouble(minPriceField.getText());
            }
            if (!maxPriceField.getText().isEmpty()) {
                maxPrice = Double.parseDouble(maxPriceField.getText());
            }
            if (minPrice < 0 || maxPrice < 0) {
                infoLabel.setText("Prices cannot be negative.");
                infoLabel.setTextFill(Color.RED);
                return;
            }
        } catch (NumberFormatException e) {
            infoLabel.setText("Invalid price. Please enter numbers only.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        List<Product> filteredProducts = DataService.getAvailableProducts(category, minPrice, maxPrice, sortByPrice);
        productTable.setItems(FXCollections.observableArrayList(filteredProducts));
        infoLabel.setText(filteredProducts.size() + " products found.");
        infoLabel.setTextFill(Color.BLACK);
    }
    
    @FXML
    void handleClearFilters(ActionEvent event) {
        categoryFilterBox.setValue("All");
        minPriceField.clear();
        maxPriceField.clear();
        sortCheckBox.setSelected(false);
        loadAvailableProducts();
    }

    @FXML
    void handleAddToCart(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            infoLabel.setText("Please select a product to add to your cart.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        boolean success = DataService.addToCart(selected.getProductId());
        if (success) {
            infoLabel.setText("'" + selected.getName() + "' added to your cart!");
            infoLabel.setTextFill(Color.GREEN);
        } else {
            infoLabel.setText("Could not add item to cart.");
            infoLabel.setTextFill(Color.RED);
        }
    }
    
    // --- "handleAddReview" method is now REMOVED ---

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
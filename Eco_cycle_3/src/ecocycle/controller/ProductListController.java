// In file: src/ecocycle/controller/ProductListController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.model.ProductStatus;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button; // NEW
import javafx.scene.control.Label;  // NEW
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color; // NEW

public class ProductListController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> idCol;
    @FXML
    private TableColumn<Product, String> nameCol;
    @FXML
    private TableColumn<Product, String> categoryCol;
    @FXML
    private TableColumn<Product, Double> priceCol;
    @FXML
    private TableColumn<Product, ProductStatus> statusCol;
    @FXML
    private TableColumn<Product, String> descCol;

    // --- NEWLY ADDED ---
    @FXML
    private Button deleteButton;
    @FXML
    private Button undoButton;
    @FXML
    private Label infoLabel;
    // --- END OF NEW ---

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        loadUserProducts();
        updateButtonStates(); // Check stack on load
    }
    
    /**
     * Helper to reload products from the DataService
     */
    private void loadUserProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList(
                DataService.getProductsForCurrentUser()
        );
        productTable.setItems(products);
    }
    
    /**
     * Helper to check the undo stack and disable the button if empty
     */
    private void updateButtonStates() {
        undoButton.setDisable(!DataService.canUndoDelete());
    }

    // --- NEW METHODS ---
    @FXML
    void handleDeleteProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            infoLabel.setText("Please select a product to delete.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        // Use the new archive method
        boolean success = DataService.archiveProductForUndo(selected.getProductId());
        
        if (success) {
            infoLabel.setText("'" + selected.getName() + "' deleted. You can undo this.");
            infoLabel.setTextFill(Color.GREEN);
            loadUserProducts(); // Refresh table
            updateButtonStates(); // Enable the undo button
        } else {
            infoLabel.setText("Could not delete product (it may be sold/in recycling).");
            infoLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    void handleUndoDelete(ActionEvent event) {
        Product restored = DataService.restoreLastDeletedProduct();
        
        if (restored != null) {
            infoLabel.setText("'" + restored.getName() + "' has been restored.");
            infoLabel.setTextFill(Color.GREEN);
            loadUserProducts(); // Refresh table
            updateButtonStates(); // Disable undo if stack is now empty
        } else {
            infoLabel.setText("Nothing to undo.");
            infoLabel.setTextFill(Color.RED);
        }
    }
    // --- END OF NEW METHODS ---

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
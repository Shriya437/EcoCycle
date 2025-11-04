// In file: src/ecocycle/controller/CartController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button; // NEW IMPORT
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.List;

public class CartController {

    @FXML
    private TableView<Product> cartTable;
    @FXML
    private TableColumn<Product, String> nameCol;
    @FXML
    private TableColumn<Product, String> categoryCol;
    @FXML
    private TableColumn<Product, Double> priceCol;
    @FXML
    private Label totalLabel;
    @FXML
    private Label infoLabel;

    // --- NEWLY ADDED FXML FIELDS ---
    @FXML
    private Button purchaseButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button undoRemoveButton;
    // --- END OF NEW FXML FIELDS ---

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        // --- UPDATED LOGIC ---
        // Disable buttons by default
        purchaseButton.setDisable(true);
        removeButton.setDisable(true);
        
        // Add a listener to enable/disable buttons based on selection
        cartTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean itemSelected = (newSelection != null);
            purchaseButton.setDisable(!itemSelected);
            removeButton.setDisable(!itemSelected);
        });
        // --- END OF UPDATED LOGIC ---

        loadCart();
    }

    private void loadCart() {
        List<Product> cartItems = DataService.getCart();
        cartTable.setItems(FXCollections.observableArrayList(cartItems));

        // Calculate total
        double total = 0.0;
        for (Product item : cartItems) {
            total += item.getPrice();
        }
        totalLabel.setText(String.format("Total Cart Value: ₹%.2f", total));
        
        if (cartItems.isEmpty()) {
            infoLabel.setText("Your cart is empty.");
            infoLabel.setTextFill(Color.BLACK);
        } else {
            infoLabel.setText("Select an item to purchase or remove.");
            infoLabel.setTextFill(Color.BLACK);
        }
        
        // --- UPDATED LOGIC ---
        // After loading, ensure buttons are in the correct state
        purchaseButton.setDisable(true);
        removeButton.setDisable(true);
        // Check if the "Undo" stack has items
        undoRemoveButton.setDisable(!DataService.canUndoRemove());
        // --- END OF UPDATED LOGIC ---
    }

    /**
     * RENAMED from handlePurchase to handlePurchaseSelected
     */
    @FXML
    void handlePurchaseSelected(ActionEvent event) {
        // 1. Get ONLY the selected item
        Product selectedItem = cartTable.getSelectionModel().getSelectedItem();
        
        if (selectedItem == null) {
            infoLabel.setText("Please select an item from the cart to purchase.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        // 2. Call the new DataService method for a single item
        boolean success = DataService.purchaseSingleItem(selectedItem.getProductId());
        
        if (success) {
            // 3. Show a professional pop-up dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Purchase Successful");
            alert.setHeaderText("Your order for '" + selectedItem.getName() + "' has been placed!");
            alert.setContentText(String.format("Your total was ₹%.2f. Thank you!", selectedItem.getPrice()));
            alert.showAndWait();
            
            // 4. Reload the cart (the item will be gone)
            loadCart(); 
        } else {
            infoLabel.setText("Purchase failed. Item may be out of stock.");
            infoLabel.setTextFill(Color.RED);
        }
    }

    /**
     * NEW METHOD - This was missing and causing the crash.
     */
    @FXML
    void handleRemoveItem(ActionEvent event) {
        Product selectedItem = cartTable.getSelectionModel().getSelectedItem();
        
        if (selectedItem == null) {
            infoLabel.setText("Please select an item to remove.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        boolean success = DataService.removeItemFromCart(selectedItem.getProductId());
        
        if (success) {
            infoLabel.setText("'" + selectedItem.getName() + "' removed from cart.");
            infoLabel.setTextFill(Color.GREEN);
            loadCart(); // Refresh the cart
        } else {
            infoLabel.setText("Error: Could not remove item.");
            infoLabel.setTextFill(Color.RED);
        }
    }

    /**
     * NEW METHOD - This was also missing.
     */
    @FXML
    void handleUndoRemove(ActionEvent event) {
        Product restoredItem = DataService.restoreLastRemovedItem();
        
        if (restoredItem != null) {
            infoLabel.setText("'" + restoredItem.getName() + "' was added back to your cart.");
            infoLabel.setTextFill(Color.GREEN);
            loadCart(); // Refresh the cart
        } else {
            infoLabel.setText("Nothing to undo.");
            infoLabel.setTextFill(Color.RED);
            undoRemoveButton.setDisable(true);
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
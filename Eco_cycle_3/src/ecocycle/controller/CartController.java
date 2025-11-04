// In file: src/ecocycle/controller/CartController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

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
        totalLabel.setText(String.format("Total: ₹%.2f", total));
        
        if (cartItems.isEmpty()) {
            infoLabel.setText("Your cart is empty.");
            infoLabel.setTextFill(Color.BLACK);
        } else {
            infoLabel.setText("");
        }
    }

 // --- THIS IS THE UPDATED METHOD ---
    @FXML
    void handlePurchase(ActionEvent event) {
        // We get the cart *before* purchasing to calculate the total
        List<Product> cartItems = DataService.getCart();
        if (cartItems.isEmpty()) {
            infoLabel.setText("Your cart is empty.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        // Calculate total
        double total = 0.0;
        for (Product item : cartItems) {
            total += item.getPrice();
        }

        boolean success = DataService.purchaseCart();
        
        if (success) {
            // Professional success message
            infoLabel.setText(String.format("Purchase for ₹%.2f complete! Thank you.", total));
            infoLabel.setTextFill(Color.GREEN);
            loadCart(); // Reload the cart (which will now be empty)
        } else {
            infoLabel.setText("Purchase failed. Items may be out of stock.");
            infoLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
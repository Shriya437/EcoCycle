// In file: src/ecocycle/controller/ViewBidsController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.model.RecyclingBid;
import ecocycle.model.User;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.List;
// We no longer need java.util.Optional
// import java.util.Optional; 

public class ViewBidsController {

    @FXML
    private ListView<Product> productList;
    @FXML
    private Text bidDetailsText;
    @FXML
    private Button acceptBidButton;
    @FXML
    private Label infoLabel;

    private ObservableList<Product> biddableProducts;

    @FXML
    public void initialize() {
        // Load the list of products
        loadBiddableProducts();

        // Use a custom cell factory to show product name and highest bid
        productList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    RecyclingBid highestBid = product.getBids().peek();
                    setText(product.getName() + " (Highest: ₹" + (highestBid != null ? highestBid.bidPrice() : "N/A") + ")");
                }
            }
        });

        // Add a listener to the selection
        productList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayBidDetails(newSelection);
            } else {
                clearBidDetails();
            }
        });
        
        if (biddableProducts.isEmpty()) {
            infoLabel.setText("No products currently have bids.");
        }
    }

    private void loadBiddableProducts() {
        List<Product> products = DataService.getBiddableProductsForSeller();
        biddableProducts = FXCollections.observableArrayList(products);
        productList.setItems(biddableProducts);
    }

    private void displayBidDetails(Product product) {
        RecyclingBid highestBid = product.getBids().peek();
        if (highestBid != null) {
            
            // --- THIS IS THE FIX ---
            // We now get a User object directly, which might be null.
            User bidder = DataService.findUserById(highestBid.recyclerId());
            
            // We must check for null in the traditional way.
            String bidderName;
            if (bidder != null) {
                bidderName = bidder.getUsername();
            } else {
                bidderName = "Unknown";
            }
            // --- END OF FIX ---
            
            bidDetailsText.setText(
                "Product: " + product.getName() + "\n" +
                "Highest Bid: ₹" + highestBid.bidPrice() + "\n" +
                "From: " + bidderName
            );
            acceptBidButton.setDisable(false);
            infoLabel.setText("");
        } else {
            clearBidDetails();
        }
    }

    private void clearBidDetails() {
        bidDetailsText.setText("Select a product to see bid details.");
        acceptBidButton.setDisable(true);
    }

 // --- THIS IS THE UPDATED METHOD ---
    @FXML
    void handleAcceptBid(ActionEvent event) {
        Product selected = productList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            
            // Call the updated DataService method
            RecyclingBid winningBid = DataService.acceptBid(selected.getProductId());
            
            if (winningBid != null) {
                // Find the bidder's name for the success message
                User bidder = DataService.findUserById(winningBid.recyclerId());
                String bidderName = (bidder != null) ? bidder.getUsername() : "Unknown";
                
                // Professional success message
                infoLabel.setText(String.format("Sold '%s' to %s for ₹%.2f!",
                    selected.getName(), bidderName, winningBid.bidPrice()));
                infoLabel.setTextFill(Color.GREEN);
                
                // Refresh the list
                loadBiddableProducts();
                clearBidDetails();
            } else {
                infoLabel.setText("Error: Could not accept bid.");
                infoLabel.setTextFill(Color.RED);
            }
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
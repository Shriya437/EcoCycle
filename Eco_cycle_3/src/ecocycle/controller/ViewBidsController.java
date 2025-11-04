// In file: src/ecocycle/controller/ViewBidsController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.model.RecyclingBid;
import ecocycle.model.User;
import ecocycle.model.Role;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert; // NEW IMPORT
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.List;

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
        loadBiddableProducts();

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
            
            User bidder = DataService.findUserById(highestBid.recyclerId());
            String bidderName = (bidder != null) ? bidder.getUsername() : "Unknown";
            
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

    @FXML
    void handleAcceptBid(ActionEvent event) {
        Product selected = productList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            
            RecyclingBid winningBid = DataService.acceptBid(selected.getProductId());
            
            if (winningBid != null) {
                User bidder = DataService.findUserById(winningBid.recyclerId());
                String bidderName = (bidder != null) ? bidder.getUsername() : "Unknown";
                
                String successText = String.format("Sold '%s' to %s for ₹%.2f!",
                    selected.getName(), bidderName, winningBid.bidPrice());
                
                infoLabel.setText(successText);
                infoLabel.setTextFill(Color.GREEN);
                
                // --- THIS IS THE FIX FOR REQ 3 (Seller-side) ---
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Bid Accepted!");
                alert.setHeaderText("Sale to Recycler Successful!");
                alert.setContentText(successText);
                alert.showAndWait();
                // --- END OF FIX ---
                
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
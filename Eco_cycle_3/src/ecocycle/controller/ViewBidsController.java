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
import java.util.Optional;

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
            Optional<User> bidderOpt = DataService.findUserById(highestBid.recyclerId());
            String bidderName = bidderOpt.isPresent() ? bidderOpt.get().getUsername() : "Unknown";
            
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
            DataService.acceptBid(selected.getProductId());
            infoLabel.setText("Bid for '" + selected.getName() + "' accepted!");
            infoLabel.setTextFill(Color.GREEN);
            
            // Refresh the list
            loadBiddableProducts();
            clearBidDetails();
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
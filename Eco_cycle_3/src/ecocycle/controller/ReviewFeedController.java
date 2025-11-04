// In file: src/ecocycle/controller/ReviewFeedController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.model.Review;
import ecocycle.model.User;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReviewFeedController {

    @FXML
    private TableView<Review> reviewTable;
    @FXML
    private TableColumn<Review, String> productNameCol;
    @FXML
    private TableColumn<Review, String> reviewCol;
    @FXML
    private TableColumn<Review, String> buyerNameCol;
    @FXML
    private TableColumn<Review, String> sellerNameCol; 
    
    // --- 'ratingCol' has been REMOVED ---

    @FXML
    public void initialize() {
        
        // Simple property from Review.java
        reviewCol.setCellValueFactory(new PropertyValueFactory<>("text"));

        // 1. Look up the Product Name from the productId
        productNameCol.setCellValueFactory(cellData -> {
            Review currentReview = cellData.getValue();
            Product product = DataService.findProductById(currentReview.getProductId());
            
            if (product != null) {
                return new SimpleStringProperty(product.getName());
            } else {
                return new SimpleStringProperty("Unknown Product");
            }
        });
        
        // 2. Look up the Buyer Name from the buyerId
        buyerNameCol.setCellValueFactory(cellData -> {
            Review currentReview = cellData.getValue();
            User buyer = DataService.findUserById(currentReview.getBuyerId());
            
            if (buyer != null) {
                return new SimpleStringProperty(buyer.getUsername());
            } else {
                return new SimpleStringProperty("Unknown Buyer");
            }
        });
        
        // 3. Look up the Seller Name (a 2-step lookup)
        sellerNameCol.setCellValueFactory(cellData -> {
            Review currentReview = cellData.getValue();
            Product product = DataService.findProductById(currentReview.getProductId());
            
            if (product != null) {
                User seller = DataService.findUserById(product.getSellerId());
                if (seller != null) {
                    return new SimpleStringProperty(seller.getUsername());
                }
            }
            return new SimpleStringProperty("Unknown Seller");
        });
        
        // Load the feed from the DataService's LinkedList
        reviewTable.setItems(FXCollections.observableArrayList(
            DataService.getGlobalReviewFeed()
        ));
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
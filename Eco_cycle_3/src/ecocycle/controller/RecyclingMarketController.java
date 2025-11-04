// In file: src/ecocycle/controller/RecyclingMarketController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.model.RecyclingBid;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class RecyclingMarketController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> nameCol;
    @FXML
    private TableColumn<Product, String> categoryCol;
    @FXML
    private TableColumn<Product, String> descCol;
    @FXML
    private TableColumn<Product, Double> baseCostCol;
    @FXML
    private TableColumn<Product, Double> highestBidCol;
    @FXML
    private Label infoLabel;
    @FXML
    private Text selectedProductText;
    @FXML
    private TextField bidField;

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        baseCostCol.setCellValueFactory(new PropertyValueFactory<>("baseCost"));
        
        // Custom cell factory for highest bid
        highestBidCol.setCellValueFactory(cellData -> {
            RecyclingBid bid = cellData.getValue().getBids().peek();
            double highestBid = (bid != null) ? bid.bidPrice() : 0.0;
            return new javafx.beans.property.SimpleDoubleProperty(highestBid).asObject();
        });

        loadMarketProducts();

        // Add listener to show selected product
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> {
            if (n != null) {
                selectedProductText.setText("Selected: " + n.getName());
                bidField.setPromptText("Min bid: ₹" + n.getBaseCost());
            } else {
                selectedProductText.setText("Select a product from the table to bid.");
                bidField.setPromptText("e.g., 500");
            }
        });
    }

    private void loadMarketProducts() {
        productTable.setItems(FXCollections.observableArrayList(
            DataService.getEligibleProductsForBidding()
        ));
    }

    @FXML
    void handlePlaceBid(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            infoLabel.setText("Please select a product to bid on.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        double bidPrice;
        try {
            bidPrice = Double.parseDouble(bidField.getText());
        } catch (NumberFormatException e) {
            infoLabel.setText("Bid must be a valid number.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        boolean success = DataService.placeBid(selected.getProductId(), bidPrice);
        if (success) {
            infoLabel.setText("Bid placed successfully on '" + selected.getName() + "'!");
            infoLabel.setTextFill(Color.GREEN);
            loadMarketProducts(); // Refresh table to show new highest bid
            bidField.clear();
        } else {
            infoLabel.setText("Bid failed. Must be at or above base cost.");
            infoLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
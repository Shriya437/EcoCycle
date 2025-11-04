// In file: src/ecocycle/controller/ProductBrowseController.java
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
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        loadAvailableProducts();
    }

    private void loadAvailableProducts() {
        productTable.setItems(FXCollections.observableArrayList(
            DataService.getAvailableProducts()
        ));
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

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
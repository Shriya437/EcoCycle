// In file: src/ecocycle/controller/ManageRecyclingController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.model.ProductStatus;
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

public class ManageRecyclingController {

    @FXML
    private TableView<Product> eligibleTable;
    @FXML
    private TableColumn<Product, String> nameCol;
    @FXML
    private TableColumn<Product, String> categoryCol;
    @FXML
    private TableColumn<Product, Double> priceCol;
    @FXML
    private Label infoLabel;

    private ObservableList<Product> eligibleProducts;

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        loadEligibleProducts();
    }

    private void loadEligibleProducts() {
        eligibleProducts = FXCollections.observableArrayList(
                DataService.getEligibleProductsForApproval()
        );
        eligibleTable.setItems(eligibleProducts);
        
        if (eligibleProducts.isEmpty()) {
            infoLabel.setText("No products are currently eligible for recycling.");
            infoLabel.setTextFill(Color.BLACK);
        }
    }

    private Product getSelectedProduct() {
        Product selected = eligibleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            infoLabel.setText("Please select a product from the table first.");
            infoLabel.setTextFill(Color.RED);
        }
        return selected;
    }

    @FXML
    void handleApprove(ActionEvent event) {
        Product selected = getSelectedProduct();
        if (selected != null) {
            DataService.updateProductRecyclingStatus(selected.getProductId(), ProductStatus.PENDING_RECYCLING);
            infoLabel.setText("'" + selected.getName() + "' approved for recycling.");
            infoLabel.setTextFill(Color.GREEN);
            loadEligibleProducts(); // Refresh the table
        }
    }

    @FXML
    void handleDeny(ActionEvent event) {
        Product selected = getSelectedProduct();
        if (selected != null) {
            DataService.updateProductRecyclingStatus(selected.getProductId(), ProductStatus.AVAILABLE_NO_RECYCLE);
            infoLabel.setText("'" + selected.getName() + "' will remain for sale only.");
            infoLabel.setTextFill(Color.ORANGE);
            loadEligibleProducts(); // Refresh the table
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
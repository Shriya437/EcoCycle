// In file: src/ecocycle/controller/SubmitProofController.java
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

public class SubmitProofController {

    @FXML
    private TableView<Product> acquiredTable;
    @FXML
    private TableColumn<Product, String> idCol;
    @FXML
    private TableColumn<Product, String> nameCol;
    @FXML
    private TableColumn<Product, String> categoryCol;
    @FXML
    private TableColumn<Product, ProductStatus> statusCol;
    @FXML
    private Label infoLabel;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadAcquiredProducts();
    }

    private void loadAcquiredProducts() {
        // This method shows ALL purchased products
        acquiredTable.setItems(FXCollections.observableArrayList(
            DataService.getAcquiredProducts()
        ));
    }

    @FXML
    void handleSubmitProof(ActionEvent event) {
        Product selected = acquiredTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            infoLabel.setText("Please select a product to submit proof for.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        if (selected.getStatus() == ProductStatus.RECYCLED) {
            infoLabel.setText("Proof already submitted for this item.");
            infoLabel.setTextFill(Color.ORANGE);
            return;
        }

        boolean success = DataService.submitRecyclingProof(selected.getProductId());
        if (success) {
            infoLabel.setText("Proof submitted for '" + selected.getName() + "'. Credits awarded!");
            infoLabel.setTextFill(Color.GREEN);
            loadAcquiredProducts(); // Refresh table to show "RECYCLED"
        } else {
            infoLabel.setText("Error submitting proof.");
            infoLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
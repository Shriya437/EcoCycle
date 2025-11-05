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
import javafx.scene.control.Button; // NEW
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser; // NEW
import java.io.File; // NEW

public class SubmitProofController {

    // --- Fields from Version 1 ---
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

    // --- Fields from Version 2 ---
    @FXML
    private Button uploadProofBtn;
    @FXML
    private Label proofStatus;
    
    private File selectedFile;

    
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

    // --- Method from Version 2 ---
    @FXML
    private void handleUploadProof(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Recycling Proof");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        // Make sure to get the window from any button
        selectedFile = fileChooser.showOpenDialog(uploadProofBtn.getScene().getWindow());

        if (selectedFile != null) {
            proofStatus.setText("Selected file: " + selectedFile.getName());
            proofStatus.setTextFill(Color.GREEN);
            infoLabel.setText(""); // Clear any old errors
        } else {
            proofStatus.setText("No file selected!");
            proofStatus.setTextFill(Color.RED);
        }
    }


    // --- MERGED handleSubmitProof Method ---
    @FXML
    void handleSubmitProof(ActionEvent event) {
        
        // 1. Check for selected product (from V1 logic)
        Product selected = acquiredTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            infoLabel.setText("Please select a product to submit proof for.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        // 2. Check for uploaded file (from V2 logic)
        if (selectedFile == null) {
            infoLabel.setText("Please upload a proof file before submitting.");
            infoLabel.setTextFill(Color.RED);
            return;
        }
        
        // 3. Check for status (from V1 logic)
        if (selected.getStatus() == ProductStatus.RECYCLED) {
            infoLabel.setText("Proof already submitted for this item.");
            infoLabel.setTextFill(Color.ORANGE);
            return;
        }

        // 4. If all checks pass, award credits (from V1 logic)
        // We can pass the file path to the DataService if needed,
        // but for now, we just call the working method.
        boolean success = DataService.submitRecyclingProof(selected.getProductId());
        
        if (success) {
            infoLabel.setText("Proof submitted for '" + selected.getName() + "'. Credits awarded!");
            infoLabel.setTextFill(Color.GREEN);
            
            // Reset the UI
            loadAcquiredProducts(); // Refresh table
            selectedFile = null;
            proofStatus.setText("");
            
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
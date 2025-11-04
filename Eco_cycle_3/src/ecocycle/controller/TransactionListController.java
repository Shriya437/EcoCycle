// In file: src/ecocycle/controller/TransactionListController.java
package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.model.Transaction;
import ecocycle.model.TransactionStatus;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TransactionListController {

    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, String> idCol;
    @FXML
    private TableColumn<Transaction, String> productCol;
    @FXML
    private TableColumn<Transaction, Double> priceCol;
    @FXML
    private TableColumn<Transaction, TransactionStatus> statusCol;
    @FXML
    private TableColumn<Transaction, Long> timestampCol;
    @FXML
    private Button addReviewButton;
    @FXML
    private Label infoLabel;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        productCol.setCellValueFactory(cellData -> {
            String productId = cellData.getValue().getProductId();
            Product p = DataService.findProductById(productId);
            if (p != null) {
                return new SimpleStringProperty(p.getName());
            } else {
                return new SimpleStringProperty("[Product Deleted]");
            }
        });
        
        timestampCol.setCellFactory(column -> {
            return new TableCell<Transaction, Long>() {
                private final SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy 'at' hh:mm a");

                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        Date date = new Date(item);
                        setText(formatter.format(date));
                    }
                }
            };
        });

        loadTransactions();
    }

    private void loadTransactions() {
        List<Transaction> transactions = DataService.getTransactionsForBuyer();
        transactionTable.setItems(FXCollections.observableArrayList(transactions));
    }
    
    @FXML
    void handleAddReview(ActionEvent event) {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            infoLabel.setText("Please select a purchased item to review.");
            infoLabel.setTextFill(Color.RED);
            return;
        }

        Product p = DataService.findProductById(selected.getProductId());
        String productName = (p != null) ? p.getName() : selected.getProductId();

        TextInputDialog reviewDialog = new TextInputDialog("It was great!");
        reviewDialog.setTitle("Add Review");
        reviewDialog.setHeaderText("Adding review for: " + productName);
        reviewDialog.setContentText("Enter your review:");

        Optional<String> result = reviewDialog.showAndWait();
        
        if (result.isPresent() && !result.get().isEmpty()) {
            String reviewText = result.get();
            
            // --- THIS IS THE FIX ---
            // We now call the correct submitReview(productId, text)
            boolean success = DataService.submitReview(selected.getProductId(), reviewText);
            
            if (success) {
                infoLabel.setText("Review for '" + productName + "' submitted!");
                infoLabel.setTextFill(Color.GREEN);
            } else {
                infoLabel.setText("Failed to submit review.");
                infoLabel.setTextFill(Color.RED);
            }
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
// In file: src/ecocycle/controller/TransactionListController.java
package ecocycle.controller;

import ecocycle.model.Transaction;
import ecocycle.model.TransactionStatus;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
// import java.sql.Timestamp; // <-- 1. REMOVED THIS LINE
import java.util.List;

public class TransactionListController {

    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, String> idCol;
    @FXML
    private TableColumn<Transaction, String> productCol; // We'll fill this manually
    @FXML
    private TableColumn<Transaction, Double> priceCol;
    @FXML
    private TableColumn<Transaction, TransactionStatus> statusCol;
    
    // --- 2. THIS IS THE FIX ---
    // Changed from Timestamp to Long, which matches the 'long' in your Transaction record
    @FXML
    private TableColumn<Transaction, Long> timestampCol;
    
    // Note: DataService does not have getTransactionsForBuyer
    // We will add it.

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        // 'productId' in Transaction needs to be mapped to a product name
        // For simplicity, we'll show the ID. A real app would use a custom CellFactory
        productCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        loadTransactions();
    }

    private void loadTransactions() {
        // We need to add this method to DataService
        List<Transaction> transactions = DataService.getTransactionsForBuyer();
        transactionTable.setItems(FXCollections.observableArrayList(transactions));
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
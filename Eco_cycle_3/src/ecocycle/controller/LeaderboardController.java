// In file: src/ecocycle/controller/LeaderboardController.java
package ecocycle.controller;

import ecocycle.model.User;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class LeaderboardController {

    @FXML
    private TableView<User> sellerTable;
    @FXML
    private TableColumn<User, String> sellerNameCol;
    @FXML
    private TableColumn<User, Double> sellerSalesCol;
    @FXML
    private TableView<User> recyclerTable;
    @FXML
    private TableColumn<User, String> recyclerNameCol;
    @FXML
    private TableColumn<User, Double> recyclerCreditsCol;

    @FXML
    public void initialize() {
        // Setup Seller Table
        sellerNameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        sellerSalesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        sellerTable.setItems(FXCollections.observableArrayList(
            DataService.getSellerLeaderboard()
        ));

        // Setup Recycler Table
        recyclerNameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        recyclerCreditsCol.setCellValueFactory(new PropertyValueFactory<>("carbonCredits"));
        recyclerTable.setItems(FXCollections.observableArrayList(
            DataService.getRecyclerLeaderboard()
        ));
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
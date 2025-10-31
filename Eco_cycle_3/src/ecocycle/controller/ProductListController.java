package ecocycle.controller;

import ecocycle.model.Product;
import ecocycle.model.ProductStatus;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProductListController {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> idCol;
    @FXML
    private TableColumn<Product, String> nameCol;
    @FXML
    private TableColumn<Product, String> categoryCol;
    @FXML
    private TableColumn<Product, Double> priceCol;
    @FXML
    private TableColumn<Product, ProductStatus> statusCol;
    @FXML
    private TableColumn<Product, String> descCol;


    /**
     * Initializes the controller.
     * Sets up the TableView columns and populates the table
     * with the current user's products from the DataService.
     */
    @FXML
    public void initialize() {
        // 1. Set up the cell value factories.
        // The string "productId", "name", etc. must match the
        // getter methods in the Product.java model (e.g., getProductId(), getName()).
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // 2. Get the data from the service
        ObservableList<Product> products = FXCollections.observableArrayList(
                DataService.getProductsForCurrentUser()
        );

        // 3. Load the data into the table
        productTable.setItems(products);
    }

    /**
     * Handles the "Back" button action.
     * Navigates back to the main dashboard.
     */
    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
    }
}
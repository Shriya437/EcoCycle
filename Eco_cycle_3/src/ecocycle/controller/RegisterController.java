package ecocycle.controller;

import ecocycle.model.Role;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<Role> roleComboBox;

    @FXML
    private Label errorLabel;

    /**
     * Initializes the controller class.
     * This method is automatically called after the fxml file has been loaded.
     * It populates the role selection box.
     */
    @FXML
    public void initialize() {
        // Add all roles from the enum to the combo box
        roleComboBox.getItems().setAll(Role.values());
    }

    /**
     * Handles the register button action.
     * Validates input and registers a new user via the DataService.
     */
    @FXML
    void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        Role role = roleComboBox.getValue();

        // Input Validation
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        // Attempt to register using the DataService
        boolean success = DataService.register(username, password, role);

        if (success) {
            // Success, go to login screen
            SceneNavigator.navigateTo(event, "/ecocycle/view/Login.fxml");
        } else {
            // Failure (username taken)
            errorLabel.setText("Username is already taken.");
        }
    }

    /**
     * Handles the login hyperlink action.
     * Navigates back to the login screen.
     */
    @FXML
    void handleLoginLink(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Login.fxml");
    }
}
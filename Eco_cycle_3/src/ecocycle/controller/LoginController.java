package ecocycle.controller;

import ecocycle.model.User;
import ecocycle.service.DataService;
import ecocycle.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    /**
     * Handles the login button action.
     * It validates user credentials using the DataService.
     */
    @FXML
    void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty.");
            return;
        }

        // Use the DataService to authenticate
        User user = DataService.login(username, password);

        if (user != null) {
            // Successful login, navigate to the main dashboard
            SceneNavigator.navigateTo(event, "/ecocycle/view/Dashboard.fxml");
        } else {
            // Failed login
            errorLabel.setText("Invalid username or password.");
        }
    }

    /**
     * Handles the register hyperlink action.
     * Navigates to the registration screen.
     */
    @FXML
    void handleRegisterLink(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/ecocycle/view/Register.fxml");
    }
}
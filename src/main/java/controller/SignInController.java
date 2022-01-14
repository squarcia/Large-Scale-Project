package main.java.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import main.java.connection.*;
import main.java.entity.User;
import main.java.utils.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SignInController {

    public AnchorPane anchorRoot;

    public Button SignUp;
    public Button SignIn;

    @FXML private TextField us;
    @FXML private PasswordField pw;

    private void initialize() {

    }

    public void ShowSignUp(MouseEvent mouseEvent) throws IOException {

        URL url = new File("src/main/resources/FXML/SignUp.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);

        Stage stage = (Stage) SignUp.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }

    public void ShowAdminPanel() throws IOException {

        URL url = new File("src/main/resources/FXML/AdminPanel.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);

        Stage stage = (Stage) SignIn.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();

    }

    public void ShowHome() throws IOException {

        URL url = new File("src/main/resources/FXML/MainPage.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);

        Stage stage = (Stage) SignIn.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }

    public void login() throws IOException {

        String username = us.getText();
        String password = pw.getText();
        Session session;

        if(!us.getText().isEmpty() && !pw.getText().isEmpty()) {
            Utility.printTerminal("Value: " + us.getText() + "\nValue: " + pw.getText());

            ConnectionMongoDB conn = new ConnectionMongoDB();

            if (us.getText().equals("admin") && pw.getText().equals("admin")) {
                session = Session.getInstance();
                session.setLogUser(username);
                ShowAdminPanel();
            }else {

                boolean logged = conn.logInUser(username, password);
                if (logged) {
                    session = Session.getInstance();
                    session.setLogUser(username);
                    ShowHome();
                }
            }
            //clear TextField
            us.setText("");
            pw.setText("");

        }else {
            Utility.infoBox("Please, insert username and password.", "Error", "Empty fields!");
        }
    }
}
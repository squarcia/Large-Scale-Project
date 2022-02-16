package main.java.it.unipi.dii.largescale.secondchance.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import main.java.it.unipi.dii.largescale.secondchance.connection.ConnectionMongoDB;
import main.java.it.unipi.dii.largescale.secondchance.entity.User;
import main.java.it.unipi.dii.largescale.secondchance.utils.Session;
import main.java.it.unipi.dii.largescale.secondchance.utils.Utility;

import java.util.Objects;


public class AddProfileImageController {

    @FXML public Button btnSubmit;
    @FXML public TextField txtFieldURL;

    private User user;

    public void initialize() {
        user  = Session.getLoggedUser();
    }

    public void submit() {

        String url = btnSubmit.getText();

        if (Objects.equals(url, "")) {
            Utility.infoBox("The URL's photo is empty!", "Information", "Update failed.");
            return;
        }

        // Update MongoDB document
        ConnectionMongoDB.connMongo.submitNewProfileImg(url, user.getUsername());
        Utility.infoBox("The URL's photo has been uploaded correctly!", "Information", "Update done.");

    }

}

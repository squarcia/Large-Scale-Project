package main.java.it.unipi.dii.largescale.secondchance.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import main.java.it.unipi.dii.largescale.secondchance.connection.ConnectionMongoDB;
import main.java.it.unipi.dii.largescale.secondchance.entity.User;
import main.java.it.unipi.dii.largescale.secondchance.utils.Session;
import main.java.it.unipi.dii.largescale.secondchance.utils.Utility;


public class AddProfileImageController {

    @FXML public Button btnSubmit;
    @FXML public TextField txtFieldURL;

    private User user;

    public void initialize() {
        user  = Session.getLogUser();
    }

    public void submit() {

        String url = btnSubmit.getText();
        // Update MongoDB document
        ConnectionMongoDB.connMongo.submitNewProfileImg(url, user.getUsername());
        Utility.infoBox("The URL's photo has been uploaded correctly!", "Information", "Update done.");
    }



}
package main.java.it.unipi.dii.largescale.secondchance.controller;

import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.java.it.unipi.dii.largescale.secondchance.connection.ConnectionMongoDB;
import main.java.it.unipi.dii.largescale.secondchance.utils.Utility;
import org.bson.Document;
import java.io.IOException;
import java.util.ArrayList;

public class FollowController {

    public ArrayList<String> list;
    public BorderPane bp;
    public VBox box;
    public Pane prev, next;
    private int nPage = 1;
    private int scrollPage;
    String type_img;

    public void initialize(ArrayList<String> users){

        //System.out.println("User 0: " + followingUsers.get(0));
        type_img = "user";
        list = users;
        scrollPage = 0;
        box = new VBox(20);

        prev.setVisible(false);
        prev.setDisable(true);


        if (users.size() == 0) {
            Utility.infoBox("This profile has not following", "Information", "No following!");
            return;
        }
        /*
        for (int i = 0; i < users.size(); i++) {
            Document user = ConnectionMongoDB.connMongo.findUserByUsername(users.get(i));
            list.add(user);
        }
        */

        show();
    }

    public void show() {

        box.getChildren().clear();

        //if there are more than k insertions enable next button
        if (list.size() == scrollPage - 1) {
            next.setDisable(false);
            next.setVisible(true);
        }
        System.out.println("(show) INDEX: " + scrollPage);

        for (int i = 0; i < nPage && i < list.size(); i++)
            addUser();

        bp.setCenter(box);

    }

    private void addUser() {

        String user = list.get(scrollPage);

        HBox hb = new HBox();
        VBox det = new VBox();

        ImageView image = Utility.getGoodImage("./img/user.png", 150, type_img);
        Label username = new Label("Username: " + list.get(scrollPage));
        //Label city = new Label("City: " + list.get(scrollPage).getString("city"));

        det.getChildren().add(username);
        //det.getChildren().add(city);
        hb.getChildren().add(image);
        hb.getChildren().add(det);
        box.getChildren().add(hb);

        image.setOnMouseClicked(event->{
                    try {
                      showUserPage(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        GridPane.setHalignment(image, HPos.LEFT);
        GridPane.setHalignment(username, HPos.LEFT);
        //GridPane.setHalignment(city, HPos.LEFT);

        det.setStyle("-fx-padding: 0 100 0 50;");
        hb.setStyle(
                "-fx-padding: 20;" +
                        " -fx-background-color: rgb(230, 230, 255);");
        box.setStyle(
                "-fx-hgap: 10;" +
                        " -fx-vgap: 10;" +
                        " -fx-max-height: 180;" +
                        " -fx-min-width: 530;" +
                        " -fx-max-width: 600;");

        scrollPage++;
        System.out.println("(add) INDEX: " + scrollPage);
    }

    public void prevPage() {

        System.out.println("(prev) INDEX: " + scrollPage);
        box.getChildren().clear();
        scrollPage = Utility.prevPage(scrollPage, nPage, prev);
        if(scrollPage < list.size())
        {
            next.setDisable(false);
            next.setVisible(true);
        }
        show();

    }

    public void nextPage() {

        box.getChildren().clear();
        System.out.println("(next) INDEX: " + scrollPage);
        if (scrollPage == list.size()) {
            next.setDisable(true);
            next.setVisible(false);
        }
        show();

    }

    private void showUserPage(String username) {

        System.out.println("USERNAME show: " + username);
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ProfileController.class.getResource("/FXML/Profile.fxml"));

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setScene(new Scene(loader.load()));
            ProfileController controller = loader.getController();
            controller.initialize(username);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

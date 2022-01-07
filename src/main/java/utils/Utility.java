package main.java.utils;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;

import java.util.*;
import org.bson.*;



public class Utility {

    public static void infoBox(String infoMessage, String titleBar, String headerMessage)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titleBar);
        alert.setHeaderText(headerMessage);
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }


    public static void showUsers(GridPane list, ArrayList<Document> filter, int item) {

        list.getChildren().clear();

        ImageView image = new ImageView("file: /../../resources/img/user.png");
        Label username = new Label(filter.get(item).getString("username"));
        Label country = new Label(filter.get(item).getString("country"));
        Label city = new Label(filter.get(item).getString("city"));

        list.add(image, 0, 0);
        list.add(username, 0, 1);
        list.add(country, 0, 2);
        list.add(city, 0, 3);

        GridPane.setHalignment(username, HPos.CENTER);
        GridPane.setHalignment(country, HPos.CENTER);
        GridPane.setHalignment(city, HPos.CENTER);

        list.setStyle(
                "    -fx-padding: 20;\n" +
                        "    -fx-hgap: 10;\n" +
                        "    -fx-vgap: 10;");

    }


}

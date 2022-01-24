package main.java.controller;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import main.java.connection.ConnectionMongoDB;
import main.java.utils.Utility;
import org.bson.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.swing.event.ChangeListener;

public class StatsController {

    private String[] countries = new String[]{"Italy", "Canada", "Spain", "Austria", "Germany", "France", "Brazil", "Netherlands", "Poland", "Ireland", "United Kingdom (Great Britain)"};
    private String[] categories = new String[]{"clothing","accessories", "bags","beauty", "house", "jewelry", "kids", "shoes"};

    @FXML private RadioButton rBUsers;
    @FXML private RadioButton rBSellers;
    @FXML private RadioButton rBTopKUsers;
    @FXML private RadioButton rBTopKInterestingIns;
    @FXML private RadioButton rBTopKViewedIns;

    @FXML private TextField boxKNumber;
    @FXML private TextField txtFieldCountry;
    @FXML private TextField txtFieldCategory;

    @FXML private Button elaboraButton;

    public void initialize(){

        Tooltip countries = new Tooltip("Italy, Canada, Spain, Austria, Germany, France, Brazil, Netherlands, Poland, Ireland, United Kingdom");
        Tooltip categories = new Tooltip("clothing,accessories, bags, beauty, house, jewelry, kids, shoes");

        elaboraButton.setDisable(true);
        txtFieldCountry.setEditable(false);
        txtFieldCountry.setMouseTransparent(true);
        txtFieldCategory.setEditable(false);
        txtFieldCategory.setMouseTransparent(true);

        txtFieldCountry.setTooltip(countries);
        txtFieldCategory.setTooltip(categories);

        rBTopKViewedIns.selectedProperty().addListener((observable, oldValue, newValue) -> {
               System.out.println("radio button changed from " + oldValue + " to " + newValue);

               txtFieldCountry.setText("");
               txtFieldCountry.setEditable(false);
               txtFieldCountry.setMouseTransparent(true);

               elaboraButton.setDisable(true);

               if (!txtFieldCategory.isEditable()) {
                   txtFieldCategory.setEditable(true);
                   txtFieldCategory.setMouseTransparent(false);
               }
        });

       rBTopKInterestingIns.selectedProperty().addListener((observable, oldValue, newValue) -> {
           System.out.println("radio button changed from " + oldValue + " to " + newValue);

           txtFieldCountry.setText("");
           txtFieldCountry.setEditable(false);
           txtFieldCountry.setMouseTransparent(true);

           elaboraButton.setDisable(true);

           if (!txtFieldCategory.isEditable()) {
               txtFieldCategory.setEditable(true);
               txtFieldCategory.setMouseTransparent(false);
           }
       });

        rBTopKUsers.selectedProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("radio button changed from " + oldValue + " to " + newValue);

            txtFieldCategory.setText("");
            txtFieldCategory.setEditable(false);
            txtFieldCategory.setMouseTransparent(true);

            elaboraButton.setDisable(true);

            if (txtFieldCategory.isEditable()) {
                txtFieldCategory.setEditable(false);
                txtFieldCategory.setMouseTransparent(true);
            }

            txtFieldCountry.setEditable(true);
            txtFieldCountry.setMouseTransparent(false);

        });

        rBSellers.selectedProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("radio button changed from " + oldValue + " to " + newValue);

            txtFieldCountry.setText("");
            txtFieldCategory.setText("");

            txtFieldCountry.setEditable(false);
            txtFieldCountry.setMouseTransparent(true);

            txtFieldCategory.setEditable(false);
            txtFieldCategory.setMouseTransparent(true);

            elaboraButton.setDisable(false);
        });

        rBUsers.selectedProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("radio button changed from " + oldValue + " to " + newValue);

            txtFieldCountry.setText("");
            txtFieldCategory.setText("");

            txtFieldCountry.setEditable(false);
            txtFieldCountry.setMouseTransparent(true);

            txtFieldCategory.setEditable(false);
            txtFieldCategory.setMouseTransparent(true);

            elaboraButton.setDisable(false);
        });

        txtFieldCategory.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("text changed from " + oldValue + " to " + newValue);

            if (Objects.equals(newValue, ""))
                elaboraButton.setDisable(true);
            else
                elaboraButton.setDisable(false);
        });

        txtFieldCountry.textProperty().addListener((observable, oldValue, newValue) -> {
            Utility.printTerminal("text changed from " + oldValue + " to " + newValue);

            if (Objects.equals(newValue, ""))
                elaboraButton.setDisable(true);
            else
                elaboraButton.setDisable(false);
        });
    }

    public void redirectToStatFunction() throws IOException {

        ConnectionMongoDB conn = new ConnectionMongoDB();
        int k;
        // Section Most

        k = Integer.parseInt(boxKNumber.getText());

        if (rBSellers.isSelected())
            showMostActiveUsersSellers(conn,false, k);

        if (rBUsers.isSelected())
            showMostActiveUsersSellers(conn, true, k);

        // Section K

        if (Objects.equals(boxKNumber.getText(), "")) {
            Utility.infoBox("Please insert a valid K number", "Error", "Empty box!");
        } else {



            if (rBTopKUsers.isSelected())
                showTopKRatedUser(conn, k);

            if (rBTopKInterestingIns.isSelected())
                showTopKInterestingInsertion(conn, k);

            if(rBTopKViewedIns.isSelected())
                showTopKViewedInsertion(conn, k);
        }
    }

    public void showMostActiveUsersSellers(ConnectionMongoDB conn, boolean choice, int k) throws IOException{

        ArrayList<Document> array = conn.findMostActiveUsersSellers(k, choice);
        StackPane secondaryLayout = new StackPane();

        ListView<String> list = new ListView<String>();
        ObservableList items = FXCollections.observableArrayList();

        for (int i = 0; i < k; i++) {

            String str = array.get(i).getString("_id") + ": " + array.get(i).getInteger("count").toString();
            items.add(str);
        }

        list.setItems(items);

        try( FileInputStream imageStream = new FileInputStream("target/classes/img/secondchance.png") ) {
            Image image = new Image(imageStream);
            Scene secondScene = new Scene(secondaryLayout, 920, 400);

            // New window (Stage)
            Stage newWindow = new Stage();
            newWindow.setTitle("Top " + k);
            newWindow.getIcons().add(image);
            secondaryLayout.getChildren().add(list);
            newWindow.setScene(secondScene);

            newWindow.show();
        }
    }

    public void showTopKRatedUser(ConnectionMongoDB conn, int k) throws IOException {

        ArrayList<Document> array = new ArrayList<Document>();
        String country = txtFieldCountry.getText();

        if(!Arrays.asList(countries).contains(country))
            Utility.infoBox("Please insert a valid country", "Error", "Country not found!");

        array = conn.findTopKRatedUser(k, country);

        StackPane secondaryLayout = new StackPane();

        for (int i = 0; i < k; i++) {

            Label x = new Label(array.get(i).getString("username"));
            x.setTranslateX(10);
            x.setTranslateY(-100 + i*50);
            secondaryLayout.getChildren().add(x);

            Label y = new Label(Double.toString(array.get(i).getDouble("rating")));
            y.setTranslateX(100);
            y.setTranslateY(-100 + i*50);
            secondaryLayout.getChildren().add(y);
        }

        try( FileInputStream imageStream = new FileInputStream("target/classes/img/secondchance.png") ) {
            Image image = new Image(imageStream);
            Scene secondScene = new Scene(secondaryLayout, 1200, 800);

            // New window (Stage)
            Stage newWindow = new Stage();
            newWindow.getIcons().add(image);
            newWindow.setTitle("Top " + k);
            newWindow.setScene(secondScene);

            newWindow.show();
        }
    }

    public void showTopKInterestingInsertion(ConnectionMongoDB conn, int k) throws IOException {

        ArrayList<Document> array;
        String category = txtFieldCategory.getText();

        if(!Arrays.asList(categories).contains(category))
            Utility.infoBox("Please insert a valid category", "Error", "Category not found!");

        array = conn.findTopKInterestingInsertion(k, category);

        StackPane secondaryLayout = new StackPane();

        for (int i=0; i < k; i++) {
            Label x = new Label(array.get(i).getString("description"));
            x.setTranslateX(10);
            x.setTranslateY(-100 + i*50);
            secondaryLayout.getChildren().add(x);

            Label y = new Label(array.get(i).getInteger("interested").toString());
            y.setTranslateX(200);
            y.setTranslateY(-100 + i*50);
            secondaryLayout.getChildren().add(y);
        }

        try( FileInputStream imageStream = new FileInputStream("target/classes/img/secondchance.png") ) {
            Image image = new Image(imageStream);
            Scene secondScene = new Scene(secondaryLayout, 1200, 800);

            // New window (Stage)
            Stage newWindow = new Stage();
            newWindow.getIcons().add(image);
            newWindow.setTitle("Top " + k);
            newWindow.setScene(secondScene);

            newWindow.show();
        }
    }

    public void showTopKViewedInsertion(ConnectionMongoDB conn, int k) throws IOException {

        ArrayList<Document> array;

        String category = txtFieldCategory.getText();

        if(!Arrays.asList(categories).contains(category))
            Utility.infoBox("Please insert a valid category", "Error", "Category not found!");

        array = conn.findTopKViewedInsertion(k, category);

        StackPane secondaryLayout = new StackPane();

        for (int i=0; i < k; i++) {
            Label x = new Label(array.get(i).getString("description"));
            x.setTranslateX(10);
            x.setTranslateY(-100 + i*50);
            secondaryLayout.getChildren().add(x);

            Label y = new Label(array.get(i).getString("views"));
            y.setTranslateX(200);
            y.setTranslateY(-100 + i*50);
            secondaryLayout.getChildren().add(y);
        }

        try( FileInputStream imageStream = new FileInputStream("target/classes/img/secondchance.png") ) {
            Image image = new Image(imageStream);
            Scene secondScene = new Scene(secondaryLayout, 1200, 800);

            // New window (Stage)
            Stage newWindow = new Stage();
            newWindow.getIcons().add(image);
            newWindow.setTitle("Top " + k);
            newWindow.setScene(secondScene);

            newWindow.show();
        }
    }
}

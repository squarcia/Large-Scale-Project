package main.java.controller;

import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import main.java.connection.ConnectionMongoDB;
import main.java.entity.Review;
import main.java.entity.User;
import main.java.utils.Session;
import main.java.utils.Utility;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyOrderController{


    private static final int MAX_LENGTH = 50;
    public BorderPane ordersContainer;
    public ComboBox<String> comboBox;
    public Pane prevSearch;
    public Pane nextSearch;

    public ArrayList<Document> ordersList;
    public VBox panel;

    int indexPage;
    int k = 6;
    boolean kind;

    public void initialize(){

        //set buttons
        prevSearch.setDisable(true);
        nextSearch.setDisable(true);
        prevSearch.setVisible(false);
        nextSearch.setVisible(false);

        k = 4;
        indexPage = 1;

        ordersList = new ArrayList<>();
        panel = new VBox();
        panel.getChildren().clear();
        ordersContainer.setCenter(panel);

    }


    public void showOrders() {

        String type = comboBox.getValue();
        ConnectionMongoDB conn = new ConnectionMongoDB();

        Session session = Session.getInstance();
        User user = session.getLoggedUser();

        if(type.equals("Purchased"))
        {
            ordersList.clear();
            panel.getChildren().clear();
            indexPage = 1;
            ordersList = conn.findAllOrders(true, user.getUsername());
            kind = true;
            showAllOrders(true);
        }
        else if(type.equals("Sold"))
        {
            ordersList.clear();
            panel.getChildren().clear();
            indexPage = 1;
            ordersList = conn.findAllOrders(false, user.getUsername());
            kind = false;
            showAllOrders(false);
        }

        if(ordersList.size() > k)
        {
            nextSearch.setVisible(true);
            nextSearch.setDisable(false);
        }
        //comboBox.setValue("Select order");
    }

    private void showAllOrders(boolean choice) {

        for(int i = 0; i < k && indexPage <= ordersList.size(); i++)
        {
            addOrder(choice);
            indexPage++;
        }

        if(ordersList.size() < indexPage)
        {
            nextSearch.setDisable(true);
            nextSearch.setVisible(false);
        }

    }

    private void addOrder(boolean choice) {

        HBox hbox;
        HBox revbox;
        VBox vbox;
        Button review = new Button();
        Document ins = (Document) ordersList.get(indexPage-1).get("insertion");

        String sellerUser = ins.getString("seller");
        String orderTimestamp = ordersList.get(indexPage-1).getString("timestamp");
        Label buyer = new Label("Buyer: " + ordersList.get(indexPage-1).getString("buyer"));
        Label timestamp = new Label("Date Order: " + ordersList.get(indexPage-1).getString("timestamp"));
        Label seller = new Label("Seller: " + ins.getString("seller"));
        Label price = new Label("Price: " + ins.getDouble("price"));
        Label size = new Label("Size: " + ins.getString("size"));
        Label status = new Label("Status: " + ins.getString("status"));
        Label category = new Label("Category: " + ins.getString("category"));
        ImageView image = Utility.getGoodImage(ins.getString("image"), 110);

        if(ordersList.get(indexPage-1).getBoolean("reviewed") && choice) {
            review.setText("Already reviewed!");
            review.setDisable(true);
        }
        else
            review.setText("Review now!");

        if(choice) {
            vbox = new VBox(seller, timestamp, price, category, size, status);
            revbox = new HBox(vbox, review);
            hbox = new HBox(image, vbox, revbox);
            revbox.setSpacing(1000);
        }
        else {
            vbox = new VBox(buyer, timestamp, price, category, size, status);
            hbox = new HBox(image, vbox);
        }

        panel.setSpacing(12);
        hbox.setSpacing(80);
        hbox.setStyle("-fx-padding: 5px; -fx-background-color: white;");
        vbox.setStyle("-fx-padding: 5px; -fx-font-size: 14px");

        panel.getChildren().add(hbox);

        review.setOnMouseClicked(event-> {
                    addReview(sellerUser, orderTimestamp, review);
                }
        );
    }

    public void PrevOrder() {

        panel.getChildren().clear();
        System.out.println("INDEX: " + indexPage);
        int ind = (indexPage%5) == 0? 1 : (indexPage%5);
        indexPage-= (k + ind);
        System.out.println("INDEX: " + indexPage);

        nextSearch.setDisable(false);
        nextSearch.setVisible(true);
        System.out.println("FIND if: " + (indexPage < k));
        if(indexPage < k)
        {
            System.out.println("INDEX < k: " + indexPage);
            prevSearch.setDisable(true);
            prevSearch.setVisible(false);
        }

        showAllOrders(kind);

    }

    public void NextOrder() {

        panel.getChildren().clear();

        showAllOrders(kind);

        prevSearch.setDisable(false);
        prevSearch.setVisible(true);
    }

    private void addReview(String seller, String timestampOrder, Button revButton) {

        StackPane secondaryLayout = new StackPane();
        TextArea txtArea = new TextArea();
        TextField txtTitle = new TextField();
        ComboBox<String> rating = new ComboBox();

        rating.getSelectionModel().select("rating");
        rating.getItems().add("1");
        rating.getItems().add("2");
        rating.getItems().add("3");
        rating.getItems().add("4");
        rating.getItems().add("5");

        txtArea.setPrefHeight(150);
        txtArea.setPrefWidth(250);

        txtTitle.setPrefHeight(10);
        txtTitle.setPrefWidth(100);

        Button sendReview = new Button();
        sendReview.setText("Send review");
        sendReview.setStyle("-fx-border-width: 50");

        txtTitle.setPromptText("Title review");
        txtArea.setPromptText("Max 150 characters");

        HBox hbox = new HBox(txtTitle, rating);
        VBox vbox = new VBox(hbox, txtArea, sendReview);
        hbox.setSpacing(10);
        vbox.setSpacing(10);

        secondaryLayout.getChildren().add(vbox);
        Scene secondScene = new Scene(secondaryLayout, 400, 200);

        // New window
        Stage newWindow = new Stage();
        newWindow.setTitle("Review user");
        newWindow.setScene(secondScene);
        newWindow.show();

        sendReview.setOnMouseClicked(event-> {

            if (txtArea.getText().trim().equals("") || txtTitle.getText().trim().isEmpty() || rating.getValue().equals("rating")) {
                Utility.infoBox("Insert all the data", "User Alert", "Empty fields");
                return;
            }

            if(txtArea.getText().length() > MAX_LENGTH)
            {
                Utility.infoBox("Review with too many characters", "User Alert", "Incorrect field");
                return;
            }

            sendReview(txtArea, txtTitle, sendReview, revButton, Integer.parseInt(rating.getValue()), seller, timestampOrder);
        });


    }

    public void sendReview(TextArea txtArea, TextField txtTitle, Button sendReview, Button revButton, int rating, String seller, String timestampOrder){

        Session session = Session.getInstance();
        String reviewer = session.getLoggedUser().getUsername();

        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        String timestamp = date.format(new Date());
        System.out.println("timestamp: " + timestamp);

        Review rev = new Review(reviewer, seller, txtArea.getText(), timestamp, txtTitle.getText(), rating);
        ConnectionMongoDB conn = new ConnectionMongoDB();
        conn.addReview(rev);
        conn.updateSellerRating(rev.getSeller());
        conn.updateOrder(timestampOrder);
        sendReview.setDisable(true);
        revButton.setText("Already reviewed!");
        revButton.setDisable(true);
    }
}

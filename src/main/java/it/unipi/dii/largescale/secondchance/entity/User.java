 package main.java.it.unipi.dii.largescale.secondchance.entity;

import main.java.it.unipi.dii.largescale.secondchance.connection.ConnectionMongoDB;
import main.java.it.unipi.dii.largescale.secondchance.utils.Utility;
import org.bson.Document;

import java.util.ArrayList;

public class User implements GeneralUser {

    String email;
    String username;
    String password;
    String name;
    String country;
    String city;
    String image;
    String address;
    boolean suspended;
    double rating;
    double balance;
    ArrayList<Document> reviews;
    ArrayList<Document> sold;
    ArrayList<Document> purchased;

    public User(String email, String username, String password, String name, String country, String city, String address, boolean suspended, Double rating, double balance, String image, ArrayList<Document> reviews, ArrayList<Document> sold, ArrayList<Document> purchased) {

        this.email = email;
        this.username = username;
        this.password = password;
        this.name = name;
        this.country = country;
        this.city = city;
        this.image = image;
        this.address = address;
        this.suspended = suspended;
        if(rating == null)
            this.rating = Double.NaN;
        else
            this.rating = rating;
        this.balance = balance;
        this.reviews = reviews;
        this.sold = sold;
        this.purchased = purchased;

    }

    public User()
    {
        this.username = null;
        this.password = null;
        this.name = null;
        this.country = null;
        this.image = null;
        this.city = null;
        this.address = null;
        this.email = null;
        this.reviews = null;
        this.sold = null;
        this.purchased = null;

    }
     public User(String username)
    {
        User user = ConnectionMongoDB.connMongo.findUserDetails(username);
        if(user.getSuspended())
            Utility.infoBox("You can't login because your account has been suspended.", "Error", "Account suspended!");
        else {
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.country = user.getCountry();
            this.address = user.getAddress();
            this.city = user.getCity();
            this.name = user.getName();
            this.image = user.getImage();
            this.suspended = user.getSuspended();
            this.rating = user.getRating();
            this.balance = user.getBalance();
            this.reviews = user.getReviews();
            this.sold = user.getSold();
            this.purchased = user.getPurchased();
        }
    }

    public static User fromDocument(Document user) {

        User us = new User(user.getString("email"), user.getString("username"), null,
                user.getString("name"), user.getString("country"), user.getString("city"), user.getString("address"),
                user.getBoolean("suspended"), user.getDouble("rating"), user.getDouble("balance"), user.getString("img_profile"),
                (ArrayList<Document>) user.get("reviews"), (ArrayList<Document>) user.get("sold"), (ArrayList<Document>) user.get("purchased"));
        return us;

    }

    public static User fromDocumentAdmin(Document user){

        User us = new User(null, user.getString("username"), null, null, null, null, null, false, Double.NaN, 0.0, null, null, null, null);
        return us;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSuspended(boolean suspended) { this.suspended = suspended; }

    public void setRating(Double rating) { this.rating = rating; }

    public void setBalance(Double balance){ this.balance = balance;}

    public void setImage(String image) { this.image = image; }

    public void setPurchased(ArrayList<Document> purchase) { this.purchased = purchased; }

    public void setSold(ArrayList<Document> sold) { this.sold = sold; }

    public void setReviews(ArrayList<Document> reviews) { this.reviews = reviews; }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public boolean getSuspended() { return suspended; }

    public Double getRating() { return rating; }

    public Double getBalance() {  return this.balance; }

    public String getImage() { return this.image; }

    public ArrayList<Document> getReviews() { return reviews; }

    public ArrayList<Document> getSold() { return sold; }

    public ArrayList<Document> getPurchased() {return purchased;}

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", image='" + image + '\'' +
                ", address='" + address + '\'' +
                ", suspended='" + suspended + '\'' +
                ", rating=" + rating +
                ", balance=" + balance +
                ", reviews=" + reviews +
                ", sold=" + sold +
                ", purchased=" + purchased +
                '}';
    }
}

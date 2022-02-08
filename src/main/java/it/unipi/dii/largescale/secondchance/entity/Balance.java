package main.java.it.unipi.dii.largescale.secondchance.entity;

import main.java.it.unipi.dii.largescale.secondchance.connection.ConnectionMongoDB;

public class Balance {

    public static Balance balance;
    String username;
    Double credit;

    public Balance(String username, Double credit){
        this.username = username;
        this.credit = credit;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public String getUsername() {
        return username;
    }

    public Double getCredit() {
        ConnectionMongoDB.connMongo.getBalance();
        return credit;
    }
}

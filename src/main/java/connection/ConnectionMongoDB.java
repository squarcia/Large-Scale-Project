package main.java.connection;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.ConnectionString;

import main.java.entity.*;
import main.java.utils.*;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class ConnectionMongoDB{

    private MongoClient mongoClient;
    private MongoDatabase db;
    MongoCursor<Document> cursor;

    /* ********* CONNECTION SECTION ********* */

    public void openConnection() {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        mongoClient = MongoClients.create(uri);
        db = mongoClient.getDatabase("local");
    }

    public void closeConnection() {
        mongoClient.close();
    }

    /* ********* USER SECTION ********* */

    public boolean logInUser(String username, String password) throws IOException {

        this.openConnection();

        if(!userAlreadyPresent(username, password)) {
            Utility.infoBox("Username or Password wrong, try again", "Error", "Try again");
            this.closeConnection();
            return false;
        } else{
            Utility.printTerminal("User found in db");
        }

        this.closeConnection();
        return true;
    }

    public boolean registerUser(User u) {

        this.openConnection();

        if (userAlreadyPresent(u.getUsername(), u.getPassword())) {
            Utility.infoBox("Please, choose another username and try again.", "Error", "Username already used!");
            return false;
        }

        MongoCollection<Document> myColl = db.getCollection("user");

        Document user = new Document("address", u.getAddress())
                .append("city", u.getCity())
                .append("country", u.getCountry())
                .append("email", u.getEmail())
                .append("name", u.getName())
                .append("password", u.getPassword())
                .append("username", u.getUsername())
                .append("suspended", u.getSuspended())
                .append("rating", u.getRating())
                .append("balance", u.getBalance());

        myColl.insertOne(user);

        this.closeConnection();
        return true;
    }

    private static Consumer<Document> printDocuments() {
        return doc -> System.out.println(doc.toJson());
    }

    public Document findUserByUsername(String username) {

        this.openConnection();
        MongoCollection<Document> myColl = db.getCollection("user");
        cursor = myColl.find(eq("username", username)).iterator();

        this.closeConnection();

        if (cursor.hasNext())
            return cursor.next();

        Utility.infoBox("There is no user with this username.", "Error", "Username not found!");
        return null;

    }

    private boolean userAlreadyPresent(String username, String password) {

        MongoCollection<Document> myColl = db.getCollection("user");
        cursor = myColl.find(and(eq("username", username),
                eq("password", password))).iterator();
        return cursor.hasNext();
    }

    public User findUserDetails(String username) {

        this.openConnection();
        User logUser = new User();
        MongoCollection<Document> myColl = db.getCollection("user");
        Document user = myColl.find(eq("username", username)).first();
        logUser.setUsername(user.getString("username"));
        logUser.setName(user.getString("name"));
        logUser.setEmail(user.getString("email"));
        logUser.setAddress(user.getString("address"));
        logUser.setCity(user.getString("city"));
        logUser.setCountry(user.getString("country"));
        logUser.setSuspended(user.getString("suspended"));
        logUser.setBalance(user.getDouble("balance"));
        logUser.setOrders((ArrayList<Order>) user.get("orders"));

        this.closeConnection();
        return logUser;
    }

    public ArrayList<Document> followedUserInsertions(ArrayList<String> usList) {

        this.openConnection();
        ArrayList<Document> insertions = new ArrayList<>();
        MongoCollection<Document> myColl = db.getCollection("insertion");

        //System.out.println("insertion id: " + usList);

        for (int i = 0; i < usList.size(); i++) {
            Document d = myColl.find(eq("uniq_id", usList.get(i))).first();
            insertions.add(d);
        }

        this.closeConnection();
        return insertions;
    }

    public ArrayList<Document> findViralInsertions(int k) {

        this.openConnection();
        ArrayList<Document> insertions = new ArrayList<>();
        MongoCollection<Document> myColl = db.getCollection("insertion");
        Bson match = match(eq("sold", "N"));
        Bson sort = sort(descending("interested"));
        //Bson project = project(fields(excludeId(), include("seller"), include("image_url"), include("status"), include("interested"), include("price"), include("uniq_id")));
        Bson limit = limit(k);

        //myColl.aggregate(Arrays.asList(sort,project ,limit));
        AggregateIterable<Document> r = myColl.aggregate(Arrays.asList(match, sort ,limit));

        for (Document document : r) {
            System.out.println("Find: " + document.getString("uniq_id"));
            insertions.add(document);
        }
        this.closeConnection();
        return insertions;
    }

    public ArrayList<Document> findUserByFilters(String country, String rating) {

        this.openConnection();
        MongoCollection<Document> myColl = db.getCollection("user");
        ArrayList<Document> users = new ArrayList<>();

        if(country.equals("country") && !rating.equals("rating"))
        {
             cursor  = myColl.find(eq("rating", Double.parseDouble(rating))).iterator();
        }
        else if(!country.equals("country") && rating.equals("rating"))
        {
             cursor  = myColl.find(eq("country", country)).iterator();
        }
        else{
             cursor  = myColl.find(and(eq("country", country),
                    eq("rating", Double.parseDouble(rating)))).iterator();
        }

        while(cursor.hasNext())
        {
            users.add(cursor.next());
        }

        this.closeConnection();
        return users;

    }

    private ArrayList<Document> partialSearch(int index, MongoCollection<Document> myColl, ArrayList<Document> insertions, String size, String price, String gender, String status, String category, String color) {

        switch (index) {
            case 0:
                cursor  = myColl.find(eq("size", size)).iterator();
                break;
            case 1:
                String[] split = price.split("-");
                Utility.printTerminal("Selected range: " + split[0]);
                if(split.length == 1) {
                    cursor  = myColl.find(gte("price", Double.parseDouble(split[0]))).iterator();
                } else {
                    Utility.printTerminal("Selected range: " + split[1]);
                    cursor  = myColl.find(and(gte("price", Double.parseDouble(split[0])),
                            lte("price", Double.parseDouble(split[1])))).iterator();
                }
                break;
            case 2:
                cursor  = myColl.find(eq("gender", gender)).iterator();
                break;
            case 3:
                cursor  = myColl.find(eq("status", status)).iterator();
                break;
            case 4:
                cursor  = myColl.find(eq("category", category)).iterator();
                break;
            case 5:
                cursor  = myColl.find(eq("color", color)).iterator();
                break;
            default:
                break;
        }
        while(cursor.hasNext())
            insertions.add(cursor.next());

        return insertions;
    }

    /* ********* INSERTION SECTION ********* */

    public ArrayList<Document> findInsertionByFilters(String size, String price, String gender, String status, String category, String color) {

        this.openConnection();
        ArrayList<Document> insertions = new ArrayList<>();
        MongoCollection<Document> myColl = db.getCollection("insertion");

        //the following variables are 1 if the relative filter is applied
        int sizeFilterOn, priceFilterOn, genderFilterOn, statusFilterOn, categoryFilterOn, colorFilterOn;
        sizeFilterOn = (size.equals("size")) ? 0 : 1;
        priceFilterOn = (price.equals("price")) ? 0 : 1;
        genderFilterOn = (gender.equals("gender")) ? 0 : 1;
        statusFilterOn = (status.equals("status")) ? 0 : 1;
        categoryFilterOn = (category.equals("category")) ? 0 : 1;
        colorFilterOn = (color.equals("color")) ? 0 : 1;

        int[] filter = {sizeFilterOn, priceFilterOn, genderFilterOn,
                statusFilterOn,categoryFilterOn, colorFilterOn};

        for (int i = 0; i < 6; i++) {
            if (filter[i] == 1)
                partialSearch(i, myColl, insertions, size, price, gender, status, category, color);
        }

        this.closeConnection();
        return insertions;
    }

    public ArrayList<Document> findInsertionBySeller(String seller) {

        this.openConnection();
        ArrayList<Document> insertions = new ArrayList<>();
        MongoCollection<Document> myColl = db.getCollection("insertion");

        cursor = myColl.find(eq("seller", seller)).iterator();
        while (cursor.hasNext())
            insertions.add(cursor.next());

        this.closeConnection();
        return insertions;
    }

    public ArrayList<Document> findInsertionByBrand(String brand) {

        this.openConnection();
        ArrayList<Document> insertions = new ArrayList<>();
        MongoCollection<Document> myColl = db.getCollection("insertion");

        cursor = myColl.find(eq("brand", brand)).iterator();
        while (cursor.hasNext())
            insertions.add(cursor.next());

        this.closeConnection();
        return insertions;
    }

    public Insertion findInsertion(String insertion_id) {

        this.openConnection();

        Insertion insertion = new Insertion();
        MongoCollection<Document> myColl = db.getCollection("insertion");
        Document insertion_found = myColl.find(eq("uniq_id", insertion_id)).first();

        insertion.setBrand(insertion_found.getString("brand"));
        insertion.setCountry(insertion_found.getString("country"));
        insertion.setCategory(insertion_found.getString("category"));
        insertion.setColor(insertion_found.getString("color"));
        insertion.setDescription(insertion_found.getString("description"));
        insertion.setGender(insertion_found.getString("gender"));
        insertion.setImage_url(insertion_found.getString("image_url"));
        insertion.setInterested(insertion_found.getInteger("interested"));
        insertion.setPrice(insertion_found.getDouble("price"));
        insertion.setViews(insertion_found.getInteger("views"));
        insertion.setSeller(insertion_found.getString("seller"));
        insertion.setSize(insertion_found.getString("size"));
        insertion.setStatus(insertion_found.getString("status"));
        insertion.setTimestamp(insertion_found.getString("timestamp"));

        this.closeConnection();
        return insertion;

    }

    public boolean buyCurrentInsertion(String insertion_id, String username, Double price, String seller, String image){

        this.openConnection();

        System.out.println("Username: " + username);
        ClientSession clientSession = mongoClient.startSession();

        MongoCollection<Document> userColl = db.getCollection("user");
        MongoCollection<Document> orderColl = db.getCollection("order");

        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        String timestamp = date.format(new Date());

        TransactionBody<String> txnFunc = () -> {

            Document balance = db.getCollection("user").find(eq("username", username)).first();
            double balanceBuyer = balance.getDouble("balance") - price;

            Bson filter = and(eq("username", username), gte("balance", price));
            Bson update = set("balance", balanceBuyer);

            //update buyer balance
            Document ret = db.getCollection("user").findOneAndUpdate(filter, update);

            if (ret == null) {
                this.closeConnection();
                Utility.infoBox("Cannot purchase, not enough balance", "Error", "Error purchase");
                return "Buyer has not enough balance";
            }

            //set insertion sold
            Bson filter1 = and(eq("uniq_id", insertion_id), eq("sold", "N"));
            Bson update1 = set("sold", "Y");

            Document ret1 = db.getCollection("insertion").findOneAndUpdate(filter1, update1);

            if (ret1 == null) {
                this.closeConnection();
                Utility.infoBox("Cannot purchase, insertion already sold", "Error", "Error purchase");
                return "Insertion already sold";
            }

            //update seller balance
            Bson filter2 = eq("username", seller);
            Bson update2 = inc("balance", price);

            Document ret3 = db.getCollection("user").findOneAndUpdate(filter2, update2);

            if (ret3 == null) {
                this.closeConnection();
                Utility.infoBox("Cannot buy product", "Error", "Error purchase");
                return "Cannot increment seller balance";
            }
            //find new order_id

            Document order = new Document()
                    .append("_id", new ObjectId())
                    .append("timestamp", timestamp)
                    .append("image", image)
                    .append("buyer", username)
                    .append("seller", seller)
                    .append("price", price);
            //insert new document into order collection
            try {
                orderColl.insertOne(order);

            } catch (MongoException me) {
                System.err.println("Unable to insert due to an error: " + me);
            }
            BasicDBObject query = new BasicDBObject();
            query.put("username",username);

            //if more than 4, find older
            if( (Document) userColl.find(and(exists("orders.4"),query)).first() != null) {
                //insert order into user document

                BasicDBObject pull_data = new BasicDBObject("$pull", "orders.1");
                userColl.findOneAndUpdate(query, Updates.popFirst("orders"));
            }
            Document ord = new Document().append("seller", seller).append("image", image).append("price",price).append("timestamp", timestamp);

            BasicDBObject push_data = new BasicDBObject("$push", new BasicDBObject("orders", ord));

            userColl.findOneAndUpdate(query, push_data);

            this.closeConnection();
            return "OK";
        };
        return executeTransaction(clientSession, txnFunc);
    }

    private boolean executeTransaction(ClientSession clientSession, TransactionBody<String> txnFunc) {

        String message = "";

        message = clientSession.withTransaction(txnFunc);

        System.out.println(message);

        return message.equals("OK");
    }

    public void updateNumInterested(String insertion_id, int i) {

        this.openConnection();

        Bson filter = eq("uniq_id", insertion_id);
        Bson update = inc("interested", i);

        db.getCollection("insertion").findOneAndUpdate(filter, update);

        this.closeConnection();
    }

    public void updateNumView(String uniq_id) {

        this.openConnection();

        Bson filter = eq("uniq_id", uniq_id);
        Bson update = inc("views", 1);

        db.getCollection("insertion").findOneAndUpdate(filter, update);

        this.closeConnection();
    }

    /* ********* ADMIN SECTION ********* */

    public Document verifyUserInDB(String username, boolean choice) {

        Document user;

        this.openConnection();
        MongoCollection<Document> myColl = db.getCollection("user");

        if (choice)
            user = myColl.find(eq("username", username)).first();
        else
            user = myColl.find(eq("name", username)).first();

        this.closeConnection();
        return user;
    }

    public Document verifyInsertionInDB(String id, boolean choice) {

        this.openConnection();

        Document insertion;
        MongoCollection<Document> myColl = db.getCollection("insertion");

        if (choice)
            insertion = myColl.find(eq("_id", id)).first();
         else
            insertion = myColl.find(eq("seller", id)).first();

        this.closeConnection();
        return insertion;
    }

    public ArrayList<Document> findMostActiveUsersSellers(int k, boolean choice) {
        // true = select the top k most active users
        // false = select the top k most active sellers

        this.openConnection();
        ArrayList<Document> array = new ArrayList<>();
        MongoCollection<Document> myColl;
        MongoCollection<Document> myCollUsr;

        if (choice)
            myColl = db.getCollection("insertion");
        else
            myColl = db.getCollection("orders");

        myCollUsr = db.getCollection("user");
        Bson limit = limit(k);
        AggregateIterable<Document> aggr  = myColl.aggregate(
                Arrays.asList(
                        Aggregates.group("$seller", Accumulators.sum("count", 1)),
                        Aggregates.sort(descending("count")),
                        limit
                )
        );

        for (Document document : aggr) {

            Document user = myCollUsr.find(eq("username", document.getString("_id"))).first();
            document.append("name", user.getString("name"));
            array.add(document);
        }

        this.closeConnection();

        return array;
    }

    public ArrayList<Document> findTopKRatedUser(int k, String country) {

        this.openConnection();
        ArrayList<Document> array = new ArrayList<>();
        MongoCollection<Document> myColl = db.getCollection("user");

        Bson limit = limit(k);
        AggregateIterable<Document> aggr  = myColl.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("country", country)),
                        Aggregates.sort(descending("rating")),
                        limit
                )
        );

        for (Document document : aggr) {

            document.append("name", document.getString("name"));
            document.append("rating", document.getDouble("rating"));
            array.add(document);
        }

        this.closeConnection();
        return array;
    }

    public ArrayList<Document> findTopKInterestingInsertion(int k, String category) {

        this.openConnection();
        ArrayList<Document> array = new ArrayList<>();
        MongoCollection<Document> myColl = db.getCollection("insertion");

        Bson limit = limit(k);
        AggregateIterable<Document> aggr  = myColl.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("category", category)),
                        Aggregates.sort(descending("interested")),
                        limit
                )
        );

        for (Document document : aggr)
            array.add(document);

        this.closeConnection();
        return array;
    }

    public ArrayList<Document> findTopKViewedInsertion(int k, String category) {

        this.openConnection();
        ArrayList<Document> array = new ArrayList<>();
        MongoCollection<Document> myColl = db.getCollection("insertion");

        Bson limit = limit(k);
        AggregateIterable<Document> aggr  = myColl.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("category", category)),
                        Aggregates.sort(descending("viewed")),
                        limit
                )
        );

        for (Document document : aggr)
            array.add(document);

        this.closeConnection();
        return array;
    }

    public void suspendUser(String username) {

        this.openConnection();

        MongoCollection<Document> myColl = db.getCollection("user");

        Document query = new Document().append("username",  username);
        Bson updates = Updates.combine(
                Updates.set("suspended", "Y"));
                UpdateOptions options = new UpdateOptions().upsert(true);

        try {
            UpdateResult result = myColl.updateOne(query, updates, options);
            System.out.println("Modified document count: " + result.getModifiedCount());
            System.out.println("Upserted id: " + result.getUpsertedId()); // only contains a value when an upsert is performed
        }catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }



    }

    public void unsuspendUser(String username) {

        this.openConnection();

        MongoCollection<Document> myColl = db.getCollection("user");
        Document query = new Document().append("username",  username);

        Bson updates = Updates.combine(
                Updates.set("suspended", "N"));
                UpdateOptions options = new UpdateOptions().upsert(true);

        try {
            UpdateResult result = myColl.updateOne(query, updates, options);
            System.out.println("Modified document count: " + result.getModifiedCount());
            System.out.println("Upserted id: " + result.getUpsertedId()); // only contains a value when an upsert is performed
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }

    public Insertion findInsertionDetails(String id) {

        this.openConnection();

        Insertion ins = new Insertion();
        MongoCollection<Document> myColl = db.getCollection("insertion");
        Document insertion = myColl.find(eq("_id", id)).first();

        ins.setCategory(insertion.getString("category"));
        ins.setPrice(Double.parseDouble(insertion.getString("price")));
        ins.setViews(Integer.parseInt(insertion.getString("views")));

        if (Objects.equals(insertion.getString("sold"), "N"))
            ins.setSold("Not sold");
        else
            ins.setSold("Sold");

        this.closeConnection();
        return ins;
    }

    public boolean findInsertionId (String id) {

        this.openConnection();
        MongoCollection<Document> myColl = db.getCollection("insertion");
        cursor = myColl.find(eq("uniq_id", id)).iterator();

        this.closeConnection();

        if (cursor.hasNext())
            return true;

        return false;
    }

    public boolean addInsertion(Insertion i) {

        this.openConnection();
        MongoCollection<Document> myColl = db.getCollection("insertion");
        Document ins = new Document("brand", i.getBrand())
                .append("category", i.getCategory())
                .append("color", i.getColor())
                .append("country", i.getCountry())
                .append("description", i.getDescription())
                .append("gender", i.getGender())
                .append("image_url", i.getImage_url())
                .append("interested", i.getInterested())
                .append("price", i.getPrice())
                .append("seller", i.getSeller())
                .append("size", i.getSize())
                .append("status", i.getStatus())
                .append("timestamp", i.getTimestamp())
                .append("uniq_id", i.getId())
                .append("views", i.getViews())
                .append("sold", i.getSold());
        myColl.insertOne(ins);
        this.closeConnection();
        return true;

    }
}

package main.java.it.unipi.dii.largescale.secondchance.connection;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import main.java.it.unipi.dii.largescale.secondchance.entity.Balance;
import main.java.it.unipi.dii.largescale.secondchance.entity.Insertion;
import main.java.it.unipi.dii.largescale.secondchance.entity.Review;
import main.java.it.unipi.dii.largescale.secondchance.entity.User;
import main.java.it.unipi.dii.largescale.secondchance.utils.Session;
import main.java.it.unipi.dii.largescale.secondchance.utils.Utility;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;

public class ConnectionMongoDB{

    private final String clusterAddress = "mongodb://172.16.4.114:27020,172.16.4.115:27020,172.16.4.116:27020/" +
            "?retryWrites=true&w=1&readPreferences=nearest&wtimeout=10000";

    public static ConnectionMongoDB connMongo;
    private MongoClient mongoClient;
    private MongoDatabase db;
    MongoCursor<Document> cursor;

    static MongoCollection<Document> userColl;
    static MongoCollection<Document> insertionColl;
    static MongoCollection<Document> codeColl;
    static MongoCollection<Document> balanceColl;

    /* ********* CONNECTION SECTION ********* */

    public void connectToVms(){
        mongoClient = MongoClients.create(clusterAddress);

        // Write concern at DB level
        db = mongoClient.getDatabase("lsmdb")
                .withWriteConcern(WriteConcern.W1);

        // Write Preferences at collection level
        userColl = db.getCollection("user");
        insertionColl = db.getCollection("insertion");
        codeColl = db.getCollection("code");

        balanceColl = db.getCollection("balance")
                .withWriteConcern(WriteConcern.W3);

    }

    public void connectToLocal(){
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        mongoClient = MongoClients.create(uri);
        db = mongoClient.getDatabase("local");

        userColl = db.getCollection("user");
        insertionColl = db.getCollection("insertion");
        codeColl = db.getCollection("code");
        balanceColl = db.getCollection("balance");
    }

    public void connectToAtlas(){
        ConnectionString uri = new ConnectionString("mongodb+srv://roots:1234@cluster0.n8fgw.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        mongoClient = MongoClients.create(uri);
        db = mongoClient.getDatabase("project");
        userColl = db.getCollection("user");
        insertionColl = db.getCollection("insertion");
        codeColl = db.getCollection("code");
        balanceColl = db.getCollection("balance");

    }

    public void openConnection() {

        connectToLocal();
        //connectToVms();
        //connectToAtlas();

        System.out.println("**************** USER ******************");
        System.out.println(userColl.countDocuments());
        System.out.println("**************** INSERTION ******************");
        System.out.println(insertionColl.countDocuments());
        System.out.println("**************** CODE ******************");
        System.out.println(codeColl.countDocuments());

        // Print the first document
        userColl.find().limit(1).forEach(printDocuments());

    }

    public void closeConnection() {
        mongoClient.close();
    }

    private static Consumer<Document> printDocuments() {
        return doc -> System.out.println(doc.toJson());
    }

    /* ************************* USER SECTION ************************* */
    //registration of the user, if the username is not already used
    public boolean registerUser(User u) {

        if (userAlreadyPresent(u.getUsername())) {
            Utility.infoBox("Please, choose another username and try again.",
                            "Error", "Username already used!");
            return false;
        }

        Document user = new Document("address", u.getAddress())
                .append("city", u.getCity())
                .append("country", u.getCountry())
                .append("email", u.getEmail())
                .append("img_profile", u.getImage())
                .append("name", u.getName())
                .append("password", u.getPassword())
                .append("suspended", u.getSuspended())
                .append("username", u.getUsername());

        Document balanceUser = new Document("username", u.getUsername())
                .append("credit", 0);

        userColl.insertOne(user);
        balanceColl.insertOne(balanceUser);

        return true;
    }

    //find user by username
    public Document findUserByUsername(String username) {

        cursor = userColl.find(eq("username", username)).iterator();

        return cursor.next();
    }

    //check if the username is already present
    public boolean userAlreadyPresent(String username) {

        cursor = userColl.find(eq("username", username)).iterator();
        return cursor.hasNext();

    }

    //check if the credentials are correct
    public boolean checkCredentials(String username, String encrypted) {

        cursor = userColl.find(and(eq("username", username), eq("password", encrypted))).iterator();

        return cursor.hasNext();
    }

    //find the user details of the specified user
    public User findUserDetails(String username) {

        User logUser = new User();
        Document user = userColl.find(eq("username", username)).first();
        logUser.setUsername(user.getString("username"));
        logUser.setName(user.getString("name"));
        logUser.setEmail(user.getString("email"));
        logUser.setAddress(user.getString("address"));
        logUser.setCity(user.getString("city"));
        logUser.setCountry(user.getString("country"));
        logUser.setSuspended(user.getBoolean("suspended"));
        logUser.setImage(user.getString("img_profile"));
        if((ArrayList<Document>) user.get("reviews") != null){
            logUser.setReviews((ArrayList<Document>) user.get("reviews"));
            logUser.setRating(user.getDouble("rating"));
        }
        if((ArrayList<Document>) user.get("sold") != null)
            logUser.setSold((ArrayList<Document>) user.get("sold"));
        if((ArrayList<Document>) user.get("purchased")!= null)
            logUser.setPurchased((ArrayList<Document>) user.get("purchased"));

        return logUser;
    }

    //find the insertions of the followed users
    public ArrayList<Document> followedUserInsertions(ArrayList<String> insList) {

        ArrayList<Document> insertions = new ArrayList<>();

        for (String s : insList) {
            Document d = insertionColl.find(eq("_id", new ObjectId(s))).first();
            insertions.add(d);
        }

        return insertions;
    }

    //filters the users by country and/or rating if specified
    public ArrayList<Document> findUserByFilters(String country, String rating) {

        ArrayList<Document> users = new ArrayList<>();
        double r ;
        double lowerBound = 0;
        double upperBound = 0;

        if(!rating.equals("rating")){
            r = Double.parseDouble(rating);
            lowerBound = r-0.5;
            upperBound = r+0.5;
        }

        if(country.equals("country") && !rating.equals("rating"))
        {
            cursor  = userColl.find(and(lte("rating", upperBound), gt("rating", lowerBound))).iterator();
        }
        else if(!country.equals("country") && rating.equals("rating"))
        {
            cursor  = userColl.find(eq("country", country)).iterator();
        }
        else{
            cursor  = userColl.find(and(eq("country", country), lte("rating", upperBound), gt("rating", lowerBound))).iterator();
        }

        while(cursor.hasNext())
        {
            users.add(cursor.next());
        }

        return users;

    }

    //delete the specified user
    public void deleteUserMongo(String username) {

        Bson query = eq("username", username);

        try {
            userColl.deleteOne(query);
        } catch (MongoException me) {
            System.err.println("Unable to delete due to an error: " + me);
        }
    }

    //updates the image profile of the specified user with the new url
    public int submitNewProfileImg(String url, String user) {

        Document queryUser = new Document().append("username",  user);

        Bson updatesUser = Updates.combine(
                Updates.set("img_profile", url)
        );

        UpdateOptions options = new UpdateOptions().upsert(true);

        try {
            UpdateResult resultUser = userColl.updateOne(queryUser, updatesUser, options);
            System.out.println("Modified document count: " + resultUser.getModifiedCount());
            System.out.println("Upserted id: " + resultUser.getUpsertedId()); // only contains a value when an upsert is performed
            return (int) resultUser.getModifiedCount();
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
            return 0;
        }
    }

    //updates the purchased and sold arrays of the logged user
    public void updateLoggedUser() {

        Bson filter = eq("username", Session.getLoggedUser().getUsername());
        Bson update = set("purchased", Session.getLoggedUser().getPurchased());
        userColl.findOneAndUpdate(filter, update);

        update = set("sold", Session.getLoggedUser().getSold());
        userColl.findOneAndUpdate(filter, update);

    }

    /* *********************** INSERTION SECTION *********************** */

    //find the insertion with most interested and views
    public ArrayList<Document> findViralInsertions(int k) {

        ArrayList<Document> insertions = new ArrayList<>();
        Bson sort = sort(descending("interested", "views"));
        Bson limit = limit(k);

        AggregateIterable<Document> r = insertionColl.aggregate(Arrays.asList(sort ,limit));

        for (Document document : r)
            insertions.add(document);

        return insertions;
    }

    //fidn the insertions with the specified charateristics
    public ArrayList<Document> findInsertionByFilters(String size, String price, String gender, String status, String category, String color) {

        ArrayList<Document> insertions = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();

        if(!size.equals("size")) {
            filters.add(Filters.eq("size", size));
        }
        if(!price.equals("price")) {
            String[] range = price.split("-");
            if(range.length == 1) {
                filters.add(Filters.gte("price", Double.parseDouble(range[0])));
            } else {
                filters.add(Filters.gte("price", Double.parseDouble(range[0])));
                filters.add(Filters.lte("price", Double.parseDouble(range[1])));
            }
        }
        if(!gender.equals("gender")) {
            filters.add(Filters.eq("gender", gender));
        }
        if(!status.equals("status")) {
            filters.add(Filters.eq("status", status));
        }
        if(!category.equals("category")) {
            filters.add(Filters.eq("category", category));
        }
        if(!color.equals("color")) {
            filters.add(Filters.eq("color", color));
        }

        cursor = insertionColl.find(Filters.and(filters)).iterator();
        while(cursor.hasNext())
            insertions.add(cursor.next());

        return insertions;
    }

    //find insertions of the specified user
    public ArrayList<Document> findInsertionBySeller(String seller) {

        ArrayList<Document> insertions = new ArrayList<>();

        cursor = insertionColl.find(eq("seller", seller)).iterator();
        while (cursor.hasNext())
            insertions.add(cursor.next());

        return insertions;
    }

    //find all the insertion with the specified brand or user ording by timestamp
    public ArrayList<Document> findInsertionBySearch(String s) {

        ArrayList<Document> insertions = new ArrayList<>();
        AggregateIterable<Document> aggr;

        Bson match = match(or(eq("brand", s), (eq("seller", s))));
        Bson sort = sort(descending("timestamp"));
        aggr = insertionColl.aggregate(
                        Arrays.asList(
                                match, sort
                        )
                );

        for (Document d : aggr)
            insertions.add(d);

        return insertions;
    }

    //find the specified insertion
    public Insertion findInsertion(String insertion_id) {

        Insertion insertion = new Insertion();
        Document insertion_found = insertionColl.find(eq("_id", new ObjectId(insertion_id))).first();

        if(insertion_found == null)
            return null;
        insertion.setId(insertion_found.get("_id").toString());
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

        return insertion;

    }

    //rollback of operations in case of failures
    public void rollBackInsertion(int i, String username, Insertion insertion) {

        for(; i < 4; i++) {
            switch (i) {
                case 0: //insert insertion again
                    insertionColl.insertOne(Insertion.toDocument(insertion));
                    System.out.println("CASE 0");
                    continue;
                case 1: //remove item from sold array in user
                    Bson filter_sold = eq("username", insertion.getSeller());
                    Bson update = Updates.popLast("sold");
                    userColl.findOneAndUpdate(filter_sold, update);
                    System.out.println("CASE 1");
                    continue;
                case 2: //decrement seller balance
                    updateBalance(insertion.getSeller(), insertion.getPrice(), '-');
                    System.out.println("CASE 2");
                    continue;
                case 3: //increment buyer balance
                    updateBalance(username, insertion.getPrice(), '+');
                    System.out.println("CASE 3");
                    continue;
                default:
                    break;
            }
        }

    }

    //purchase of insertion
    public boolean buyCurrentInsertion(String username, Insertion insertion){

        ClientSession clientSession = mongoClient.startSession();

        SimpleDateFormat date = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String timestamp = date.format(new Date());

        TransactionBody<String> txnFunc = () -> {

            double currentBalance = ConnectionMongoDB.connMongo.getBalance();
            double checkBalance = currentBalance - insertion.getPrice();

            //check if enough balance to do the purchase
            if (checkBalance < 0.0) {
                Utility.infoBox("Cannot purchase, not enough balance", "Error", "Error purchase");
                return "Buyer has not enough balance";
            }

            boolean upBuyer, upSeller;
            //update of buyer balance
            upBuyer = updateBalance(Session.getLoggedUser().getUsername(), insertion.getPrice(), '-');
            if(!upBuyer)
            {
                Utility.infoBox("Cannot buy product", "Error", "Error purchase");
                return "Cannot update buyer balance";
            } else {
                //update of seller balance
                upSeller = updateBalance(insertion.getSeller(), insertion.getPrice(), '+');
                if(!upSeller) {
                    rollBackInsertion(3, Session.getLoggedUser().getUsername(), insertion);
                    Utility.infoBox("Cannot buy product", "Error", "Error purchase");
                    return "Cannot update seller balance";
                }
            }

            //order purchased
            Document purchased = new Document()
                    .append("_id", new ObjectId())
                    .append("timestamp", timestamp)
                    .append("seller", insertion.getSeller())
                    .append("reviewed", false)
                    .append("insertion", new Document("image", insertion.getImage_url()).
                            append("price", insertion.getPrice()).
                            append("size", insertion.getSize()).
                            append("status", insertion.getStatus()).
                            append("category", insertion.getCategory()));

            //order sold
            Document sold = new Document()
                    .append("_id", new ObjectId())
                    .append("timestamp", timestamp)
                    .append("buyer", username)
                    .append("reviewed", false)
                    .append("insertion", new Document("image", insertion.getImage_url()).
                            append("price", insertion.getPrice()).
                            append("size", insertion.getSize()).
                            append("status", insertion.getStatus()).
                            append("category", insertion.getCategory()));

            //update sold orders seller user
            Bson filter_sold = eq("username", insertion.getSeller());
            BasicDBObject update_sold = new BasicDBObject("$push", new BasicDBObject("sold", sold));

            //insert new document into user collection
            try {
                userColl.findOneAndUpdate(filter_sold, update_sold);
            } catch (MongoException e) {
                rollBackInsertion(2, Session.getLoggedUser().getUsername(), insertion);
                return ("Unable to insert item in sold array: " + e);
            }

            //update local purchased array
            ArrayList<Document> purc;
            if(Session.getLoggedUser().getPurchased() != null)
                purc = Session.getLoggedUser().getPurchased();
            else
                purc = new ArrayList<>();
            purc.add(purchased);
            Session.getLoggedUser().setPurchased(purc);

            try {
                insertionColl.deleteOne(new Document("image_url", insertion.getImage_url()).append("seller", insertion.getSeller()).append("timestamp", insertion.getTimestamp()));
                return "OK";
            } catch (MongoException e) {
                rollBackInsertion(1, Session.getLoggedUser().getUsername(), insertion);
                return ("Unable to delete insertion: " + e);
            }
        };
        return executeTransaction(clientSession, txnFunc);
    }

    private boolean executeTransaction(ClientSession clientSession, TransactionBody<String> txnFunc) {

        String message = "";

        message = clientSession.withTransaction(txnFunc);

        System.out.println(message);

        return message.equals("OK");
    }

    //update num interesting for insertion
    public boolean updateNumInterested(String insertion_id, int i) {

        Bson filter = eq("_id", new ObjectId(insertion_id));
        Bson update = inc("interested", i);
        try {
            db.getCollection("insertion").findOneAndUpdate(filter, update);
            return true;
        }catch(MongoException me){
            return false;
        }
    }

    //update num views for insertion
    public void updateNumView(String uniq_id) {

        Bson filter = eq("_id", new ObjectId(uniq_id));
        Bson update = inc("views", 1);

        db.getCollection("insertion").findOneAndUpdate(filter, update);
    }


    /* ************************* ADMIN SECTION ************************* */

    //find the top rated users of the specified country
    public ArrayList<Document> findTopRatedUsersByCountry(String country) {

        ArrayList<Document> list = new ArrayList<>();

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("country", country);

        try (MongoCursor<Document> cursor = userColl.find(whereQuery).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                if (doc.get("rating") == null)
                    continue;
                list.add(doc);
            }
        }
        return list;
    }

    //find the user with specified username or name
    public Document verifyUserInDB(String username, boolean choice) {

        Document user;

        if (choice)
            user = userColl.find(eq("username", username)).first();
        else
            user = userColl.find(eq("name", username)).first();

        return user;
    }

    //find the insertion with specified
    public Document verifyInsertionInDB(String id, boolean choice) {

        Document insertion;

        if (choice)
            insertion = insertionColl.find(eq("_id", new ObjectId(id))).first();
        else
            insertion = insertionColl.find(eq("seller", id)).first();

        return insertion;
    }

    //fidn the users with most purchased or sold orders
    public ArrayList<Document> findMostActiveUsers(int k, boolean choice) {

        ArrayList<Document> orders = new ArrayList<>();
        AggregateIterable<Document> aggr;
        // true = select the top k users with more purchased orders
        if(choice) {

            Bson match = match(exists("purchased.0"));
            Bson projection = new Document("$size", "$purchased");
            Bson project = Aggregates.project(new Document("count", projection).append("username", "$username"));
            Bson sort = sort(descending("count"));
            Bson limit = limit(k);
            aggr = userColl.aggregate(
                    Arrays.asList(
                            match, project, sort, limit
                    )
            );
        }
        else        // false = select the top k with more purchased orders
        {
            Bson match = match(exists("sold.0"));
            Bson projection = new Document("$size", "$sold");
            Bson project = Aggregates.project(new Document("count", projection).append("username", "$username"));
            Bson sort = sort(descending("count"));
            Bson limit = limit(k);
            aggr = userColl.aggregate(
                    Arrays.asList(
                            match, project, sort, limit
                    )
            );
        }
        for (Document d : aggr)
            orders.add(d);

        return orders;
    }

    //find the k users with higher rate for a specified country
    public ArrayList<Document> findTopKRatedUser(int k, String country) {

        ArrayList<Document> array = new ArrayList<>();

        Bson limit = limit(k);
        Bson project = project(fields(excludeId(), include("username"), include("rating")));
        AggregateIterable<Document> aggr  = userColl.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("country", country)),
                        Aggregates.sort(descending("rating")),
                        project,
                        limit
                )
        );

        for (Document document : aggr)
            array.add(document);

        return array;
    }

    //topk insertions with higher interested for specified category
    public ArrayList<Document> findTopKInterestingInsertion(int k, String category) {

        ArrayList<Document> array = new ArrayList<>();

        Bson limit = limit(k);
        AggregateIterable<Document> aggr = insertionColl.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("category", category)),
                        Aggregates.sort(descending("interested")),
                        limit
                )
        );

        for (Document document : aggr)
            array.add(document);

        return array;
    }

    //find the insertions with specified country and category
    public ArrayList<Insertion> findInsertionsByCountryAndCategory(String country, String category) {

        ArrayList<Insertion> insertions = new ArrayList<>();
        List<Bson> filters = new ArrayList<>();

        filters.add(Filters.eq("category", category));
        filters.add(Filters.eq("country", country));

        cursor = insertionColl.find(Filters.and(filters)).iterator();

        while(cursor.hasNext()) {
            Document doc = cursor.next();
            Insertion ins = new Insertion();
            ins.setCategory(doc.getString("category"));
            ins.setPrice(doc.getDouble("price"));
            ins.setViews(doc.getInteger("views"));
            ins.setId(doc.get("_id").toString());
            ins.setImage_url(doc.getString("image_url"));
            insertions.add(ins);
        }

        return insertions;

    }

    //find top k insertions for the specified category
    public ArrayList<Document> findTopKViewedInsertion(int k, String category) {

        ArrayList<Document> array = new ArrayList<>();

        Bson limit = limit(k);
        AggregateIterable<Document> aggr  = insertionColl.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("category", category)),
                        Aggregates.sort(descending("views")),
                        limit
                )
        );

        for (Document document : aggr)
            array.add(document);

        return array;
    }

    //suspends the specified user
    public void suspendUser(String username) {

        Document query = new Document().append("username",  username);
        Bson updates = Updates.combine(
                Updates.set("suspended", true));
                UpdateOptions options = new UpdateOptions().upsert(true);

        try {
            UpdateResult result = userColl.updateOne(query, updates, options);
            System.out.println("Modified document count: " + result.getModifiedCount());
            System.out.println("Upserted id: " + result.getUpsertedId()); // only contains a value when an upsert is performed
        }catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }



    }

    //unsuspend the specified user
    public void unsuspendUser(String username) {

        Document query = new Document().append("username",  username);

        Bson updates = Updates.combine(
                Updates.set("suspended", false));
                UpdateOptions options = new UpdateOptions().upsert(true);

        try {
            UpdateResult result = userColl.updateOne(query, updates, options);
            System.out.println("Modified document count: " + result.getModifiedCount());
            System.out.println("Upserted id: " + result.getUpsertedId()); // only contains a value when an upsert is performed
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }

    public Insertion findInsertionDetails(String id) {

        Insertion ins = new Insertion();
        Document insertion = insertionColl.find(eq("_id", new ObjectId(id))).first();

        ins.setCategory(insertion.getString("category"));
        ins.setPrice(insertion.getDouble("price"));
        ins.setViews(insertion.getInteger("views"));

        return ins;
    }

    public ArrayList<Insertion> findMultipleInsertionDetails(String seller) {

        ArrayList<Insertion> array = new ArrayList<>();

        AggregateIterable<Document> aggr  = insertionColl.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("seller", seller))
                )
        );

        for (Document document : aggr) {
            Insertion ins = new Insertion();
            ins.setCategory(document.getString("category"));
            ins.setPrice(document.getDouble("price"));
            ins.setViews(document.getInteger("views"));
            ins.setId(document.get("_id").toString());
            ins.setImage_url(document.getString("image_url"));
            array.add(ins);
        }
        return array;
    }

    public ArrayList<Document> findInsertionDetailsNeo4J(ArrayList<String> followed_ins)  {

        //Insertion ins;
        ArrayList<Document> insertions = new ArrayList<>();

        for (String followed_in : followed_ins) {
            Document insertion = insertionColl.find(eq("_id", new ObjectId(followed_in))).first();
            /*
            ins = new Insertion();
            ins.setCategory(insertion.getString("category"));
            ins.setPrice(insertion.getDouble("price"));
            ins.setImage_url(insertion.getString("image_url"));
            ins.setViews(insertion.getInteger("views"));
            ins.setSeller(insertion.getString("seller"));
            ins.setId(insertion.get("_id").toString());
            */
            insertions.add(insertion);
        }
        return insertions;
    }

    //add insertion
    public void addInsertion(Insertion i) throws Exception {

        Document ins = Insertion.toDocument(i);
        insertionColl.insertOne(ins);

    }

    //add review
    public void addReview(Review rev) {

        Document review = new Document()
                .append("timestamp", rev.getTimestamp())
                .append("reviewer", rev.getReviewer())
                .append("title", rev.getTitle())
                .append("text", rev.getText())
                .append("rating", rev.getRating());

        System.out.println("REVIEW: " + review);
        BasicDBObject query = new BasicDBObject();
        query.put("username", rev.getSeller());

        BasicDBObject push_data = new BasicDBObject("$push", new BasicDBObject("reviews", review));

        userColl.findOneAndUpdate(query, push_data);
    }

    //updates the rating of the seller after a new review insertion
    public void updateSellerRating(String seller) {

        Document d = userColl.find(eq("username", seller)).first();
        List<Document> list = d.getList("reviews", Document.class);

        Double avg;
        int sum = 0;

        for (Document document : list)
            sum += document.getInteger("rating");

        avg = (double) sum / (double) list.size();

        // {$set: {"rating": avg}}
        Bson filter = eq("username", d.getString("username"));
        Bson update = set("rating", avg);;

        userColl.findOneAndUpdate(filter, update);
    }

    //insert new review into the review array of the reviewed user
    public void setInsertionReviewed(String timestamp) {

        BasicDBObject query = new BasicDBObject();
        query.put("username",Session.getLoggedUser().getUsername());
        query.put("purchased.timestamp", timestamp);
        BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("purchased.$.reviewed", true));
        userColl.findOneAndUpdate(query, update);

    }

    //delete the insertion
    public boolean deleteInsertionMongo(String id) {

        Bson query = eq("_id", new ObjectId(id));

        try {
            DeleteResult result = insertionColl.deleteOne(query);
            return (result.getDeletedCount() == 1);
        } catch (MongoException me) {
            System.err.println("Unable to delete due to an error: " + me);
            return false;
        }
    }

    /* ************************* BALANCE SECTION ************************* */

    //add the credit of the specified code into the logged user balance
    public void addFundsToWallet(String id_code) {

        Document code;

        code = codeColl.find(eq("code", id_code)).first();
        if (code == null) {
            Utility.infoBox("The code that you have inserted is not valid.", "Error", "Code doesn't exist!");
            return;
        }

        double creditToAdd = code.getInteger("credit");

        try {
            updateBalance(Session.getLoggedUser().getUsername(), creditToAdd, '+');
            Utility.infoBox("Deposit of " + code.getInteger("credit") + "€ euros successfully executed", "Success", "Deposit done!");
            deleteCode(code.getString("_id"));
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }

    //updates the balance of specified user with the credit
    public boolean updateBalance(String username, double credit, char c) {

        double updated;
        Bson query = eq("username", username);
        Bson update = null;

        switch(c) {
            case '+':
                update = inc("credit", credit);
                break;
            case '-':
                update = inc("credit", -credit);
                break;
            default:
                Utility.printTerminal("Operation not allowed.");
                break;
        }

        //update balance
        try {
            Document d = balanceColl.findOneAndUpdate(query, update);
            updated = d.getDouble("credit");
            Balance.balance.setCredit(updated);
            return true;
        } catch (MongoException me) {
            System.err.println("Unable to update " + username + "'s balance: " + me);
            return false;
        }
    }

    //get the balance of the logged user
    public double getBalance() {

        FindIterable<Document> cursor = null;
        try {
            Bson filter = Filters.eq("username", Session.getLoggedUser().getUsername());
            Bson projection = fields(include("credit"), excludeId());
            cursor = balanceColl.find(filter).projection(projection);
        } catch (MongoException me) {
            System.err.println("Unable to get balance from db: " + me);
        }
        return cursor.first().getDouble("credit");
    }

    //insert the balance document
    public boolean insertBalance(Balance b) {

        try {
            balanceColl.insertOne(b.toDocument());
            return true;
        } catch (MongoException me) {
            System.err.println("Unable to add new document in balance collection: " + me);
            return false;
        }

    }

    /* ************************* CODE SECTION ************************* */

    //delete the code specified
    private void deleteCode(String id) {

        Bson query = eq("_id", id);

        try {
            DeleteResult result = codeColl.deleteOne(query);
            System.out.println("Deleted document count: " + result.getDeletedCount());
        } catch (MongoException me) {
            System.err.println("Unable to delete due to an error: " + me);
        }

    }

}

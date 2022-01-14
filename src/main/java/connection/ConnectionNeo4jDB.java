package main.java.connection;

import main.java.entity.*;
import main.java.utils.Utility;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class ConnectionNeo4jDB implements AutoCloseable
{
    private Driver driver;
    String uri = "neo4j://127.0.0.1:7687";
    String user = "neo4j";
    String password = "2nd-chance";

    public void open()
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close()
    {
        driver.close();
    }

    public void addUser(final User u)
    {
        this.open();
        try ( Session session = driver.session() )
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MERGE (u:User {username: $username, country: $country})",
                        parameters("username", u.getUsername(),
                                "country", u.getCountry()));
                return null;
            });
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addInsertion(final Insertion i)
    {
        this.open();
        try ( Session session = driver.session() )
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MERGE (i:Insertion {uniq_id: $uniq_id, category: $category," +
                                "gender: $gender})",
                        parameters( "uniq_id", i.getId(), "category", i.getCategory(),
                                "gender", i.getGender()));
                return null;
            });
            this.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utility.printTerminal("Cannot create new insertion node");
        return false;
    }

    public void followUser(String follower, String followed) {
        try ( Session session = driver.session() )
        {
            this.open();
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u:User),(v) " +
                                "WHERE u.username = $username1 AND v.username = $username2 " +
                                "CREATE (u)-[:FOLLOWS]->(v)",
                        parameters( "username1", follower, "username2", followed));
                return null;
            });
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unfollowUser(String unfollower, String unfollowed) {
        try ( Session session = driver.session() )
        {
            this.open();
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u:User)-[rel:FOLLOWS]->(v)  " +
                                "WHERE u.username = $username1 AND v.username = $username2 " +
                                "DELETE rel",
                        parameters( "username1", unfollower, "username2", unfollowed));
                return null;
            });
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void likeAnInsertion(User u, Insertion i) {

    }

    public ArrayList<String> getSuggestedUsers(String username, String country, int k) {
        this.open();
        ArrayList<String> suggestions = new ArrayList<>();
        try (Session session = driver.session()) {

            String u = "72q0jrBM81n7vySAL";
            String c = "Austria";

            List<String> similar = session.readTransaction((TransactionWork<List<String>>) tx -> {

                Result result = tx.run( "MATCH (u:User)-[:FOLLOWS]->(m)-[:FOLLOWS]->(others) " +

                                "WHERE u.username = $username AND u.country = $country AND others.country = $country " +
                                "AND NOT (u)-[:FOLLOWS]->(others) " +
                                "RETURN others.username as SuggUsers " +
                                "LIMIT $k",
                        parameters("username", u,
                                "country", c,
                                "k", k));
/*
                List<String> similar = session.readTransaction((TransactionWork<List<String>>) tx -> {
                    Result result = tx.run( "MATCH (u:User)-[:FOLLOWS]->(m)-[:FOLLOWS]->(others) " +
                                "WHERE u.username = $username AND u.country = $country AND others.country = $country " +
                                "AND NOT (u)-[:FOLLOWS]->(others) " +
                                "RETURN others.username as SuggUsers " +
                                "LIMIT $k",
                            parameters( "username", username,
                                    "country", country,
                                    "k", k));
*/
                while (result.hasNext()) {
                    Record r = result.next();
                    suggestions.add(r.get("SuggUsers").asString());
                }
                return suggestions;
            });
            Utility.printTerminal("NEO4j\n" + similar);
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return suggestions;
    }


    public ArrayList<String> getFollowedInsertions(String username, int k) {

        this.open();
        ArrayList<String> followed = new ArrayList<>();
        try (Session session = driver.session()) {

            String u = "72q0jrBM81n7vySAL";
            //String c = "Austria";

            List<String> insertions = session.readTransaction((TransactionWork<List<String>>) tx -> {

                Result result = tx.run( "MATCH (u:User)-[:FOLLOWS]->(m)-[:POSTED]->(i:Insertion) " +
                                "WHERE u.username = $username " +
                                "RETURN i.uniq_id as SuggIns " +
                                "LIMIT $k",
                        parameters("username", u,
                                "k", k));

                while (result.hasNext()) {
                    Record r = result.next();
                    followed.add(r.get("SuggIns").asString());
                }
                return followed;
            });
            Utility.printTerminal("NEO4j\n" + insertions);
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return followed;

    }

    public void setFavouriteInsertion(String username, String insertion_id) {

        this.open();

        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run(
                        "MATCH (u:User {username: $username})" +
                                "CREATE (u)-[rel:INTERESTED]->(i: Insertion {uniq_id: $id})", parameters("username", username,
                                "id", insertion_id));
                return null;
            });

            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dislikeInsertion(String username, String insertion_id) {

        this.open();

        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run(
                        "MATCH (u:User { username: $username})-[r:INTERESTED]->(i :Insertion { uniq_id: $id})\n" +
                                "DELETE r", parameters("username", username,
                                "id", insertion_id));
                return null;
            });

            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean showIfInterested(String username, String insertion_id) {


        this.open();

        try (Session session = driver.session()) {
            Boolean relation = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run("MATCH (u:User { username: $username})-[r:INTERESTED]->(i :Insertion { uniq_id: $id})\n" +
                        "RETURN r", parameters("username", username,
                        "id", insertion_id));

                return result.hasNext();
            });
            System.out.println(relation);

            return relation;
        }
    }

    public boolean checkNewUser(String username) {
        this.open();
        boolean check = false;

        try(Session session = driver.session()) {
            check = session.writeTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run(
                        "MATCH (u:User)-[r:FOLLOWS]->(v) " +
                                "WHERE u.username = $username " +
                                "RETURN COUNT(r) AS NewUser;",
                        parameters( "username", username));

                Record r = result.next();
                if((r.get("NewUser").asInt()) == 0)
                    return true;
                return false;
            });
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return check;
    }

    public boolean checkIfFollows(String us1, String us2) {

        this.open();
        boolean check = false;

        try(Session session = driver.session()) {
            check = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result result = tx.run(
                        "MATCH  (p:User {username: '$username1'}), (b:User {username: '$username2'}) " +
                                "RETURN exists( (p)-[:FOLLOWS]-(b) ) AS Follows",
                        parameters( "username1", us1,
                                "username2", us2));

                /*Record r = result.next();
                if((r.get(0).asBoolean()))
                    return true;
                return false;*/
                return result.hasNext();
            });

            this.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return check;
    }

    public void followUnfollowButton(String text, String us1, String us2) {

        switch (text) {
            case "Follows":
                followUser(us1, us2);
                break;

            case "Unfollows":
                unfollowUser(us1, us2);
                break;

            default:
                break;
        }

    }

    public boolean createPostedRelationship(String node1, String node2) {

        this.open();
        try(Session session = driver.session()) {
                session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run(
                        "MATCH (u:User),(i:Insertion) " +
                                "WHERE u.username = $username AND i.uniq_id = $id " +
                                "CREATE (u)-[:POSTED]->(i)",
                        parameters( "username", node1,
                                "id", node2));
                return null;
            });
            this.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utility.printTerminal("Cannot create POSTED relationship");
        return false;

    }
}

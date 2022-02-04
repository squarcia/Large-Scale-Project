package main.java.it.unipi.dii.largescale.secondchance.utils;
import main.java.it.unipi.dii.largescale.secondchance.entity.GeneralUser;
import main.java.it.unipi.dii.largescale.secondchance.entity.*;
import org.bson.Document;

public class Session{

    private GeneralUser logUser;
    private static Session session = null;

    public Session(){}

    public static Session getInstance(){

        if(session == null)
            session = new Session();
        return session;
    }

    public void setLogUser(Document user, Boolean isAdmin){

        if(session == null)
            throw new RuntimeException("Session not already active");
        else { //check
            if(isAdmin)
                session.logUser = new Admin(user.getString("username"), null);
            else
                session.logUser = User.fromDocument(user);
        }
    }

    public static User getLogUser() {

        if(session == null)
            throw new RuntimeException("Session not already active");
        else
            return (User) session.logUser;
    }

    public User getLoggedUser() {
        if(session == null)
            throw new RuntimeException("Session is not active.");
        else
            return (User) session.logUser;
    }

    public void getLogoutUser() {

        logUser = null;
        session = null; // [1]
    }

}


// [1]: After this, if there is no reference to the object
//      it will be deleted by the garbage collector
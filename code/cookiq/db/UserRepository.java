/**
 * UserRepository.java
 * 
 * Handles all user-related operations in MongoDB:
 * - Register new users
 * - Retrieve existing users
 * - Update user data (preferences, liked/disliked recipes)
 */

package cookiq.db;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

import cookiq.utils.PasswordUtils;

public class UserRepository {

    private final MongoCollection<Document> users;

    public UserRepository() {
        // Get the 'users' collection from the MongoDB database
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.users = db.getCollection("users");
    }

    /**
     * Registers a new user if the username does not already exist.
     * 
     * @param username the desired username
     * @param password the plain-text password
     * @return true if registration succeeded, false if username already exists
     */
    public boolean registerUser(String username, String password) {
        if (username == null || password == null)
            return false;

        username = username.toLowerCase();

        if (users.find(eq("username", username)).first() != null) {
            return false; // User already exists
        }

        String passwordHash = PasswordUtils.sha256(password);

        // Save preferences as a JSON string to ensure compatability
        String prefJson = cookiq.utils.PreferencesUtils.toJsonString(new cookiq.models.Preferences());

        Document newUser = new Document("username", username)
                .append("passwordHash", passwordHash)
                .append("preferences", prefJson)
                .append("likedRecipes", new ArrayList<String>())
                .append("dislikedRecipes", new ArrayList<String>());

        users.insertOne(newUser);
        System.out.println("Registered new user: " + username);
        return true;
    }

    /**
     * Fetches a user document by username.
     * 
     * @param username the username to search for
     * @return the Document representing the user, or null if not found
     */
    public Document getUser(String username) {
        if (username == null)
            return null;
        return users.find(eq("username", username.toLowerCase())).first();
    }

    /**
     * Updates an existing user document in MongoDB.
     * 
     * @param username    the username to update
     * @param updatedUser the new Document representing the user
     */
    public void updateUser(String username, Document updatedUser) {
        if (username == null || updatedUser == null)
            return;
        users.replaceOne(eq("username", username.toLowerCase()), updatedUser);
        System.out.println("Updated user: " + username);
    }

    /**
     * Helper: Fetches the list of liked recipes for a user.
     * 
     * @param username the username to fetch liked recipes for
     * @return list of recipe names, empty if none
     */
    public List<String> getLikedRecipes(String username) {
        Document user = getUser(username);
        if (user == null)
            return new ArrayList<>();
        List<String> liked = user.getList("likedRecipes", String.class);
        return liked != null ? liked : new ArrayList<>();
    }

    /**
     * Helper: Fetches the list of disliked recipes for a user.
     * 
     * @param username the username to fetch disliked recipes for
     * @return list of recipe names, empty if none
     */
    public List<String> getDislikedRecipes(String username) {
        Document user = getUser(username);
        if (user == null)
            return new ArrayList<>();
        List<String> disliked = user.getList("dislikedRecipes", String.class);
        return disliked != null ? disliked : new ArrayList<>();
    }

    /**
     * Authenticates a user by comparing the entered password hash with the stored
     * one.
     * 
     * @param username The username to check
     * @param password The plain-text password
     * @return true if authentication succeeds, false otherwise
     */
    public boolean authenticateUser(String username, String password) {
        if (username == null || password == null)
            return false;

        username = username.toLowerCase();
        Document user = getUser(username);
        if (user == null)
            return false;

        String storedHash = user.getString("passwordHash");
        String enteredHash = cookiq.utils.PasswordUtils.sha256(password);

        // Timing-safe comparison
        return cookiq.utils.PasswordUtils.slowEquals(storedHash, enteredHash);
    }
}

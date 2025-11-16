/**
 * User.java
 *
 * Represents a user of CookIQ, storing username, password, preferences,
 * and history of liked/disliked recipes.
 */

package cookiq.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private Preferences preferences;
    private List<String> liked;
    private List<String> disliked;
    private List<String> seen;

    // Constructor for new users
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.preferences = new Preferences();
        this.liked = new ArrayList<>();
        this.disliked = new ArrayList<>();
        this.seen = new ArrayList<>();
    }

    // Constructor for loading users with liked recipes
    public User(String username, String password, Preferences preferences, List<String> likedRecipes, List<String> dislikedRecipes, List<String> seenRecipes) {
        this.username = username;
        this.password = password;
        this.preferences = preferences != null ? preferences : new Preferences();
        this.liked = likedRecipes != null ? new ArrayList<>(likedRecipes) : new ArrayList<>();
        this.disliked = dislikedRecipes != null ? new ArrayList<>(dislikedRecipes) : new ArrayList<>();
        this.seen = seenRecipes != null ? new ArrayList<>(seenRecipes) : new ArrayList<>();
        this.seen = new ArrayList<>();
    }

    // ==================== Getters ====================
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Preferences getPreferences() { return preferences; }

    // Return copies to prevent external modification
    public List<String> getLikedRecipes() { return new ArrayList<>(liked); }
    public List<String> getDislikedRecipes() { return new ArrayList<>(disliked);}
    public List<String> getSeenRecipes() {return new ArrayList<>(seen);}

    // ==================== Add/Remove ====================
    public void addLikedRecipe(String recipeId) {
        if (!liked.contains(recipeId)) {
            liked.add(recipeId);
            disliked.remove(recipeId);
            addSeenRecipe(recipeId);
        }
    }

    public void addDislikedRecipe(String recipeId) {
        if (!disliked.contains(recipeId)) {
            disliked.add(recipeId);
            liked.remove(recipeId);
            addSeenRecipe(recipeId);
        }
    }

    public void addSeenRecipe(String recipeId) {
        if (recipeId != null && !seen.contains(recipeId)) {
            seen.add(recipeId);
        }
    }

    public void clearSeenRecipes() {
        seen.clear();
    }

    public void removeLikedRecipe(String recipeId) {
        if (recipeId != null) {
            liked.remove(recipeId);
        }
    }

    public void removeDislikedRecipe(String recipeId) {
        if (recipeId != null) {
            disliked.remove(recipeId);
        }
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences != null ? preferences : new Preferences();
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", liked=" + liked +
                ", disliked=" + disliked + 
                ", seen=" + seen +
                '}';
    }
}

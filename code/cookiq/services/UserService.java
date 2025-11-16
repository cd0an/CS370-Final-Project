/**
 * UserService handles all account operations:
 * - Register new users
 * - Login existing users
 * - Save/load users from a file
 */

package cookiq.services;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import cookiq.db.RecipeRepository;
import cookiq.db.UserRepository;
import cookiq.models.Preferences;
import cookiq.models.Recipe;
import cookiq.models.User;
import cookiq.utils.PasswordUtils;
import cookiq.utils.PreferencesUtils;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        userRepository = new UserRepository();
    }

    // ==================== User Registration/Login ====================
    public boolean registerUser(String username, String password) {
        return userRepository.registerUser(username, password);
    }

    public boolean loginUser(String username, String password) {
        if (username == null || password == null) return false;
        username = username.trim().toLowerCase();
        password = password.trim();

        Document user = userRepository.getUser(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            return false;
        }

        String storedHash = user.getString("passwordHash");
        String enteredHash = PasswordUtils.sha256(password);

        System.out.println("Trying login for: " + username);
        System.out.println("Stored hash: " + storedHash);
        System.out.println("Entered hash: " + enteredHash);

        return PasswordUtils.slowEquals(storedHash, enteredHash);
    }

    // ==================== Full User Fetch ====================
    public User getUserByUsername(String username) {
        Document userDoc = userRepository.getUser(username);
        if (userDoc == null) return null;

        User user = new User(username, ""); // password not needed

        List<String> liked = userDoc.getList("likedRecipes", String.class);
        if (liked != null) user.getLikedRecipes().addAll(liked);

        List<String> disliked = userDoc.getList("dislikedRecipes", String.class);
        if (disliked != null) user.getDislikedRecipes().addAll(disliked);

         Object prefObj = userDoc.get("preferences");
        if (prefObj instanceof Document) {
            Document prefDoc = (Document) prefObj;
            user.getPreferences().copyPrefs(PreferencesUtils.fromDocument(prefDoc));
        } else if (prefObj instanceof String) {
            user.getPreferences().copyPrefs(PreferencesUtils.fromJsonString((String) prefObj));
        } else {
            user.setPreferences(new Preferences());
        }

        return user;
    }

    // ==================== Liked Recipes ====================
    public boolean addLikedRecipe(String username, String recipeName) {
        Document user = userRepository.getUser(username);
        if (user == null) return false;

        List<String> likedRecipes = user.getList("likedRecipes", String.class);
        if (likedRecipes == null) likedRecipes = new ArrayList<>();

        if (!likedRecipes.contains(recipeName)) {
            likedRecipes.add(recipeName);
            user.put("likedRecipes", likedRecipes);
            userRepository.updateUser(username, user);
            return true;
        }
        return false;
    }

    public List<String> getLikedRecipes(String username) {
        Document user = userRepository.getUser(username);
        if (user == null) return new ArrayList<>();
        List<String> likedRecipes = user.getList("likedRecipes", String.class);
        return likedRecipes != null ? likedRecipes : new ArrayList<>();
    }

    public boolean removeLikedRecipe(String username, String recipeName) {
        Document user = userRepository.getUser(username);
        if (user == null) return false;

        List<String> likedRecipes = user.getList("likedRecipes", String.class);
        if (likedRecipes == null) likedRecipes = new ArrayList<>();

        if (likedRecipes.remove(recipeName)) {
            user.put("likedRecipes", likedRecipes);
            userRepository.updateUser(username, user);
            return true;
        }
        return false;
    }

    public List<Recipe> getLikedRecipesFull(String username) {
        List<String> likedNames = getLikedRecipes(username);
        List<Recipe> likedRecipes = new ArrayList<>();
        if (likedNames.isEmpty()) return likedRecipes;

        List<Recipe> allRecipes = new RecipeRepository().getAllRecipes();
        for (Recipe r : allRecipes) {
            if (likedNames.contains(r.getName())) likedRecipes.add(r);
        }
        return likedRecipes;
    }

    // ==================== Disliked Recipes ====================
    public boolean addDislikedRecipe(String username, String recipeName) {
        Document user = userRepository.getUser(username);
        if (user == null) return false;

        List<String> dislikedRecipes = user.getList("dislikedRecipes", String.class);
        if (dislikedRecipes == null) dislikedRecipes = new ArrayList<>();

        if (!dislikedRecipes.contains(recipeName)) {
            dislikedRecipes.add(recipeName);
            user.put("dislikedRecipes", dislikedRecipes);
            userRepository.updateUser(username, user);
            return true;
        }
        return false;
    }

    public List<String> getDislikedRecipes(String username) {
        Document user = userRepository.getUser(username);
        if (user == null) return new ArrayList<>();
        List<String> dislikedRecipes = user.getList("dislikedRecipes", String.class);
        return dislikedRecipes != null ? dislikedRecipes : new ArrayList<>();
    }

    // ==================== User Preferences ====================
    public boolean saveUserPreferences(String username, Preferences prefs) {
        Document user = userRepository.getUser(username);
        if (user == null) return false;

        String prefStr = PreferencesUtils.toJsonString(prefs);
        user.put("preferences", prefStr);

        userRepository.updateUser(username, user);
        return true;
    }

    public Preferences getUserPreferences(String username) {
        Document user = userRepository.getUser(username);
        if (user == null) return new Preferences();

        Object prefObj = user.get("preferences");
        if (prefObj instanceof Document) {
            return PreferencesUtils.fromDocument((Document) prefObj);
        } else if (prefObj instanceof String) {
            return PreferencesUtils.fromJsonString((String) prefObj);
        }
        return new Preferences();
    }

    // ==================== Utility ====================
    public void printPreferences(Preferences prefs) {
        System.out.println("Vegetarian:" + prefs.isVegetarian());
        System.out.println("Keto:" + prefs.isKeto());
        System.out.println("Gluten Free:" + prefs.isGlutenFree());
        System.out.println("Low Calorie:" + prefs.isLowCalorie());
        System.out.println("High Calorie:" + prefs.isHighCalorie());
        System.out.println("High Protein:" + prefs.isHighProtein());
        System.out.println("Italian:" + prefs.isItalian());
        System.out.println("Mexican:" + prefs.isMexican());
        System.out.println("Asian:" + prefs.isAsian());
        System.out.println("American:" + prefs.isAmerican());
        System.out.println("Mediterranean:" + prefs.isMediterranean());
        System.out.println("Max Cook Time:" + prefs.getMaxCookTime());
        System.out.println("Max Budget:" + prefs.getMaxBudget());
        System.out.println("Available Ingredients:" + prefs.getAvailableIngredients());
    }
}

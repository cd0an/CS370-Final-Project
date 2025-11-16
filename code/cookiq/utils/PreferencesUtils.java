/**
 * 
 * This class converts the preferences class for the following
 * 1) Java object <---> JSON
 * 2) Java object <---> BSON
*/
package cookiq.utils;

import org.bson.Document;

import com.google.gson.Gson; //MongoDB Java Driver

import cookiq.models.Preferences; //Used to convert java objects to JSON and vice versa

public class PreferencesUtils {
    //Creates a Gson object to convert java objects <--> JSON in this class
    private static final Gson gson = new Gson();

    //Convert Preferences object to JSON string
    public static String toJsonString(Preferences prefs) {
        return prefs != null ? gson.toJson(prefs) : "{}";
    }

    //Convert JSON string to Preferences object
    public static Preferences fromJsonString(String json) {
        if (json == null || json.isEmpty()) return new Preferences(); // Empty json return default constructor for Preferences class
        return gson.fromJson(json, Preferences.class); // Else return preferences class with json data
    }

    //Convert Preferences to BSON Document for MongoDB
    public static Document toDocument(Preferences prefs) { return Document.parse(toJsonString(prefs)); }

    //Convert BSON Document to Preferences
    public static Preferences fromDocument(Document doc) {
        if (doc == null) return new Preferences();
        return gson.fromJson(doc.toJson(), Preferences.class);
    }
}

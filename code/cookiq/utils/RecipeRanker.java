/**
 * RecipeRanker.java
 *
 * Contains helper methods to score and rank recipes based on
 * how well they match user preferences.
 */

package cookiq.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cookiq.models.Preferences;
import cookiq.models.Recipe;

public class RecipeRanker {
    private List<Recipe> recipeDatabase;
    
    public RecipeRanker() {
        this.recipeDatabase = new ArrayList<>();
    }
    
    /**
     * Get recipe recommendations sorted by match score
     * NOTE: Dietary restrictions and health goals are already filtered as mandatory
     */
    public List<Recipe> getRecommendations(Preferences preferences) {
        List<ScoredRecipe> scoredRecipes = new ArrayList<>();
        
        for (Recipe recipe : recipeDatabase) {
            int score = calculateMatchScore(recipe, preferences);
            scoredRecipes.add(new ScoredRecipe(recipe, score));
        }
        
        // Sort recipes by score in descending order (best matches first)
        scoredRecipes.sort(Comparator.comparing(ScoredRecipe::getScore).reversed());
        
        // Extract sorted recipes 
        List<Recipe> recommendations = new ArrayList<>();
        for (ScoredRecipe scored : scoredRecipes) {
            recommendations.add(scored.getRecipe());
        }
        
        return recommendations;
    }
    
    /**
     * Calculate score based on non-mandatory preferences
     * Dietary restrictions and health goals are already handled as mandatory filters
     */
    private int calculateMatchScore(Recipe recipe, Preferences prefs) {
        int score = 0;
        
        // Cuisine preferences - high priority
        String cuisine = recipe.getCuisine() != null ? recipe.getCuisine().trim().toLowerCase() : "";

        if (prefs.isItalian() && cuisine.contains("italian")) score += 50;
        if (prefs.isMexican() && cuisine.contains("mexican")) score += 50;
        if (prefs.isAsian() && cuisine.contains("asian")) score += 50;
        if (prefs.isAmerican() && cuisine.contains("american")) score += 50;
        if (prefs.isMediterranean() && cuisine.contains("mediterranean")) score += 50;
        
        // Cooking time - medium priority 
        if (prefs.getMaxCookTime() > 0) {
            if (recipe.getCookTime() <= prefs.getMaxCookTime()) {
                score += 10;
            } else {
                // Gradual penalty for exceeding time limit
                int timeOver = recipe.getCookTime() - prefs.getMaxCookTime();
                int timePenalty = Math.min(timeOver / 5, 5);
                score -= timePenalty;
            }
        }
        
        // Budget - rmedium priority 
        if (prefs.getMaxBudget() > 0) {
            if (recipe.getCost() <= prefs.getMaxBudget()) {
                score += 10;
            } else {
                // Penalty for exceeding budget
                double costOver = recipe.getCost() - prefs.getMaxBudget();
                int costPenalty = Math.min((int)(costOver / 2), 10);
                score -= costPenalty;
            }
        }
        
        return Math.max(score, 0);
    }
    
    private static class ScoredRecipe {
        private Recipe recipe;
        private int score;
        
        public ScoredRecipe(Recipe recipe, int score) {
            this.recipe = recipe;
            this.score = score;
        }
        
        public Recipe getRecipe() { return recipe; }
        public int getScore() { return score; }
    }
    
    public void setRecipeDatabase(List<Recipe> recipes) {
        this.recipeDatabase = new ArrayList<>(recipes);
    }
}
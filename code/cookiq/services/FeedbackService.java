/**
 * FeedbackService.java
 *
 * Processes user likes/dislikes and updates future recommendations.
 */
package cookiq.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cookiq.models.Preferences;
import cookiq.models.Recipe;
import cookiq.models.User;

public class FeedbackService {
    private Set<String> seenRecipeIds;
    private Map<String, Integer> recipeFeedback; // recipeId -> score (1 for like, -1 for dislike)
    private RecommendationService recommendationService;
    
    public FeedbackService(RecommendationService recommendationService) {
        this.seenRecipeIds = new HashSet<>();
        this.recipeFeedback = new HashMap<>();
        this.recommendationService = recommendationService;
    }

    public void markRecipeAsSeen(Recipe recipe) {
        if (recipe != null && recipe.getId() != null) {
            seenRecipeIds.add(recipe.getId());
        }
    }
    
    /**
     * Record user feedback for a recipe
     */
    public void recordFeedback(User user, Recipe recipe, boolean liked) {
        if (recipe != null && recipe.getId() != null) {

            String recipeId = recipe.getId();
            recipeFeedback.put(recipeId, liked ? 1 : -1);
            markRecipeAsSeen(recipe);
            seenRecipeIds.add(recipeId);

            // Update the User object
            if (liked) {
                user.addLikedRecipe(recipeId);
            } else {
                user.addDislikedRecipe(recipeId);
            }
        }
    }
    
    /**
     * Get new suggestions 
     */
    public List<Recipe> getNewSuggestions(Preferences preferences, User user) {
        List<Recipe> allRecommendations = recommendationService.getRecommendations(preferences, user);
        List<Recipe> newSuggestions = new ArrayList<>();
        
        // Collect unseen recipes
        for (Recipe recipe : allRecommendations) {
            if (recipe.getId() != null && !seenRecipeIds.contains(recipe.getId())) {
                newSuggestions.add(recipe);
            }
        }
        
        System.out.println("New suggestions: " + newSuggestions.size() + " (filtered from " + 
                          allRecommendations.size() + " total recommendations)");
        return newSuggestions;
    }
    
    /**
     * Get recommendations based on feedback (ML-like feedback loop)
     * Prefers recipes similar to liked ones and avoids disliked patterns
     */
    public List<Recipe> getFeedbackBasedRecommendations(Preferences preferences, User user) {
        List<Recipe> baseRecommendations = getNewSuggestions(preferences, user);
        
        // Simple feedback-based reordering
        List<ScoredRecipe> rescored = new ArrayList<>();
        
        for (Recipe recipe : baseRecommendations) {
            int feedbackScore = calculateFeedbackScore(recipe);
            rescored.add(new ScoredRecipe(recipe, feedbackScore));
        }
        
        // Sort by feedback score
        rescored.sort(Comparator.comparing(ScoredRecipe::getScore).reversed());
        
        List<Recipe> finalRecommendations = new ArrayList<>();
        for (ScoredRecipe sr : rescored) {
            finalRecommendations.add(sr.recipe);
        }
        
        return finalRecommendations;
    }
    
    /**
     * Simple feedback scoring based on similarity to liked recipes
     */
    private int calculateFeedbackScore(Recipe recipe) {
        int score = 0;
        
        // Check if similar to liked recipes
        for (Map.Entry<String, Integer> entry : recipeFeedback.entrySet()) {
            if (entry.getValue() > 0) { // Liked recipe
                // Simple similarity check - same dietary category and health goals
                Recipe likedRecipe = findRecipeById(entry.getKey());
                if (likedRecipe != null) {
                    if (likedRecipe.getDietaryCategory().equals(recipe.getDietaryCategory())) {
                        score += 2;
                    }
                    if (likedRecipe.getHealthGoals().equals(recipe.getHealthGoals())) {
                        score += 2;
                    }
                    if (likedRecipe.getCuisine().equals(recipe.getCuisine())) {
                        score += 1;
                    }
                }
            }
        }
        
        return score;
    }
    
    private Recipe findRecipeById(String recipeId) {
        // This would ideally query the database, but for simplicity we'll check current recipes
        for (Recipe recipe : recommendationService.getAllRecipes()) {
            if (recipeId.equals(recipe.getId())) {
                return recipe;
            }
        }
        return null;
    }

    public void resetSeenRecipes() {
        seenRecipeIds.clear();
    }

    public int getSeenRecipeCount() {
        return seenRecipeIds.size();
    }
    
    private static class ScoredRecipe {
        Recipe recipe;
        int score;
        
        ScoredRecipe(Recipe recipe, int score) {
            this.recipe = recipe;
            this.score = score;
        }
        
        int getScore() { return score; }
    }
}
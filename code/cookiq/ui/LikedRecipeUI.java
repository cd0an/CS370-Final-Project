/* View All Liked Recipes UI */

package cookiq.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import cookiq.models.Recipe;
import cookiq.services.ImageService;
import cookiq.services.UserService;

public class LikedRecipeUI extends JPanel {
    private JPanel likedRecipesPanel; // Container for all liked recipe cards
    private JScrollPane scrollPane; // Scrolls when there are many recipes 
    private MainFrame mainFrame; // Reference to MainFrame for navigation
    private UserService userService; // Calls UserService 
    private ImageService img_service = new ImageService();

    // Constructor
    public LikedRecipeUI(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = mainFrame.getUserService();

        // Main layout setup
        setLayout(new BorderLayout()); 
        setBackground(new Color(0xF2, 0xEf, 0xEB)); // Light Orange

        // ========= Title ========= 
        JLabel header = new JLabel("Favorites", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 28));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // ========= Liked Recipe Panel ========= 
        likedRecipesPanel = new JPanel();
        likedRecipesPanel.setLayout(new BoxLayout(likedRecipesPanel, BoxLayout.Y_AXIS));
        likedRecipesPanel.setBackground(new Color(0xF2, 0xEf, 0xEB));
        likedRecipesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add scrollable container for the cards
        scrollPane = new JScrollPane(likedRecipesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

    }

    //  ========= Load all liked recipes from UserService =========
    public void loadLikedRecipes() {
        if (mainFrame == null || mainFrame.getCurrentUser() == null) return;

        likedRecipesPanel.removeAll();

        // Fetch full Recipe objects, not just names
        List<Recipe> likedRecipes = userService.getLikedRecipesFull(mainFrame.getCurrentUser().getUsername());

        for (Recipe recipe : likedRecipes) {
            addLikedRecipe(recipe);
        }

        likedRecipesPanel.revalidate();
        likedRecipesPanel.repaint();
    }

    // ========= Add a Liked Recipe Card ========= 
    // Dynamically adds a recipe card to the list
    public void addLikedRecipe(Recipe recipe) {
        JPanel card = createRecipeCard(recipe);
        likedRecipesPanel.add(card);
        likedRecipesPanel.add(Box.createVerticalStrut(20)); // Space between cards
        likedRecipesPanel.revalidate();
        likedRecipesPanel.repaint();
    }

    // Create a Single Recipe Card
    private JPanel createRecipeCard(Recipe recipe) {
        JPanel card = new RoundedPanel(25, Color.WHITE);
        card.setBackground(Color.WHITE);
        card.setOpaque(true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(480, 480));
        card.setMaximumSize(new Dimension(480, 480));
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // --- Recipe Image ---
        img_service.displayRecipeImageLiked(card, recipe.getName());

        // --- Recipe Title ---
        JLabel titleLabel = new JLabel(recipe.getName(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));

        // --- Cuisine | Cook Time | Cost ---
        JLabel infoLabel = new JLabel(recipe.getCuisine() + " | " + recipe.getCookTime() + " min | $" + recipe.getCost(), SwingConstants.CENTER);
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(infoLabel);
        card.add(Box.createVerticalStrut(12));

        // --- Recipe Tags ---
        JLabel tagsLabel = new JLabel(recipe.getDietaryCategory() + " • " + recipe.getHealthGoals(), SwingConstants.CENTER);
        tagsLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        tagsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(tagsLabel);
        card.add(Box.createVerticalStrut(20));

        // --- "View Full Recipe" Button ---
        JButton viewRecipeBtn = new JButton("View Full Recipe");
        viewRecipeBtn.setBackground(new Color(0x6E, 0x92, 0x77)); // Soft green
        viewRecipeBtn.setForeground(Color.WHITE);
        viewRecipeBtn.setFocusPainted(false);
        viewRecipeBtn.setContentAreaFilled(true);
        viewRecipeBtn.setOpaque(true);
        viewRecipeBtn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        viewRecipeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover effect for button
        viewRecipeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                viewRecipeBtn.setBackground(new Color(0x5A, 0x7B, 0x63));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                viewRecipeBtn.setBackground(new Color(0x6E, 0x92, 0x77));
            }
        });

        // --- Navigate to RecipeDetailsUI when clicked ---
        viewRecipeBtn.addActionListener(e -> {
            mainFrame.showRecipeDetailsUI(recipe);
        });

        card.add(viewRecipeBtn);
        card.add(Box.createVerticalGlue());

        return card;
    }

    //  ========= Custom Rounded Panel ========= 
    private static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color borderColor;

        public RoundedPanel(int radius, Color borderColor) {
            this.cornerRadius = radius;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }
    }
}



/* Home Dashboard */

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import cookiq.services.UserSession;

public class HomeDashboardUI extends JPanel {
    private JPanel cardPanel;
    private MainFrame mainFrame; // Reference to parent frame

    // Constructor
    public HomeDashboardUI(MainFrame frame) {
        this.mainFrame = frame;

        setLayout(new BorderLayout());
        setBackground(new Color(0xF2, 0xEF, 0xEB)); // Light Orange

        // ======= Title =======
        JLabel title = new JLabel("Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(new Color(0x473c38));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // ======= White Panel =======
        cardPanel = new RoundedPanel(25, Color.WHITE);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setPreferredSize(new Dimension(400, 300));
        cardPanel.setMaximumSize(new Dimension(400, 300));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== Buttons =====
        JButton prefsBtn = new JButton("My Preferences");
        JButton mealMatchBtn = new JButton("Meal Match");
        JButton likedBtn = new JButton("View Liked Recipes");

        // Styling
        JButton[] buttons = { prefsBtn, mealMatchBtn, likedBtn };
        for (JButton btn : buttons) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setBackground(new Color(0x6E, 0x92, 0x77));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            btn.setFont(new Font("SansSerif", Font.BOLD, 16));
            btn.setMaximumSize(new Dimension(300, 50)); // uniform size
            addHoverEffect(btn, new Color(0x5A7B63));
        }

        // Add buttons to card panel
        cardPanel.add(Box.createVerticalGlue());
        for (int i = 0; i < buttons.length; i++) {
            cardPanel.add(buttons[i]);
            if (i < buttons.length - 1) {
                cardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            }
        }
        cardPanel.add(Box.createVerticalGlue());

        // When user clicks the 'My Preferences' button, it navigates to the Preferences UI
        prefsBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPreferencesUI();
            }
        });

        // When user clicks the 'Meal Match' button, it navigates to the Swipe UI
        mealMatchBtn.addActionListener(e -> {
            if (mainFrame != null) {
                // Trigger the navbar's Meal Match button
                mainFrame.getNavbar().getMealMatchBtn().doClick();
            }
        });

        // When user clicks the 'View Liked Recipes' button, it navigates to the Liked Recipes UI
        likedBtn.addActionListener(e -> {
            if (mainFrame == null) return;

            // Always check at the moment of click
            if (UserSession.getInstance().isGuest() || mainFrame.getCurrentUser() == null) {
                // Show dialog for guests
                javax.swing.JOptionPane.showMessageDialog(
                    mainFrame,
                    "Please log in to view your liked recipes.",
                    "Login Required",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                // Logged-in user
                mainFrame.showLikedRecipesUI();
            }
        });

        // ===== Center Panel =====
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(0xF2, 0xEF, 0xEB));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalGlue());

        // Wrap title and card in a container panel to align horizontally
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false); // transparent to show background
        container.add(title);
        container.add(cardPanel);

        centerPanel.add(container);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    // ====================== Helper Functions ======================
    private void addHoverEffect(JButton button, Color hoverColor) {
        Color originalColor = button.getBackground();
        Dimension originalSize = button.getPreferredSize();

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.setPreferredSize(new Dimension(originalSize.width + 10, originalSize.height + 5));
                button.revalidate();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
                button.setPreferredSize(originalSize);
                button.revalidate();
            }
        });
    }

    // Custom Rounded Panel
    private static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color borderColor;

        public RoundedPanel(int radius, Color borderColor) {
            this.cornerRadius = radius;
            this.borderColor = borderColor;
            setBackground(Color.WHITE);
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

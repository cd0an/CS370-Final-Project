package cookiq.services;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cookiq.models.Recipe; 

public class ImageService 
{
    /**
     * API_KEY --> Api key
     * AIzaSyCHa3fu4NsDuWU8WejIpNnFfHQm9_11p2w
     * AIzaSyAf3Pj9kGdiCx5wtnM0Q0xpS6hVZPAGinw
     * 
     * CX --> Search Engine ID
     * 83632e2b5a7c14e72
     * d71afe979ffb143fd     * 
     */
    private static final String API_KEY = "AIzaSyCHa3fu4NsDuWU8WejIpNnFfHQm9_11p2w";
    private static final String CX = "83632e2b5a7c14e72 ";

    List<BufferedImage> image_list = new ArrayList<>();
    int img_index = 0;

    /**
     * Method to get 1 (or more) images based on passed in recipe name
     * Buffered --> Means editable image
     */
    public List<BufferedImage> getImage(String recipe_name) {
        image_list.clear(); // Clear old images 

        try {
            String query = recipe_name;
            String encoded_query = URLEncoder.encode(query, "UTF-8"); //Allows the query to be URL search friendly

            /**
             * Findn and return 1 (or more) image urls for the queried recipe name
             * If you want to change the number returned, change the value at the end of the https
             */
            String url_str = String.format(
                "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&searchType=image&num=1",
                API_KEY, CX, encoded_query
            );

            URL url = new URL(url_str);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); //The Https URL connection

            //Read response
            StringBuilder response = new StringBuilder();
            try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) 
            {
                String line;                
                while((line = in.readLine()) != null) 
                {
                    response.append(line); //if line isn't null we append it to create the final url
                }
            }

            JSONObject json = new JSONObject(response.toString());
            JSONArray items = json.getJSONArray("items"); //Returned json of 5 urls

            for(int i = 0; i < items.length(); i++) 
            {
                JSONObject item = items.getJSONObject(i);
                String image_url = item.getString("link"); //Get the url link from the JSONObject
                BufferedImage img = ImageIO.read(new URL(image_url));
                image_list.add(img);
            }
        }

        /**
         * Error catching
         */
        catch(MalformedURLException e) {
            e.printStackTrace(); //Invalid URL format
        } 
        catch(IOException e) {
            e.printStackTrace(); //Connection/stream errors
        } 
        catch(JSONException e) {
            e.printStackTrace(); //JSON parsing errors
        }
        return image_list;
    }

    public JLabel displayImage(List<BufferedImage> image_list, String RECIPE_NAME, int WIDTH, int HEIGHT) {
        if (image_list == null || image_list.isEmpty()) {
            return null;
        }

        BufferedImage img = image_list.get(img_index++);
        if(img_index >= image_list.size()){
            img_index = 0; //Loop to first image
        } 

        //Scale down but maintain aspect ratio
        int scaleWidth = WIDTH;
        int scaleHeight = HEIGHT;

        double imageAspectRatio = (double) img.getWidth() / img.getHeight();
        double targetAspectRatio = (double) WIDTH / HEIGHT;

        if(imageAspectRatio > targetAspectRatio){
            //Wider image
            scaleHeight = (int)(WIDTH / imageAspectRatio);
        }
        else
        {
            //Taller image
            scaleWidth = (int)(HEIGHT * imageAspectRatio);
        }

        Image scaled_img = img.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled_img); 
        JLabel label = new JLabel(icon);

        //Create JFrame to show image
        JFrame frame = new JFrame(RECIPE_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        //Center image if it doesn't fill the space
        int x = (WIDTH - scaleWidth) / 2;
        int y = (HEIGHT - scaleHeight) / 2;

        label.setBounds(0, 0, scaleWidth, scaleHeight);
        frame.add(label);
        frame.setSize(WIDTH + 16, HEIGHT + 39); //Window boarder
        frame.setResizable(false);
        frame.setVisible(true);

        return label;
    }

    private BufferedImage blurImage(BufferedImage img, int blurRadius) {
        //Downscale image for faster blur
        int downscaleFactor = 3;
        int smallWidth = Math.max(1, img.getWidth() / downscaleFactor);
        int smallHeight = Math.max(1, img.getHeight() / downscaleFactor);
        
        //Create a smaller version of the og image
        BufferedImage small = new BufferedImage(smallWidth, smallHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = small.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(img, 0, 0, smallWidth, smallHeight, null); //Draw the downscaled ver.
        g2d.dispose();
        
        //Apply Gaussian-like blur 3 times
        BufferedImage blurred = small;
        for(int i = 0; i < 3; i++) {
            blurred = applyGaussianBlur(blurred, blurRadius);
        }
        
        //Scale back to original size
        BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(blurred, 0, 0, img.getWidth(), img.getHeight(), null);
        g.dispose(); //Free
        
        return result;
    }

    private BufferedImage applyGaussianBlur(BufferedImage src, int radius) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        //Get 1D array of all pixels from og img
        int[] srcPixels = new int[width * height];
        int[] destPixels = new int[width * height];
        src.getRGB(0, 0, width, height, srcPixels, 0, width);
        
        //Horizontal pass - blurring along the X-axis
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int r = 0, g = 0, b = 0, count = 0;
        
                for(int i = -radius; i <= radius; i++) {
                    int px = Math.min(Math.max(x + i, 0), width - 1);
                    int pixel = srcPixels[y * width + px];
                    
                    r += (pixel >> 16) & 0xFF;
                    g += (pixel >> 8) & 0xFF;
                    b += pixel & 0xFF;
                    count++;
                }
                
                destPixels[y * width + x] = 0xFF000000 | 
                    ((r / count) << 16) | 
                    ((g / count) << 8) | 
                    (b / count);
            }
        }
        
        //Vertical pass - blurring along the Y-axis
        int[] tempPixels = new int[width * height];

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int r = 0, g = 0, b = 0, count = 0;
                
                for(int i = -radius; i <= radius; i++) {
                    int py = Math.min(Math.max(y + i, 0), height - 1);
                    int pixel = destPixels[py * width + x];
                    
                    r += (pixel >> 16) & 0xFF;
                    g += (pixel >> 8) & 0xFF;
                    b += pixel & 0xFF;
                    count++;
                }
                
                tempPixels[y * width + x] = 0xFF000000 | 
                    ((r / count) << 16) | 
                    ((g / count) << 8) | 
                    (b / count);
            }
        }
        
        dest.setRGB(0, 0, width, height, tempPixels, 0, width);
        return dest;
    }

    public ImageIcon getScaledImage(BufferedImage img, int WIDTH, int HEIGHT) {
        if(img == null) {
            return null;
        }
        
        //Create blurred background
        BufferedImage blurred = blurImage(img, 15);
        BufferedImage background = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = background.createGraphics();
        
        //Draw scaled blurred image to fill background
        g2d.drawImage(blurred, 0, 0, WIDTH, HEIGHT, null);
        
        //Calculate scaled dimensions while maintaining aspect ratio
        int scaledWidth = WIDTH;
        int scaledHeight = HEIGHT;
        
        double imageAspectRatio = (double) img.getWidth() / img.getHeight();
        double targetAspectRatio = (double) WIDTH / HEIGHT;
        
        if(imageAspectRatio > targetAspectRatio) {
            //Image is wider
            scaledHeight = (int)(WIDTH / imageAspectRatio);
        } 
        else 
        {
            //Image is taller
            scaledWidth = (int)(HEIGHT * imageAspectRatio);
        }
        
        //Scale original image (unblurred)
        Image scaledImage = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        
        //Center and draw scaled image on top of blurred background
        int x = (WIDTH - scaledWidth) / 2;
        int y = (HEIGHT - scaledHeight) / 2;
        g2d.drawImage(scaledImage, x, y, null);
        g2d.dispose();
        
        return new ImageIcon(background);
    }

    public void displayRecipeImageFR(JPanel leftPanel, String recipeName, int width, int height) {
        ImageService imgService = new ImageService();
        
        //Load images for a recipe
        List<BufferedImage> images = imgService.getImage(recipeName);
        if(images == null || images.isEmpty()) {
            System.out.println("No images found.");
            //Fallback to placeholder
            JLabel imageLabel = new JLabel("Recipe Image", SwingConstants.CENTER);
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.LIGHT_GRAY);
            imageLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageLabel.setPreferredSize(new Dimension(width, height));
            imageLabel.setMaximumSize(new Dimension(width, height));
            leftPanel.add(imageLabel);
            leftPanel.add(Box.createVerticalStrut(20));
            return;
        }
        
        //Get scaled image with blurred background
        ImageIcon scaledIcon = imgService.getScaledImage(images.get(0), width, height);
        
        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setPreferredSize(new Dimension(width, height));
        imageLabel.setMaximumSize(new Dimension(width, height));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        leftPanel.add(imageLabel);
        leftPanel.add(Box.createVerticalStrut(20));
    }

    public void displayRecipeImageLiked(JPanel card, String recipeName) {
        ImageService imgService = new ImageService();
        
        // Load images for a recipe
        List<BufferedImage> images = imgService.getImage(recipeName);
        if (images == null || images.isEmpty()) {
            System.out.println("No images found.");
            // Fallback to placeholder
            JLabel imageLabel = new JLabel("Recipe Image", SwingConstants.CENTER);
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.LIGHT_GRAY);
            imageLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageLabel.setPreferredSize(new Dimension(420, 250));
            imageLabel.setMaximumSize(new Dimension(420, 250));
            card.add(imageLabel);
            card.add(Box.createVerticalStrut(15));
            return;
        }
        
        // Get scaled image with blurred background
        ImageIcon scaledIcon = imgService.getScaledImage(images.get(0), 420, 250);
        
        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setPreferredSize(new Dimension(420, 250));
        imageLabel.setMaximumSize(new Dimension(420, 250));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(imageLabel);
        card.add(Box.createVerticalStrut(15));
    }

    public JLabel displayRecipeImagePreview(Recipe recipe) {
        List<BufferedImage> images = getImage(recipe.getName());
        JLabel imageLabel;

        if (images == null || images.isEmpty()) {
            imageLabel = new JLabel("Recipe Image", SwingConstants.CENTER);
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.LIGHT_GRAY);
            imageLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
            imageLabel.setPreferredSize(new Dimension(420, 250));
            imageLabel.setMaximumSize(new Dimension(420, 250));
        } else {
            BufferedImage img = images.get(0);
            ImageIcon scaledIcon = getScaledImage(img, 420, 250);
            imageLabel = new JLabel(scaledIcon);
        }
        imageLabel.setPreferredSize(new Dimension(420, 250));
        imageLabel.setMaximumSize(new Dimension(420, 250));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        return imageLabel;
    }
}
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Parser {
    static List<Game> games = new ArrayList<>();
    private Connection dbConnection;

    // Database setup methods
    public void setupDatabase() {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Create a connection to the database (creates file if it doesn't exist)
            dbConnection = DriverManager.getConnection("jdbc:sqlite:games.db");

            // Create games table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS games (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "rating REAL NOT NULL," +
                    "price INTEGER NOT NULL," +
                    "category TEXT," +
                    "description TEXT," +
                    "source TEXT NOT NULL)";

            try (Statement stmt = dbConnection.createStatement()) {
                stmt.execute(createTableSQL);
            }

            System.out.println("Database initialized successfully");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database setup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveGamesToDatabase(List<Game> games, String source) {
        if (dbConnection == null) {
            System.err.println("Database not initialized");
            return;
        }

        String insertSQL = "INSERT INTO games (name, rating, price, category, description, source) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(insertSQL)) {
            // Start a transaction for better performance
            dbConnection.setAutoCommit(false);

            for (Game game : games) {
                pstmt.setString(1, game.getName());
                pstmt.setDouble(2, game.getRating());
                pstmt.setInt(3, game.getPrice());
                pstmt.setString(4, game.getCategory());
                pstmt.setString(5, game.getDescription());
                pstmt.setString(6, source);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            dbConnection.commit();

            System.out.println("Saved " + games.size() + " games to database from source: " + source);
        } catch (SQLException e) {
            try {
                dbConnection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error saving to database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                dbConnection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Game> loadGamesFromDatabase(String source) {
        List<Game> loadedGames = new ArrayList<>();

        if (dbConnection == null) {
            System.err.println("Database not initialized");
            return loadedGames;
        }

        String selectSQL = "SELECT name, rating, price, category, description FROM games WHERE source = ?";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(selectSQL)) {
            pstmt.setString(1, source);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    double rating = rs.getDouble("rating");
                    int price = rs.getInt("price");
                    String category = rs.getString("category");
                    String description = rs.getString("description");

                    Game game = new Game(name, rating, price, category, description);
                    loadedGames.add(game);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading from database: " + e.getMessage());
            e.printStackTrace();
        }

        return loadedGames;
    }

    public void closeDatabase() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Original sorting methods
    public List<Game> sortByName() {
        List<Game> sortedByName = new ArrayList<>(games);
        // Sort games alphabetically (least)
        Collections.sort(sortedByName, Comparator.comparing(Game::getName));
        return sortedByName;
    }

    public List<Game> sortByRating() {
        List<Game> sortedByRating = new ArrayList<>(games);
        // Sort games by rating (most)
        Collections.sort(sortedByRating, Comparator.comparing(Game::getRating).reversed());
        return sortedByRating;
    }

    public List<Game> sortByPrice() {
        List<Game> sortedByPrice = new ArrayList<>(games);
        // Sort games by price (most)
        Collections.sort(sortedByPrice, Comparator.comparing(Game::getPrice).reversed());
        return sortedByPrice;
    }

    // Additional sorting method by category
    public List<Game> sortByCategory() {
        List<Game> sortedByCategory = new ArrayList<>(games);
        // Sort games by category alphabetically
        Collections.sort(sortedByCategory, Comparator.comparing(Game::getCategory));
        return sortedByCategory;
    }

    // Filter method by minimum rating
    public List<Game> filterByMinimumRating(double minRating) {
        return games.stream()
                .filter(game -> game.getRating() >= minRating)
                .collect(Collectors.toList());
    }

    // Filter method by price range
    public List<Game> filterByPriceRange(int minPrice, int maxPrice) {
        return games.stream()
                .filter(game -> game.getPrice() >= minPrice && game.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public void setUp() throws IOException {
        // Parse the HTML file using Jsoup
        File input = new File("src/Resources/Video_Games.html");
        Document doc = Jsoup.parse(input, "UTF-8");

        // Extract data from the HTML
        Elements gameElements = doc.select("div.game");

        // Iterate through each Game div to extract Game data
        for (Element gameElement : gameElements) {
            String name = gameElement.select("h3.game-name").text();

            // Extract rating and convert to double
            String ratingText = gameElement.select("span.game-rating").text();
            double rating = Double.parseDouble(ratingText.replace("/5", ""));

            // Extract price and convert to int
            String priceText = gameElement.select("span.game-price").text();
            // Remove currency symbol and parse as int
            int price = Integer.parseInt(priceText.replaceAll("[^0-9]", ""));

            // Try to extract category if exists
            String category = gameElement.select("span.game-category").text();
            if (category.isEmpty()) {
                category = "Unknown"; // Default category
            }

            // Try to extract description if exists
            String description = gameElement.select("p.game-description").text();

            // Create new Game object and add to list
            Game game = new Game(name, rating, price, category, description);
            games.add(game);
        }

        // Save games to database
        if (!games.isEmpty()) {
            setupDatabase();
            saveGamesToDatabase(games, "local_html_file");
        }
    }

    // New method to scrape Oscar Winning Films (bonus feature #1)
    public List<Game> scrapeOscarFilms() throws IOException {
        // This is a hypothetical example - in a real implementation,
        // you'd use a real URL for Oscar winning films
        List<Game> oscarFilms = new ArrayList<>();

        try {
            // For this example, we'll simulate scraping by creating some sample data
            // In a real implementation, you'd do something like:
            // Document doc = Jsoup.connect("https://example.com/oscar-films").get();

            // Instead, create some sample data
            Game film1 = new Game("The Godfather", 4.9, 15, "Drama", "Classic mafia film");
            Game film2 = new Game("Parasite", 4.7, 12, "Thriller", "Korean social commentary");
            Game film3 = new Game("Nomadland", 4.5, 10, "Drama", "Modern American nomads");

            oscarFilms.add(film1);
            oscarFilms.add(film2);
            oscarFilms.add(film3);

            // Save to database
            setupDatabase();
            saveGamesToDatabase(oscarFilms, "oscar_films");

            System.out.println("Oscar films added successfully");
        } catch (Exception e) {
            System.err.println("Error scraping Oscar films: " + e.getMessage());
            e.printStackTrace();
        }

        return oscarFilms;
    }

    // Method to create and show the GUI (bonus feature #2)
    public void createAndShowGUI() {
        JFrame frame = new JFrame("Game & Film Database Explorer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create table model and table for displaying games
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Name");
        tableModel.addColumn("Rating");
        tableModel.addColumn("Price");
        tableModel.addColumn("Category");

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Create control panel for buttons and filters
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        // Sorting buttons
        JPanel sortingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortingPanel.setBorder(BorderFactory.createTitledBorder("Sort By"));

        JButton sortByNameBtn = new JButton("Name");
        JButton sortByRatingBtn = new JButton("Rating");
        JButton sortByPriceBtn = new JButton("Price");
        JButton sortByCategoryBtn = new JButton("Category");

        sortingPanel.add(sortByNameBtn);
        sortingPanel.add(sortByRatingBtn);
        sortingPanel.add(sortByPriceBtn);
        sortingPanel.add(sortByCategoryBtn);

        // Filter controls
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        JLabel minRatingLabel = new JLabel("Min Rating:");
        JSlider ratingSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 0);
        ratingSlider.setMajorTickSpacing(10);
        ratingSlider.setMinorTickSpacing(5);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);

        JLabel priceRangeLabel = new JLabel("Price Range:");
        JSlider minPriceSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        JSlider maxPriceSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);

        filterPanel.add(minRatingLabel);
        filterPanel.add(ratingSlider);
        filterPanel.add(priceRangeLabel);
        filterPanel.add(minPriceSlider);
        filterPanel.add(maxPriceSlider);

        // Data source selection
        JPanel sourcePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourcePanel.setBorder(BorderFactory.createTitledBorder("Data Source"));

        JRadioButton gamesRadio = new JRadioButton("Video Games", true);
        JRadioButton oscarRadio = new JRadioButton("Oscar Films", false);
        JButton refreshBtn = new JButton("Refresh Data");

        ButtonGroup sourceGroup = new ButtonGroup();
        sourceGroup.add(gamesRadio);
        sourceGroup.add(oscarRadio);

        sourcePanel.add(gamesRadio);
        sourcePanel.add(oscarRadio);
        sourcePanel.add(refreshBtn);

        // Add panels to control panel
        controlPanel.add(sortingPanel);
        controlPanel.add(filterPanel);
        controlPanel.add(sourcePanel);

        // Add control panel to main layout
        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // Add status bar
        JLabel statusBar = new JLabel("Ready");
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        // Add action listeners
        sortByNameBtn.addActionListener(e -> {
            updateTable(tableModel, sortByName());
            statusBar.setText("Sorted by name");
        });

        sortByRatingBtn.addActionListener(e -> {
            updateTable(tableModel, sortByRating());
            statusBar.setText("Sorted by rating (highest first)");
        });

        sortByPriceBtn.addActionListener(e -> {
            updateTable(tableModel, sortByPrice());
            statusBar.setText("Sorted by price (highest first)");
        });

        sortByCategoryBtn.addActionListener(e -> {
            updateTable(tableModel, sortByCategory());
            statusBar.setText("Sorted by category");
        });

        ratingSlider.addChangeListener(e -> {
            double minRating = ratingSlider.getValue() / 10.0;
            List<Game> filtered = filterByMinimumRating(minRating);
            updateTable(tableModel, filtered);
            statusBar.setText("Filtered by minimum rating: " + minRating);
        });

        refreshBtn.addActionListener(e -> {
            try {
                if (gamesRadio.isSelected()) {
                    games = loadGamesFromDatabase("local_html_file");
                    if (games.isEmpty()) {
                        setUp();
                    }
                } else {
                    games = loadGamesFromDatabase("oscar_films");
                    if (games.isEmpty()) {
                        games = scrapeOscarFilms();
                    }
                }
                updateTable(tableModel, games);
                statusBar.setText("Data refreshed from " +
                        (gamesRadio.isSelected() ? "video games" : "Oscar films") +
                        " source");
            } catch (IOException ex) {
                statusBar.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Set up initial table data
        updateTable(tableModel, games);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    private void updateTable(DefaultTableModel model, List<Game> gamesToShow) {
        model.setRowCount(0); // Clear table

        for (Game game : gamesToShow) {
            model.addRow(new Object[]{
                    game.getName(),
                    game.getRating(),
                    "$" + game.getPrice(),
                    game.getCategory()
            });
        }
    }

    public static void main(String[] args) {
        // You can test your code here before you run the unit tests
        Parser parser = new Parser();
        try {
            parser.setUp();

            System.out.println("Games sorted by name:");
            List<Game> nameSort = parser.sortByName();
            for (Game game : nameSort) {
                System.out.println(game);
            }

            System.out.println("\nGames sorted by rating (highest first):");
            List<Game> ratingSort = parser.sortByRating();
            for (Game game : ratingSort) {
                System.out.println(game);
            }

            System.out.println("\nGames sorted by price (highest first):");
            List<Game> priceSort = parser.sortByPrice();
            for (Game game : priceSort) {
                System.out.println(game);
            }

            // Test bonus features
            System.out.println("\nBonus features:");

            // 1. Oscar films scraping
            System.out.println("\nOscar Winning Films:");
            List<Game> oscarFilms = parser.scrapeOscarFilms();
            for (Game film : oscarFilms) {
                System.out.println(film);
            }

            // 2. Show GUI for interactive use
            parser.createAndShowGUI();

            // 3. Database is already implemented and used throughout the code

        } catch (IOException e) {
            System.err.println("Error processing HTML file: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close database connection when done
            parser.closeDatabase();
        }
    }
}
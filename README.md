# Video Game Scraper Project

## Overview
This project is a Java application that demonstrates HTML parsing and data manipulation techniques. It extracts video game information from an HTML document, stores it in a structured format, and provides various ways to sort, filter, and analyze the data. The application includes a graphical user interface for interactive exploration and database integration for persistent storage.

## Features

### Core Features
- HTML parsing using JSoup to extract game data (name, rating, price)
- Object-oriented representation of video games
- Multiple sorting algorithms (by name, rating, price)
- Clean console output for sorted data

### Bonus Features
1. **Extended Data Extraction**
   - Support for scraping data from additional sources (Oscar-winning films)
   - Flexible data model to accommodate various types of media

2. **Interactive User Interface**
   - Swing-based GUI for data exploration
   - Dynamic sorting and filtering capabilities
   - Real-time feedback and status updates
   - Toggle between different data sources

3. **Database Integration**
   - SQLite integration for persistent data storage
   - Save and load functionality
   - Data source tracking for organization
   - Transaction support for reliable data operations

## Project Structure

### Game Class
The `Game` class represents a video game or film with the following properties:
- `name`: The title of the game/film
- `rating`: User rating (out of 5)
- `price`: Price in dollars
- `category`: Genre or category (optional)
- `description`: Brief description (optional)

This class includes:
- Constructors for creating objects with different sets of attributes
- Getters and setters for all properties
- `toString()` method for readable string representation
- `equals()` and `hashCode()` methods for proper object comparison

### Parser Class
The `Parser` class handles the core functionality:
- HTML parsing and data extraction
- Database operations
- Sorting and filtering algorithms
- GUI implementation

#### Key Methods:
- `setUp()`: Parses HTML and extracts game data
- `sortByName()`, `sortByRating()`, `sortByPrice()`: Different sorting algorithms
- `filterByMinimumRating()`, `filterByPriceRange()`: Data filtering options
- `scrapeOscarFilms()`: Extracts data from alternative sources
- `createAndShowGUI()`: Builds and displays the interactive interface
- Database methods: `setupDatabase()`, `saveGamesToDatabase()`, `loadGamesFromDatabase()`

## Dependencies
- **JSoup (1.14.3)**: HTML parsing library
- **SQLite JDBC (3.36.0.3)**: Database connectivity
- **JUnit (5.8.1)**: For unit testing
- **Java Swing**: For GUI components (part of Java standard library)

## How to Run

### Prerequisites
- Java JDK 17 or higher
- Gradle 7.6 or newer
- Git (for version control)

### Build Instructions
1. Clone the repository:
   ```
   git clone <repository-url>
   ```

2. Navigate to the project directory:
   ```
   cd Fourth-Assignment-Steam-Scraper
   ```

3. Build the project using Gradle:
   ```
   gradle build
   ```

### Run Instructions
1. Execute the main class:
   ```
   gradle run
   ```
   Or run it directly from your IDE by executing the `main` method in the `Parser` class.

2. For the GUI interface, the application will automatically open a window after processing the initial HTML file.

## Code Explanation

### HTML Parsing Logic
The application uses JSoup to parse HTML from `Video_Games.html` located in the `src/Resources` directory. It looks for:
- `div.game` elements for each game entry
- `h3.game-name` for game titles
- `span.game-rating` for rating information
- `span.game-price` for price data
- Additional tags for optional category and description information

The parsing logic handles converting text values to appropriate data types (e.g., removing "/5" from ratings and currency symbols from prices).

### Sorting Algorithms
The application implements several sorting mechanisms using Java's `Comparator` interface:
- Alphabetical sorting for names and categories
- Descending sort for ratings (highest first)
- Descending sort for prices (highest first)

Each sorting method creates a new copy of the games list to avoid modifying the original data.

### Database Implementation
The SQLite database integration provides persistent storage with:
- Automatic table creation
- Parameterized SQL statements to prevent injection
- Transaction support for batch operations
- Proper resource management with try-with-resources
- Source tracking to distinguish different data origins

### GUI Architecture
The Swing-based GUI uses:
- `JTable` for displaying game data
- `JButton` components for sorting triggers
- `JSlider` elements for range-based filtering
- `JRadioButton` for data source selection
- Status bar for user feedback
- Nested panels with appropriate layouts for organized UI

## Design Decisions

### Why Use SQLite?
SQLite was chosen for its:
- Lightweight nature (no separate server required)
- Easy integration with Java applications
- Support for ACID transactions
- Single-file database for simplicity

### Component-Based Structure
The code follows object-oriented principles with clear separation of concerns:
- `Game` class focuses solely on data representation
- `Parser` class handles data processing and interaction
- Database operations are encapsulated in dedicated methods
- GUI components are logically organized

### Error Handling
The application implements robust error handling:
- Appropriate exception catching and logging
- Resource cleanup in finally blocks
- Fallback options when data sources are unavailable
- User-friendly error messages in the GUI

## Future Enhancements
- Web scraping capabilities for live data from online sources
- Advanced filtering options with multiple criteria
- Data visualization with charts and graphs
- Export functionality to various formats (CSV, JSON, etc.)
- Support for more media types beyond games and films

## Conclusion
This project demonstrates practical application of web scraping techniques, data manipulation, and user interface design in Java. The implementation of bonus features showcases additional capabilities in database integration and multi-source data extraction.

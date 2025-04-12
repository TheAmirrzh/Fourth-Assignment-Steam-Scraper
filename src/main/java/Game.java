import java.util.Objects;

public class Game {
    private String name;
    private double rating;
    private int price;
    private String category; // Added for categorization
    private String description; // Added for more details

    public Game(String name, double rating, int price) {
        this.name = name;
        this.rating = rating;
        this.price = price;
        this.category = "";
        this.description = "";
    }

    public Game(String name, double rating, int price, String category, String description) {
        this.name = name;
        this.rating = rating;
        this.price = price;
        this.category = category;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name + " - Rating: " + rating + "/5 - Price: $" + price +
                (category.isEmpty() ? "" : " - Category: " + category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Double.compare(game.rating, rating) == 0 &&
                price == game.price &&
                Objects.equals(name, game.name) &&
                Objects.equals(category, game.category) &&
                Objects.equals(description, game.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rating, price, category, description);
    }
}
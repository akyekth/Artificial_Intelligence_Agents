package se.kth.id2209.hw1;

import java.io.Serializable;
import java.util.Random;

public class Artifact implements Serializable {

    public static final String[] NAMES = new String[]{"Destiny", "Atomic", "Shadow", "Holy", "Haunted", "Quantum", "Universal", "Monstrous"};
    public static final String[] CREATORS = new String[]{"Gustav Klimt", "Donatello", "Henri Rousseau", "Oscar Claude Monet", "Paul Cézanne"};
    public static final String[] PLACES = new String[]{"Macedonia", "Finland", "Slovakia", "Croatia", "Germany", "France", "Ukraine", "Sweden"};
    public static final String[] GENRES = new String[]{"Reading", "TV series", "Family Time", "Fishing", "Computers", "Gardening", "Exercise"};

    private int id;
    private String name;
    private String creator;
    private int yearOfCreation;
    private String placeOfCreation;
    private String genre;

    /**
     * Create a random artifact
     */
    public Artifact(int id) {
        this.id = id;
        name = random(NAMES);
        creator = random(CREATORS);
        yearOfCreation = new Random().nextInt(2015);
        placeOfCreation = random(PLACES);
        genre = random(GENRES);
    }

    private String random(String[] array) {
        return array[new Random().nextInt(array.length)];
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creator='" + creator + '\'' +
                ", yearOfCreation=" + yearOfCreation +
                ", placeOfCreation='" + placeOfCreation + '\'' +
                ", genre='" + genre + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public int getYearOfCreation() {
        return yearOfCreation;
    }

    public String getPlaceOfCreation() {
        return placeOfCreation;
    }

    public String getGenre() {
        return genre;
    }
}

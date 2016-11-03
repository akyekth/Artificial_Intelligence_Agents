package se.kth.id2209.hw1;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;

public class User implements Serializable {

    public static final String[] OCCUPATIONS = new String[]{"Barrister", "Stockbroker", "Traffic warden", "Dietitian", "Computer engineer"};
    public static final String[] GENDERS = new String[]{"Male", "Female"};
    public static final String[] INTERESTS = new String[]{"Reading", "TV series", "Family Time", "Fishing", "Computers", "Gardening", "Exercise"};

    private int age;
    private String occupation;
    private String gender;
    private HashSet<String> interests;
    private HashSet<Integer> visited;

    /**
     * Create a random user
     */
    public User() {
        age = new Random().nextInt(100) + 20;
        occupation = random(OCCUPATIONS);
        gender = random(GENDERS);
        interests = new HashSet();
        for (int i = 0; i < 3; i++) interests.add(random(INTERESTS));
        visited = new HashSet();
    }

    private String random(String[] array) {
        return array[new Random().nextInt(array.length)];
    }

    @Override
    public String toString() {
        return "User{" +
                "age=" + age +
                ", occupation='" + occupation + '\'' +
                ", gender='" + gender + '\'' +
                ", interests=" + interests +
                ", visited=" + visited +
                '}';
    }

    public int getAge() {
        return age;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getGender() {
        return gender;
    }

    public HashSet<String> getInterests() {
        return interests;
    }

    public HashSet<Integer> getVisited() {
        return visited;
    }
}

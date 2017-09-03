package io.github.iamutkarshtiwari.trivia.models;

import java.util.ArrayList;

/**
 * Created by iamutkarshtiwari on 03/09/17.
 */

public class User {
    //name and address string
    private String name;
    private String email;
    private ArrayList<String> difficulty;
    private ArrayList<String> categories;

    public User() {
        /*
        Blank default constructor essential for Firebase
        */
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String address) {
        this.email = address;
    }

    public void setDifficulty(ArrayList<String> array) {
        this.difficulty = new ArrayList<>(array);
    }

    public ArrayList<String> getDifficulty() {
        return difficulty;
    }

    public void setCategories(ArrayList<String> array) {
        this.categories = new ArrayList<>(array);
    }

    public ArrayList<String> getCategories() {
        return categories;
    }
}

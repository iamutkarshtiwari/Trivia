package io.github.iamutkarshtiwari.trivia.models;

import java.util.ArrayList;

/**
 * Created by iamutkarshtiwari on 03/09/17.
 */

public class User {
    //name and address string
    public String name;
    public String email;
    public float hotScore;
    private String difficulty;
    private String categories;
    private String types;
    private String music;


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

    public void setDifficulty(String diff) {
        this.difficulty = diff;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setCategories(String cat) {
        this.categories = cat;
    }

    public String getCategories() {
        return categories;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getTypes() {
        return types;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getMusic() {
        return this.music;
    }
}

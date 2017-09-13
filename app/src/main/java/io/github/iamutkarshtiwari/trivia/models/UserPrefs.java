package io.github.iamutkarshtiwari.trivia.models;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by iamutkarshtiwari on 09/09/17.
 */

public class UserPrefs {

    public String name;
    public String email;
    public ArrayList<String> difficulty;
    public ArrayList<Integer> categories;
    public ArrayList<String> types;
    int categoryValues[] = {27, 25, 16, 10, 32, 26, 29, 18, 11, 30, 9, 22, 23, 31, 19, 12, 13, 20, 24, 17, 21, 14, 28, 15};
    String difficultyValues[] = {"easy", "medium", "hard"};
    String typeValues[] = {"multiple", "boolean"};

    public UserPrefs() {
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

    public ArrayList<String> getDifficulty() {
        return this.difficulty;
    }

    public void setDifficulty(String diff) {
        ArrayList<String> result = new ArrayList<>();
        int index = 0;
        for (String value: Arrays.asList(diff.split(","))) {
            if (value.equalsIgnoreCase("true")) {
                result.add(difficultyValues[index]);
            }
            index++;
        }
        this.difficulty = result;
    }

    public ArrayList<Integer> getCategories() {
        return categories;
    }

    public void setCategories(String cat) {
        ArrayList<Integer> result = new ArrayList<>();
        int index = 0;
        for (String bool : Arrays.asList(cat.split(","))) {
            if (bool.equalsIgnoreCase("true")) {
                result.add(categoryValues[index]);
            }
            index++;
        }
        this.categories = result;
    }

    public ArrayList<String> getTypes() {
        ArrayList<String> result = new ArrayList<>();
        result.addAll(Arrays.asList(typeValues));
        this.types = result;
        return types;
    }

    public void setTypes() {

    }
}
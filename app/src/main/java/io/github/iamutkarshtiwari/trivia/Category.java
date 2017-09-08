package io.github.iamutkarshtiwari.trivia;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.iamutkarshtiwari.trivia.models.CustomList;
import io.github.iamutkarshtiwari.trivia.models.CustomList.ViewHolder;

public class Category extends AppCompatActivity implements View.OnClickListener {

    private static final String MY_PREFS_NAME = "Trivia";
    private static Button saveButton;
    private static ListView categoryListView;
    private static boolean areAllCategoriesSelected = false;
    private static CustomList categoryAdapter;
    private FirebaseAuth mAuth;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private NavigationView navigationView;
    private DatabaseReference mDatabase;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.category_toolbar);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the back button
        ab.setDisplayHomeAsUpEnabled(true);

        // Firebase instance
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        // Preference manager
        pref = this.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = pref.edit();

        boolean loadedSelections[] = loadCategoryPreferences();

        // Categories
        String[] categoryNames = getResources().getStringArray(R.array.category_names);
        categoryAdapter = new
                CustomList(Category.this, categoryNames, loadedSelections);
        categoryListView = (ListView) findViewById(R.id.category_list);
        categoryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        categoryListView.setAdapter(categoryAdapter);

        // Save button
        saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(this);

        // Attach listener to selectAll button
        Button selectAll = (Button) findViewById(R.id.select_all);
        selectAll.setOnClickListener(this);

    }

    public void onClick(View view) {
        int viewID = view.getId();

        if (viewID == R.id.save) {
            saveCategoryPreferences();
            onBackPressed();
        } else if (viewID == R.id.select_all) {
            toggleSelectAll();
            if (!areAllCategoriesSelected) {
                ((Button) view).setText(R.string.deselect_all);
            } else {
                ((Button) view).setText(R.string.select_all);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Toggles all selections checkboxes
     */
    public void toggleSelectAll() {

        for (int i = 0; i < categoryAdapter.getCount(); i++) {
            categoryAdapter.checkSelectionList[i] = !areAllCategoriesSelected;

        }
        areAllCategoriesSelected = !areAllCategoriesSelected;
        // Notifies listview to refresh changes
        categoryAdapter.notifyDataSetChanged();
    }

    public boolean[] loadCategoryPreferences() {
        ArrayList<String> loadedSelection = new ArrayList<String>();
        String savedSelections = pref.getString("user_categories", "");
        if (savedSelections.length() > 0) {
            loadedSelection.addAll(Arrays.asList(savedSelections.split(",")));
            boolean result[] = new boolean[loadedSelection.size()];
            int i = 0;
            for (String bool : loadedSelection) {
                result[i] = bool.equalsIgnoreCase("true");
                i++;
            }
            return result;
        }
        // All checkboxes remain unselected if no saved prefs found
        return new boolean[24];

    }

    public void saveCategoryPreferences() {
        String currentSelections = "";
        for (int i = 0; i < categoryAdapter.getCount(); i++) {
            ViewHolder viewHolder = (ViewHolder) categoryAdapter.getView(i, null, null).getTag();
            CheckBox checkBox = viewHolder.checkBox;
            if (checkBox.isChecked()) {
                currentSelections += "true,";
            } else {
                currentSelections += "false,";
            }
        }

        editor.putString("user_categories", currentSelections);
        editor.commit();
    }
}


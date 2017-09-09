package io.github.iamutkarshtiwari.trivia;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.iamutkarshtiwari.trivia.models.CustomList;
import io.github.iamutkarshtiwari.trivia.models.CustomList.ViewHolder;
import io.github.iamutkarshtiwari.trivia.models.User;

public class Category extends AppCompatActivity implements View.OnClickListener {

    private static final String MY_PREFS_NAME = "Trivia";
    private static Button saveButton;
    private static ListView categoryListView;
    private boolean areAllCategoriesSelected;
    private int selectedCount;
    private static CustomList categoryAdapter;
    public static ProgressDialog progressDialog;
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

        areAllCategoriesSelected = false;
        selectedCount = 0;

        // Firebase instance
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        // Preference manager
        pref = this.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = pref.edit();

        boolean loadedSelections[] = loadCategoryPreferences();
        areAllCategoriesSelected = (selectedCount == loadedSelections.length);
        updateSelectionButtonLabel();

        // Categories
        String[] categoryNames = getResources().getStringArray(R.array.category_names);
        Arrays.sort(categoryNames);
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
            areAllCategoriesSelected = !areAllCategoriesSelected;
            updateSelectionButtonLabel();
        }

    }

    /**
     * Create a progress dialog
     * @param message Message to be displayed
     */
    public void createProgressDialog(int message) {
        progressDialog = new ProgressDialog(Category.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(String.format(getString(message)));
        progressDialog.show();
    }

    public void updateSelectionButtonLabel() {
        Button selectAll = (Button) findViewById(R.id.select_all);
        if (areAllCategoriesSelected) {
            selectAll.setText(R.string.deselect_all);
        } else {
            selectAll.setText(R.string.select_all);
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
        // Notifies listview to refresh changes
        categoryAdapter.notifyDataSetChanged();
    }

    public boolean[] loadCategoryPreferences() {
        ArrayList<String> loadedSelection = new ArrayList<String>();
        String savedSelections = pref.getString("user_categories", "");

        if (savedSelections.length() > 0) {
            loadedSelection.addAll(Arrays.asList(savedSelections.split(",")));
            return stringBooleanToArrayBoolean(loadedSelection);
        } else {
            fetchCategoryPrefsFromFirebase(loadedSelection);
        }
        // All checkboxes remain unselected if no saved prefs found
        return new boolean[24];
    }

    public void fetchCategoryPrefsFromFirebase(final ArrayList<String> loadedSelection) {
        // Fetch user category prefs from Firebase
        createProgressDialog(R.string.loading_prefs);
        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user value
                User user = dataSnapshot.getValue(User.class);
                Log.e("Firebase Fetch", user.getCategories());
                loadedSelection.addAll(Arrays.asList(user.getCategories().split(",")));
                stringBooleanToArrayBoolean(loadedSelection);
                categoryAdapter.setCheckedSelections(stringBooleanToArrayBoolean(loadedSelection));
                categoryAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase Database Error", "");
                progressDialog.dismiss();
            }
        });
    }

    /**
     * Converts string boolean list to boolean array
     * @param list that cantains boolean in string format
     * @return boolean array
     */
    public boolean[] stringBooleanToArrayBoolean(ArrayList<String> list) {
        boolean result[] = new boolean[list.size()];
        int i = 0;
        for (String bool : list) {
            result[i] = bool.equalsIgnoreCase("true");
            if (result[i]) {
                selectedCount++;
            }
            i++;
        }
        return result;
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

        syncCategoryPrefsInFirebase();
    }

    public void syncCategoryPrefsInFirebase() {
        try {
            mDatabase.child("users").child(user.getUid()).child("categories").setValue(pref.getString("user_categories", ""));
        } catch (Exception e) {
            Log.e("Error", String.format(getString(R.string.firebase_sync_error)));
        }
    }
}


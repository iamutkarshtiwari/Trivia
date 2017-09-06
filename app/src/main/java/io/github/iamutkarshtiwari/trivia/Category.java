package io.github.iamutkarshtiwari.trivia;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class Category extends AppCompatActivity implements View.OnClickListener{

    private static Button saveButton;
    private static ListView categoryListView;
    private static boolean areAllCategoriesSelected = false;
    private static final String[] IMAGE_LIST = {"general_knowledge", "books", "films", "music_player", "theater",
            "television", "video_games", "board_games", "science", "computer", "mathematics", "mythology", "sports",
            "geography", "history", "politics", "arts", "celebrity", "animals", "vehicles", "comics", "gadgets", "anime",
            "cartoons"
    };
    ArrayAdapter<String> categoryAdapter;

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

        // Categories
        String[] sports = getResources().getStringArray(R.array.category_names);
        categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, sports);
        categoryListView = (ListView) findViewById(R.id.category_list);
        categoryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        categoryListView.setAdapter(categoryAdapter);

        // Save button
        saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(this);

        // Attach listener to selectAll button
        Button selectAll = (Button) findViewById(R.id.select_all);
        selectAll.setOnClickListener(this);
//        {
//
//            @Override
//            public void onClick(View view) {
//
//                toggleSelectAll();
//                if (!areAllCategoriesSelected) {
//                    selectAll.setText(R.string.deselect_all);
//                } else {
//                    selectAll.setText(R.string.select_all);
//                }
//                areAllCategoriesSelected = !areAllCategoriesSelected;
//            }
//        });
    }

    public void onClick(View view) {

        Toast.makeText(getApplicationContext(), "Hello", Toast.LENGTH_SHORT).show();
//        SparseBooleanArray checked = categoryListView.getCheckedItemPositions();
//        ArrayList<String> selectedItems = new ArrayList<String>();
//        for (int i = 0; i < checked.size(); i++) {
//            // Item position in adapter
//            int position = checked.keyAt(i);
//            // Add sport if it is checked i.e.) == TRUE!
//            if (checked.valueAt(i))
//                selectedItems.add(adapter.getItem(position));
//        }
//
//        String[] outputStrArr = new String[selectedItems.size()];
//
//        for (int i = 0; i < selectedItems.size(); i++) {
//            outputStrArr[i] = selectedItems.get(i);
//        }
//
//        Intent intent = new Intent(getApplicationContext(),
//                ResultActivity.class);
//
//        // Create a bundle object
//        Bundle b = new Bundle();
//        b.putStringArray("selectedItems", outputStrArr);
//
//        // Add the bundle to the intent.
//        intent.putExtras(b);
//
//        // start the ResultActivity
//        startActivity(intent);

//        if ()
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

    public void toggleSelectAll() {
        for (int i = 0; i < categoryAdapter.getCount(); i++ ) {
            categoryListView.setItemChecked(i, !areAllCategoriesSelected);
        }
    }

    public void saveCategoryPreferences() {

    }

}


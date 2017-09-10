package io.github.iamutkarshtiwari.trivia;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Preferences extends AppCompatActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            Log.e("ERROR: ", "Could not set up back button");
        }

        setupLabels();
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
     * Setups the lables for all the views
     */
    public void setupLabels() {
        int labels[][] = {{R.id.easy, R.string.easy}, {R.id.medium, R.string.medium}, {R.id.hard, R.string.hard},
                {R.id.bool, R.string.true_false}, {R.id.multiple, R.string.multiple}, {R.id.music, R.string.music}};

        for (int i = 0; i < labels.length; i++) {
            View easyRow = findViewById(labels[i][0]);
            easyRow.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               ToggleButton togglebtn = (ToggleButton) v.findViewById(R.id.toggleButton);
                                               togglebtn.setChecked(!togglebtn.isChecked());
                                           }
                                       });

            TextView txt = (TextView) easyRow.findViewById(R.id.label);
            txt.setText(labels[i][1]);
        }

    }
    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    public void onClick(View view) {

    }

    public void savePrefs() {
    }

}

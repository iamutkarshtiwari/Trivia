package io.github.iamutkarshtiwari.trivia;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.iamutkarshtiwari.trivia.models.User;

public class Preferences extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "Trivia";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public static ProgressDialog progressDialog;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private final int LABELS[][] = {{R.id.easy, R.string.easy}, {R.id.medium, R.string.medium}, {R.id.hard, R.string.hard},
            {R.id.bool, R.string.true_false}, {R.id.multiple, R.string.multiple}, {R.id.music, R.string.music}};

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

        // Firebase instance
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        // Preference manager
        pref = this.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = pref.edit();

        setupLabelsAndPrefs();
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
    public void setupLabelsAndPrefs() {
        String loadedSelection = "";
        ArrayList<String> selections = new ArrayList<>();
        loadedSelection += pref.getString("user_difficulty", "");
        loadedSelection += pref.getString("user_question_types", "");
        loadedSelection += pref.getString("user_music", "");
        selections.addAll(Arrays.asList(loadedSelection.split(",")));

        if (pref.getString("user_difficulty", "").length() == 0) {
            fetchPrefsFromFirebase();
        } else {
            int i = 0;
            for (String value: selections) {
                View easyRow = findViewById(LABELS[i][0]);
                ToggleButton toggleButton = (ToggleButton) easyRow.findViewById(R.id.toggleButton);
                toggleButton.setChecked(value.equalsIgnoreCase("true"));
                i++;
            }
        }

        // Set up the labels
        for (int i = 0; i < LABELS.length; i++) {
            View easyRow = findViewById(LABELS[i][0]);
            easyRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToggleButton togglebtn = (ToggleButton) v.findViewById(R.id.toggleButton);
                    togglebtn.setChecked(!togglebtn.isChecked());
                }
            });

            TextView txt = (TextView) easyRow.findViewById(R.id.label);
            txt.setText(LABELS[i][1]);
        }
    }

    @Override
    public void onBackPressed() {
        savePreferences();
        syncCategoryPrefsInFirebase();
        super.onBackPressed();
    }

    public void onClick(View view) {

    }

    public void savePrefs() {
    }

    public void fetchPrefsFromFirebase() {
        // Fetch user category prefs from Firebase
        createProgressDialog(R.string.loading_prefs);

        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user value
                User user = dataSnapshot.getValue(User.class);
                if (user.getTypes() != null && user.getDifficulty() != null && user.getMusic() != null) {
                    String data = "";
                    data += user.getTypes();
                    data += user.getDifficulty();
                    data += user.getMusic();

                    editor.putString("user_types", user.getTypes());
                    editor.putString("user_question_types", user.getTypes());
                    editor.putString("user_music", user.getMusic());
                    editor.commit();

                    ArrayList<String> selections = new ArrayList<>();
                    selections.addAll(Arrays.asList(data.split(",")));

                    int i = 0;
                    for (String value: selections) {
                        View easyRow = findViewById(LABELS[i][0]);
                        ToggleButton toggleButton = (ToggleButton) easyRow.findViewById(R.id.toggleButton);
                        toggleButton.setChecked(value.equalsIgnoreCase("true"));
                        i++;
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase Database Error", "");
                progressDialog.dismiss();
            }
        });
        if (!isNetworkAvailable()) {
            progressDialog.dismiss();
        }
    }

    /**
     * Check if internet available to download preferences
     * @return boolean state
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Create a progress dialog
     * @param message Message to be displayed
     */
    public void createProgressDialog(int message) {
        progressDialog = new ProgressDialog(Preferences.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(String.format(getString(message)));
        progressDialog.show();
    }


    public void savePreferences() {
        String result = "";
        for (int i = 0; i < LABELS.length; i++) {
            View easyRow = findViewById(LABELS[i][0]);
            ToggleButton toggleButton = (ToggleButton) easyRow.findViewById(R.id.toggleButton);
            if (toggleButton.isChecked()) {
                result += "true,";
            } else {
                result += "false,";
            }
            if (i == 2) {
                editor.putString("user_difficulty", result);
                result = "";
            } else if (i == 4) {
                editor.putString("user_question_types", result);
                result = "";
            } else if (i == 5){
                editor.putString("user_music", result);
                result = "";

            }
        }
        editor.commit();
    }

    public void syncCategoryPrefsInFirebase() {
        try {
            DatabaseReference ref = mDatabase.child("users").child(user.getUid());
            ref.child("difficulty").setValue(pref.getString("user_difficulty", ""));
            ref.child("types").setValue(pref.getString("user_question_types", ""));
            ref.child("music").setValue(pref.getString("user_music", ""));
        } catch (Exception e) {
            Log.e("Error", String.format(getString(R.string.firebase_sync_error)));
        }
    }

}

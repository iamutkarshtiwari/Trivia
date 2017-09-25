package io.github.iamutkarshtiwari.trivia;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.iamutkarshtiwari.trivia.models.ProfileRecyclerViewAdapter;
import io.github.iamutkarshtiwari.trivia.models.User;

public class Profile extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "Trivia";

    private RecyclerView recyclerView;
    private ArrayList<String> number;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private ProfileRecyclerViewAdapter recyclerViewHorizontalAdapter;
    private LinearLayoutManager horizontalLayout ;
    private View ChildView ;
    private int recyclerViewItemPosition ;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            Log.e("ERROR: ", "Could not set up back button");
        }

        recyclerView = (RecyclerView) findViewById(R.id.category_stats);
        recyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        // Shared preferences
        pref = this.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = pref.edit();

        // Adding items to RecyclerView.
        AddItemsToRecyclerViewArrayList();

        recyclerViewHorizontalAdapter = new ProfileRecyclerViewAdapter(number);
        horizontalLayout = new LinearLayoutManager(Profile.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayout);
        recyclerView.setAdapter(recyclerViewHorizontalAdapter);

        // Adding on item click listener to RecyclerView.
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(Profile.this, new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
                    return true;
                }
            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {
                ChildView = Recyclerview.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if(ChildView != null && gestureDetector.onTouchEvent(motionEvent)) {
                    //Getting clicked value.
                    recyclerViewItemPosition = Recyclerview.getChildAdapterPosition(ChildView);
                    // Showing clicked item value on screen using toast message.
                    Toast.makeText(Profile.this, number.get(recyclerViewItemPosition), Toast.LENGTH_LONG).show();

                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        // Read user details from shared preferences
        try {
            // Display user name and email in navigation header view
            final TextView userName = (TextView) findViewById(R.id.playerName);
            userName.setText(pref.getString("user_name", ""));

            CircleImageView profileImageView = (CircleImageView) findViewById(R.id.imageView);
            Bitmap image = decodeToBase64(pref.getString("user_image", ""));
            profileImageView.setImageBitmap(image);
        } catch (Exception e) {
            Log.e("Image ERROR: ", Log.getStackTraceString(e));
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
     * Gets bitmap image from base64 string
     *
     * @param input base64 string
     * @return Bitmap image
     */
    public Bitmap decodeToBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    @Override
    public void onBackPressed() {
        // TODO Add what you wanna add
        super.onBackPressed();
    }

    // function to add items in RecyclerView.
    public void AddItemsToRecyclerViewArrayList(){

        number = new ArrayList<>();
        number.add("ONE");
        number.add("TWO");
        number.add("THREE");
        number.add("FOUR");
        number.add("FIVE");
//        Number.add("SIX");
//        Number.add("SEVEN");
//        Number.add("EIGHT");
//        Number.add("NINE");
//        Number.add("TEN");

    }
}

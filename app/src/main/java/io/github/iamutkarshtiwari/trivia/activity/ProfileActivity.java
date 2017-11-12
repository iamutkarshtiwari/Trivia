package io.github.iamutkarshtiwari.trivia.activity;

import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.iamutkarshtiwari.trivia.R;
import io.github.iamutkarshtiwari.trivia.models.PrevQuestionsAdapter;
import io.github.iamutkarshtiwari.trivia.models.Question;
import io.github.iamutkarshtiwari.trivia.models.QuestionContract;
import io.github.iamutkarshtiwari.trivia.models.ScoreStatsAdapter;

public class ProfileActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TRIVIA_SETTINGS = "TriviaSettings";
    private static final int QUESTION_LOADER = 0;

    private RecyclerView statsRecyclerView;
    private RecyclerView prevQuestionsRecyclerView;
    private RecyclerView.LayoutManager statsViewLayoutManager;
    private RecyclerView.LayoutManager prevQuestionsViewLayoutManager;
    private ScoreStatsAdapter scoreStatsAdapter;
    private PrevQuestionsAdapter prevQuestionsAdapter;


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

        // Remove action bar elevation
        getSupportActionBar().setElevation(0);


        // Shared preferences
        pref = this.getSharedPreferences(TRIVIA_SETTINGS, MODE_PRIVATE);
        editor = pref.edit();

        // Stats section
        statsRecyclerView = (RecyclerView) findViewById(R.id.category_stats);
        scoreStatsAdapter = new ScoreStatsAdapter(getCategoryStats());
        statsViewLayoutManager = new LinearLayoutManager(ProfileActivity.this, LinearLayoutManager.HORIZONTAL, false);
        statsRecyclerView.setLayoutManager(statsViewLayoutManager);
        statsRecyclerView.setAdapter(scoreStatsAdapter);

        // Prev Questions view
        View emptyView = findViewById(R.id.empty_view);
        ArrayList<Question> prevQuestions = populatePreviousQuestions();
        SnapHelper snapper = new LinearSnapHelper();
        prevQuestionsRecyclerView = (RecyclerView) findViewById(R.id.previous_questions);
        prevQuestionsAdapter = new PrevQuestionsAdapter(prevQuestions);
        prevQuestionsViewLayoutManager = new LinearLayoutManager(ProfileActivity.this, LinearLayoutManager.HORIZONTAL, false);
        prevQuestionsRecyclerView.setLayoutManager(prevQuestionsViewLayoutManager);
        prevQuestionsRecyclerView.setAdapter(prevQuestionsAdapter);
        snapper.attachToRecyclerView(prevQuestionsRecyclerView);
        Log.d("QUESTION LIST: ", prevQuestions.size() + "");

        if (prevQuestions.size() == 0) {
            prevQuestionsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            prevQuestionsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        String[] projection = {
//                ProductEntry._ID,
//                ProductEntry.COLUMN_PRODUCT_NAME,
//                ProductEntry.COLUMN_PRODUCT_QUANTITY,
//                ProductEntry.COLUMN_PRODUCT_PRICE,
//                ProductEntry.COLUMN_PRODUCT_IMAGE};
//
//        return new CursorLoader(this,
//                ProductEntry.CONTENT_URI,
//                projection,
//                null,
//                null,
//                null
//        );
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        mCursorAdapter.swapCursor(null);
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
            if (pref.getString("user_image", "").length() > 0) {
                Bitmap image = decodeToBase64(pref.getString("user_image", ""));
                profileImageView.setImageBitmap(image);
            } else {
                profileImageView.setImageDrawable(getResources().getDrawable(R.drawable.profile));
            }
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
    public ArrayList<String> getCategoryStats() {
        ArrayList<String> number = new ArrayList<>();
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

        return number;

    }


    public ArrayList<Question> populatePreviousQuestions() {
        ArrayList<Question> result = new ArrayList<>();

        // Run query
        Uri uri = QuestionContract.QuestionEntry.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String question = cursor.getString(cursor.getColumnIndex(QuestionContract.QuestionEntry.COLUMN_QUESTION));
                String answer = cursor.getString(cursor.getColumnIndex(QuestionContract.QuestionEntry.COLUMN_CORRECT_ANSWER));
                result.add(new Question(question, answer));
                cursor.moveToNext();
            }
            // always close the cursor
            cursor.close();
        }
        return result;
    }
}

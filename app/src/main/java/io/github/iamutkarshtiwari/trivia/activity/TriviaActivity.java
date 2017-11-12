package io.github.iamutkarshtiwari.trivia.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.iamutkarshtiwari.trivia.R;
import io.github.iamutkarshtiwari.trivia.models.QuestionContract.QuestionEntry;
import io.github.iamutkarshtiwari.trivia.models.User;
import io.github.iamutkarshtiwari.trivia.models.UserPrefs;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class TriviaActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {


    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_OUT = 9002;
    private static final int RC_CATEGORY = 9003;
    private static final int RC_PREFERENCE = 9004;
    private static final int RC_PROFILE = 9005;
    private static final int RC_LEADERBOARD = 9006;
    private static final int RC_START = 9007;
    private static final String MY_PREFS_NAME = "TriviaSettings";
    private static String correctOption = "";
    private static ArrayList<String> options = new ArrayList<>();
    private static boolean isCurrentQuestionBooleanStyled = false;
    private static boolean isInFront = true;
    private static int correctOptionIndex = -1;
    private static String hitURL = "";
    private static LinearLayout nextQuestionBtn, removeOneBtn, extraSecondsBtn;
    private final String TRIVIA_URL = "https://opentdb.com/api.php?amount=1";
    private final String JSERVICE_URL = "http://jservice.io/api/random?count=1";
    private FirebaseAuth mAuth;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private NavigationView navigationView;
    private DatabaseReference mDatabase;
    private GoogleApiClient mGoogleApiClient;
    private UserPrefs currentUserPrefs;
    private ProgressBar countdownProgress;
    private CountDownTimer countDownTimer;
    private TextView progressValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        findViewById(R.id.appbar).bringToFront();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Firebase instance
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        pref = this.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = pref.edit();

        // Send to confirmation screen
        sendToStartActivity();

        // Loads user prefs
        loadPreferences();
        // Load player name & score
        ((TextView) findViewById(R.id.playerName)).setText(pref.getString("user_name", ""));
        // ((TextView) findViewById(R.id.playerScore)).setText(pref.getString("user_name", ""));

        // Next question
        nextQuestionBtn = (LinearLayout) findViewById(R.id.next_question);
        nextQuestionBtn.setOnClickListener(this);

        // Remove one option
        removeOneBtn = (LinearLayout) findViewById(R.id.remove_one);
        removeOneBtn.setOnClickListener(this);

        // Extra seconds
        extraSecondsBtn = (LinearLayout) findViewById(R.id._15_seconds);
        extraSecondsBtn.setOnClickListener(this);

        // EditText
        final EditText editText = (EditText) findViewById(R.id.written_option);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    compareAnswers(editText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Hide indication symbol
                toggleTextTypeSymbol(View.INVISIBLE);
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });


        // Load the countdown timer
        Animation an = new RotateAnimation(0.0f, 270.0f, 90f, 90f);
        an.setFillAfter(true);
        countdownProgress = (ProgressBar) findViewById(R.id.progressBar);
        countdownProgress.startAnimation(an);
        progressValue = (TextView) findViewById(R.id.progressValue);

        // Load a question on start
        nextQuestion();

        // Attach listeners to multi choice option buttons
        for (int i = 0; i < 6; i++) {
            String optionID = ((i < 4) ? "option" : "boolean") + ((i % 4) + 1);
            int resID = getResources().getIdentifier(optionID, "id", getPackageName());

            View includedLayout = findViewById(resID);
            Button button = (Button) includedLayout.findViewById(R.id.option);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Send clicks to class listener
                    checkAnswer(v);
                }
            });
        }
    }

    /**
     * Redirect to 'Shall we start' prompt screen
     */
    public void sendToStartActivity() {
        Intent intent = new Intent(TriviaActivity.this, StartActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        isInFront = true;
        toggleQuestionPanelVisibilty(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        isInFront = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CATEGORY) {
            loadPreferences();
        } else if (requestCode == RC_PREFERENCE) {
            loadPreferences();
        } else if (requestCode == RC_START) {
            onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivityForResult(intent, RC_PROFILE);
        } else if (id == R.id.nav_leaderboard) {
            Intent intent = new Intent(getApplicationContext(), LeaderboardActivity.class);
            startActivityForResult(intent, RC_LEADERBOARD);
        } else if (id == R.id.nav_category) {
            Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
            startActivityForResult(intent, RC_CATEGORY);

        } else if (id == R.id.nav_preferences) {
            Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);
            startActivityForResult(intent, RC_PREFERENCE);
        } else if (id == R.id.nav_logout) {
            // Confirmation alert
            AlertDialog.Builder builder = new AlertDialog.Builder(TriviaActivity.this);
            builder.setMessage(getStringFromID(R.string.wanna_logout));
            builder.setPositiveButton(getStringFromID(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    signOut();
                    clearPreferences();
                    sendToLogin();
                }
            });

            builder.setNegativeButton(getStringFromID(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

        }
        return closeDrawer();
    }

    public boolean closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if (id == R.id.next_question) {
            nextQuestion();
        } else if (id == R.id.remove_one) {
            removeOneOption(view);
        } else if (id == R.id._15_seconds) {
            giveExtraSeconds(view);
        }
    }

    /**
     * Shows next question
     */
    public void nextQuestion() {
        String params = "";
        String difficulty = "";

        // Hide network error message
        toggleNetworkMessage(View.INVISIBLE);

        // Hide all question details
        toggleQuestionAndCategoryVisibilty(View.INVISIBLE);

        // Hide all option views
        toggleMultiChoiceOptionView(View.INVISIBLE);
        toggleTextTypeView(View.INVISIBLE);
        toggleTextTypeSymbol(View.INVISIBLE);
        toggleBooleanChoiceOptionView(View.INVISIBLE);

        // Disable remove-option and extra-time options
        disableOptionButton(removeOneBtn);
        disableOptionButton(extraSecondsBtn);
        disableOptionButton(nextQuestionBtn);

        // Stop previous timers
        stopTimer();

        if (currentUserPrefs.getCategories().size() > 0) {
            params += "&category=" + getRandomItem(currentUserPrefs.getCategories());
        }
        if (currentUserPrefs.getDifficulty().size() > 0) {
            difficulty = getRandomItem(currentUserPrefs.getDifficulty());
            params += "&difficulty=" + difficulty;
        }
        if (currentUserPrefs.getTypes().size() > 0) {
            if (difficulty.equalsIgnoreCase("hard")) {
                params += "&type=" + "multiple";
            } else {
                String type = getRandomItem(currentUserPrefs.getTypes());
                params += "&type=" + type;
            }
        }


        Random random = new Random();
        int weight = random.nextInt(10) + 1;
        if (difficulty.equalsIgnoreCase("easy")) {
            if (weight < 7) {
                // Question from TRIVIA DB
                hitURL = TRIVIA_URL;
            } else {
                // Question from JSERVICE.IO
                hitURL = JSERVICE_URL;
                params = "";
            }
        } else if (difficulty.equalsIgnoreCase("medium")) {
            if (weight <= 4) {
                // Question from TRIVIA DB
                hitURL = TRIVIA_URL;
            } else {
                // Question from JSERVICE.IO
                hitURL = JSERVICE_URL;
                params = "";
            }
        } else {
            if (weight <= 2) {
                // Question from TRIVIA DB
                hitURL = TRIVIA_URL;
            } else {
                // Question from JSERVICE.IO
                hitURL = JSERVICE_URL;
                params = "";
            }
        }

        String url = hitURL + params;
        Log.e("URL HIT: ", url);
        new HttpGetRequest().execute(url);
    }

    /**
     * Returns random item from passed ArrayList
     *
     * @param list list from which random item is picked
     * @param <T>  generic datatype
     * @return random item from the list
     */
    private <T> T getRandomItem(ArrayList<T> list) {
        Random random = new Random();
        int listSize = list.size();
        int randomIndex = random.nextInt(listSize);
        return list.get(randomIndex);
    }

    /**
     * Replaces & with "and"
     * Remove articles (a|an|the)
     * Removes white spaces
     * Removes special characters
     *
     * @param str to be sanitized
     * @return sanitized string
     */
    public String sanitizeString(String str) {
        str = str.replaceAll("&", " and ");
        str = str.replaceAll("[^\\w\\s]", "");
        str = str.replaceAll("[\\s+]", " ");
        str = str.replaceAll("^(the|a|an) ]", "");
        str = str.replaceAll("[\\?+$/]", "");
        str = str.trim();
        return str;
    }

    /**
     * Parse the JSON response from triviaDB
     *
     * @param jsonObject containing the response
     */
    public void retrieveQuestion(JSONObject jsonObject) {
        if (jsonObject != null) {
            String question = "";
            String category = "";
            String type = "";
            options = new ArrayList<>();
            try {
                // If question is served from OPEN_TRIVIA DB
                if (hitURL.equalsIgnoreCase(TRIVIA_URL)) {
                    // If a blank response, find next question
                    if (jsonObject.getJSONArray("results").length() == 0) {
                        nextQuestion();
                        return;
                    }
                    Log.e("JSON ", jsonObject.getJSONArray("results").getJSONObject(0).getString("question"));
                    question = Jsoup.parse(jsonObject.getJSONArray("results").getJSONObject(0).getString("question")).text();
                    category = Jsoup.parse(jsonObject.getJSONArray("results").getJSONObject(0).getString("category")).text();

                    if (category.length() > 15 && category.substring(0, 13).equalsIgnoreCase("Entertainment")) {
                        category = category.substring(15);
                    } else if (category.length() > 8 && category.substring(0, 8).equalsIgnoreCase("Science:")) {
                        category = category.substring(9);
                    }

                    type = Jsoup.parse(jsonObject.getJSONArray("results").getJSONObject(0).getString("type")).text();
                    correctOption = Jsoup.parse(jsonObject.getJSONArray("results").getJSONObject(0).getString("correct_answer")).text();
                    options.add(correctOption);

                    if (!type.equalsIgnoreCase("boolean")) {
                        for (int i = 1; i < 4; i++) {
                            options.add(Jsoup.parse(jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("incorrect_answers").getString(i - 1)).text());
                        }
                    } else {
                        options.add(Jsoup.parse(jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("incorrect_answers").getString(0)).text());
                    }
                    Collections.shuffle(options);
                } else {
                    // If quesiton is served from JSERVICE.IO
                    if (jsonObject.getString("question").length() == 0) {
                        nextQuestion();
                        return;
                    } else {
                        question = Jsoup.parse(jsonObject.getString("question")).text();
                        category = Jsoup.parse(jsonObject.getJSONObject("category").getString("title")).text();
                        correctOption = (Jsoup.parse(jsonObject.getString("answer")).text());
                        Log.e("CORRECT OPTION: ", correctOption);
                    }
                }

            } catch (JSONException e) {
                Log.e("ERROR: ", "Could not parse question");
                // Hide question-option panels
                stopTimer();
                toggleQuestionPanelVisibilty(View.INVISIBLE);
                toggleNetworkMessage(View.VISIBLE);
                return;
            }

            TextView questionView = (TextView) findViewById(R.id.question);
            questionView.setText(question);

            TextView categoryView = (TextView) findViewById(R.id.category);
            categoryView.setText(category);

            if (hitURL.equalsIgnoreCase(TRIVIA_URL)) {
                if (!type.equalsIgnoreCase("boolean")) {
                    for (int i = 0; i < 4; i++) {
                        String optionID = "option" + (i + 1);
                        int resID = getResources().getIdentifier(optionID, "id", getPackageName());

                        if (options.get(i).equalsIgnoreCase(correctOption)) {
                            correctOptionIndex = i;
                        }

                        View includedLayout = findViewById(resID);
                        Button option = (Button) includedLayout.findViewById(R.id.option);
                        option.setText(options.get(i));

                        // Fix the alpha and visibility from previous answer result
                        includedLayout.setAlpha(1.0f);
                        includedLayout.setVisibility(View.VISIBLE);

                        ImageView symbol = (ImageView) includedLayout.findViewById(R.id.symbol);
                        symbol.setVisibility(View.INVISIBLE);
                    }
                    enableOptionButton(removeOneBtn);
                } else {
                    for (int i = 0; i < 2; i++) {
                        String optionID = "boolean" + (i + 1);
                        int resID = getResources().getIdentifier(optionID, "id", getPackageName());

                        if (options.get(i).equalsIgnoreCase(correctOption)) {
                            correctOptionIndex = i;
                        }

                        View includedLayout = findViewById(resID);
                        Button option = (Button) includedLayout.findViewById(R.id.option);
                        option.setText(options.get(i));

                        // Fix the alpha and visibility from previous answer result
                        includedLayout.setAlpha(1.0f);
                        includedLayout.setVisibility(View.VISIBLE);

                        ImageView symbol = (ImageView) includedLayout.findViewById(R.id.symbol);
                        symbol.setVisibility(View.INVISIBLE);
                    }
                }

                // Display question details
                toggleQuestionPanelVisibilty(View.VISIBLE);
                enableClickOnOptions(true);
                if (type.equalsIgnoreCase("boolean")) {
                    toggleBooleanChoiceOptionView(View.VISIBLE);
                } else {
                    toggleMultiChoiceOptionView(View.VISIBLE);
                }
            } else {
                toggleTextTypeView(View.VISIBLE);
            }

            insertQuestion(question, correctOption);

            toggleNetworkMessage(View.INVISIBLE);
            toggleQuestionPanelVisibilty(View.VISIBLE);
            toggleQuestionAndCategoryVisibilty(View.VISIBLE);
            enableOptionButton(extraSecondsBtn);
            enableOptionButton(nextQuestionBtn);

            // Start countdown timer
            startTimer(41);
        } else {
            // Hide question-option panels
            toggleQuestionPanelVisibilty(View.INVISIBLE);
            toggleNetworkMessage(View.VISIBLE);
            enableOptionButton(nextQuestionBtn);
        }
    }

    public void toggleQuestionAndCategoryVisibilty(int flag) {
        TextView questionView = (TextView) findViewById(R.id.question);
        questionView.setVisibility(flag);

        TextView categoryView = (TextView) findViewById(R.id.category);
        categoryView.setVisibility(flag);
    }

    /**
     * Toggles editText panel visibility
     *
     * @param flag
     */
    public void toggleTextTypeView(int flag) {
        EditText writtenAnswer = (EditText) findViewById(R.id.written_option);
        writtenAnswer.setVisibility(flag);
        writtenAnswer.setText("");

        TextView editTextLabel = (TextView) findViewById(R.id.edittext_label);
        editTextLabel.setVisibility(flag);
    }

    /**
     * Toggle indicator symbol over editText
     *
     * @param flag
     * @return indicator symbol imageView
     */
    public ImageView toggleTextTypeSymbol(int flag) {
        ImageView answerSymbol = (ImageView) findViewById(R.id.edittext_symbol);
        answerSymbol.setVisibility(flag);
        return answerSymbol;
    }

    /**
     * Toggles multiple choice option visibility
     *
     * @param flag
     */
    public void toggleMultiChoiceOptionView(int flag) {
        TableLayout optionPanel = (TableLayout) findViewById(R.id.option_grid);
        optionPanel.setVisibility(flag);

    }

    public void toggleBooleanChoiceOptionView(int flag) {
        TableLayout booleanOptionPanel = (TableLayout) findViewById(R.id.boolean_option_grid);
        booleanOptionPanel.setVisibility(flag);
    }

    /**
     * Checks selected answer from provided options
     *
     * @param view
     */
    public void checkAnswer(View view) {
        Log.e("CLICK : ", view.getResources().getResourceName(view.getId()));
        stopTimer();
        Button input = (Button) view;
        String pressedOption = input.getText().toString();

        int index = 1;
        String id = options.size() == 2 ? "boolean" : "option";
        for (String option : options) {
            int resID = getResources().getIdentifier(id + index, "id", getPackageName());
            View includedLayout = findViewById(resID);

            Button optionButton = (Button) includedLayout.findViewById(R.id.option);
            ImageView symbol = (ImageView) includedLayout.findViewById(R.id.symbol);
            // If pressed option
            if (option.equalsIgnoreCase(pressedOption)) {
                symbol.setVisibility(View.VISIBLE);
                // If wrong option
                if (!option.equalsIgnoreCase(correctOption)) {
                    symbol.setImageDrawable(getResources().getDrawable(R.drawable.wrong));
                }

            }
            if (option.equalsIgnoreCase(correctOption)) {
                symbol.setVisibility(View.VISIBLE);
                symbol.setImageDrawable(getResources().getDrawable(R.drawable.right));
            } else {
                includedLayout.setAlpha(0.5f);
            }
            index++;
        }

        // Disable clicks on option until further set of questions
        enableClickOnOptions(false);
        // Disable remove-option and extra-time options
        disableOptionButton(removeOneBtn);
        disableOptionButton(extraSecondsBtn);

    }

    /**
     * Does a fuzzy string comparison between entered text
     * and correct answer
     *
     * @param enteredText text entered by the user
     */
    public void compareAnswers(String enteredText) {
        int words = correctOption.isEmpty() ? 0 : correctOption.split("\\s+").length;
        int ratio;
        if (words == 1) {
            int ratio1 = FuzzySearch.ratio(sanitizeString(enteredText), sanitizeString(correctOption));
            ratio = ratio1;
        } else {
            int ratio2 = FuzzySearch.partialRatio(sanitizeString(enteredText), sanitizeString(correctOption));
            int ratio3 = FuzzySearch.weightedRatio(sanitizeString(enteredText), sanitizeString(correctOption));
            ratio = (ratio2 + ratio3) / 2;
        }

        Log.e("RATIO: ", ratio + "");

        ImageView symbol = toggleTextTypeSymbol(View.VISIBLE);
        if ((words == 1 && ratio > 90) || (words > 1 && ratio > 80)) {
            symbol.setImageDrawable(getResources().getDrawable(R.drawable.right));
            stopTimer();
        } else {
            symbol.setImageDrawable(getResources().getDrawable(R.drawable.wrong));
        }
    }

    private void insertQuestion(String question, String answer) {
        ContentValues values = new ContentValues();
        values.put(QuestionEntry.COLUMN_QUESTION, question);
        values.put(QuestionEntry.COLUMN_CORRECT_ANSWER, answer);
        getContentResolver().insert(QuestionEntry.CONTENT_URI, values);
    }

    /**
     * Removes one of the wrong options
     *
     * @param pressedButton this pressed button
     */
    public void removeOneOption(View pressedButton) {
        ArrayList<Integer> wrongOptions = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != correctOptionIndex) {
                wrongOptions.add(i);
            }
        }
        Random random = new Random();
        int listSize = wrongOptions.size();
        int randomIndex = random.nextInt(listSize);

        String optionID = "option" + (wrongOptions.get(randomIndex) + 1);
        int resID = getResources().getIdentifier(optionID, "id", getPackageName());
        View includedLayout = findViewById(resID);
        includedLayout.setVisibility(View.INVISIBLE);

        disableOptionButton(pressedButton);
    }

    /**
     * Add extra seconds
     *
     * @param pressedButton this pressed button
     */
    public void giveExtraSeconds(View pressedButton) {
        int leftTime = Integer.parseInt(progressValue.getText().toString());
        leftTime = (leftTime + 16);
        // Stop previous timers
        stopTimer();
        startTimer(leftTime);
        disableOptionButton(pressedButton);
    }

    public void enableOptionButton(View button) {
        button.setAlpha(1.0f);
        button.setClickable(true);
    }

    public void disableOptionButton(View button) {
        button.setAlpha(0.5f);
        button.setClickable(false);
    }

    /**
     * Countdown timer for questions
     *
     * @param secs seconds
     */
    public void startTimer(final int secs) {
        progressValue.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(secs * 1000, 1000) {
            // 500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                countdownProgress.setProgress((int) ((seconds / (float) (secs - 1)) * 100.0));
                progressValue.setText(String.format("%d", seconds));
            }

            @Override
            public void onFinish() {
                // Move to next question
                countdownProgress.setProgress(0);
                progressValue.setText(String.format("%d", 0));
                progressValue.setVisibility(View.INVISIBLE);
                // Move to next question if activity is active
                if (isInFront) {
                    nextQuestion();
                } else {
                    toggleQuestionPanelVisibilty(View.INVISIBLE);
                }
            }
        }.start();
    }

    /**
     * Stops any previous timers
     */
    public void stopTimer() {
        // Stop previous countdown timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countdownProgress.setProgress(0);
            progressValue.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Toggels the network error message
     *
     * @param flag to toggle visibility
     */
    public void toggleNetworkMessage(int flag) {
        TextView networkText = ((TextView) findViewById(R.id.network_issue));
        networkText.setVisibility(flag);
    }

    /**
     * Toggles Question panel visibility
     *
     * @param flag to toggle visibility
     */
    public void toggleQuestionPanelVisibilty(int flag) {
        RelativeLayout questionPanel = (RelativeLayout) findViewById(R.id.question_panel);
        questionPanel.setVisibility(flag);
    }

    /**
     * Disables clicks on options until next set of question
     *
     * @param flag to toggle click listener
     */
    public void enableClickOnOptions(boolean flag) {
        for (int i = 0; i < 6; i++) {
            String optionID = ((i < 4) ? "option" : "boolean") + ((i % 4) + 1);
            int resID = getResources().getIdentifier(optionID, "id", getPackageName());
            View includedLayout = findViewById(resID);
            Button option = (Button) includedLayout.findViewById(R.id.option);
            option.setClickable(flag);
        }
    }

    /**
     * Enable clicks on this view
     *
     * @param view of which clicks are to be enabled
     */
    public void enableTouch(View view) {
        view.setClickable(true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        createToast(R.string.google_services_error, Toast.LENGTH_SHORT);
    }

    /**
     * Toast creator
     *
     * @param messageID Message to be shown
     * @param length    Toast duration
     */
    public void createToast(int messageID, int length) {
        Toast.makeText(getApplicationContext(), String.format(getString(messageID)), length).show();

    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToLogin();
        } else {
            // Load user details
            editor.putString("user_email", currentUser.getEmail());
            editor.apply();

            // Display user name and email in navigation header view
            View navHeaderView = navigationView.getHeaderView(0);
            final TextView userName = (TextView) navHeaderView.findViewById(R.id.name);

            if (pref.getString("user_name", "").length() == 0) {
                if (currentUser.getDisplayName() != null) {
                    editor.putString("user_name", currentUser.getDisplayName());
                    editor.apply();
                    userName.setText(pref.getString("user_name", ""));
                    ((TextView) findViewById(R.id.playerName)).setText(pref.getString("user_name", ""));
                } else {
                    // Fetch user's name from database
                    mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);
                            editor.putString("user_name", user.getName());
                            editor.apply();
                            userName.setText(pref.getString("user_name", ""));
                            ((TextView) findViewById(R.id.playerName)).setText(pref.getString("user_name", ""));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("Firebase Database Error", "");
                        }
                    });
                }
            } else {
                userName.setText(pref.getString("user_name", ""));
            }

            TextView userEmail = (TextView) navHeaderView.findViewById(R.id.email);
            userEmail.setText(pref.getString("user_email", ""));

            // Set user profile image
            try {
                if (pref.getString("user_image", "").length() == 0) {
                    String photoURL = currentUser.getPhotoUrl().toString();
                    new ImageDownloader().execute(photoURL);
                } else {
                    CircleImageView profileImageView = (CircleImageView) navHeaderView.findViewById(R.id.imageView);
                    CircleImageView playerImageView = (CircleImageView) findViewById(R.id.imageView);
                    Bitmap image = decodeToBase64(pref.getString("user_image", ""));
                    profileImageView.setImageBitmap(image);
                    playerImageView.setImageBitmap(image);
                }

            } catch (Exception e) {
                Log.e("Image ERROR: ", Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.trivia, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Load all shared preferences
     */
    public void loadPreferences() {
        currentUserPrefs = new UserPrefs();
        currentUserPrefs.setName(pref.getString("user_name", ""));
        currentUserPrefs.setEmail(pref.getString("user_email", ""));
        currentUserPrefs.setCategories(pref.getString("user_categories", ""));
        currentUserPrefs.setDifficulty(pref.getString("user_difficulty", ""));
    }

    /**
     * Clear all stored preferences
     */
    public void clearPreferences() {
        editor.putString("user_name", "");
        editor.putString("user_email", "");
        editor.putString("user_difficulty", "");
        editor.putString("user_categories", "");
        editor.putString("user_image", "");
        editor.putString("user_music", "");
        editor.apply();
    }

    /**
     * Sign out from Firebase and Google
     */
    private void signOut() {
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        return;
                    }
                });
    }

    /**
     * Gets string from ID
     *
     * @param ID string ID
     * @return String value
     */
    public String getStringFromID(int ID) {
        return String.format(getString(ID));
    }

    /**
     * Redirect to login activity
     */
    public void sendToLogin() {
        Intent intent = new Intent(TriviaActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Converts bitmap image to base64
     *
     * @param img bitmap image
     * @return encoded base64 string
     */
    public String encodeToBase64(Bitmap img) {
        Bitmap image = img;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
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

    /**
     * Asyncronous class to make HTTP request to TriviaActivity questions API
     */
    public class HttpGetRequest extends AsyncTask<String, Void, JSONObject> {
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 10000;
        public static final int CONNECTION_TIMEOUT = 10000;
        TextView networkText;
        AVLoadingIndicatorView spinner;

        @Override
        protected void onPreExecute() {
            spinner = (AVLoadingIndicatorView) findViewById(R.id.spinner);
            spinner.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.loading_question)).setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            String stringUrl = params[0];
            String result;
            String inputLine;
            JSONObject jsonObject = null;
            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection = (HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //Create a new InputStreamReader
                    InputStreamReader streamReader = new
                            InputStreamReader(connection.getInputStream(), "UTF-8");
                    //Create a new buffered reader and String Builder
                    BufferedReader reader = new BufferedReader(streamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    //Check if the line we are reading is not null
                    while ((inputLine = reader.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                    //Close our InputStream and Buffered reader
                    reader.close();
                    streamReader.close();
                    //Set our result equal to our stringBuilder
                    result = stringBuilder.toString();
                    if (hitURL.equalsIgnoreCase(JSERVICE_URL)) {
                        JSONArray jsonArray = new JSONArray(result);
                        jsonObject = jsonArray.getJSONObject(0);
                    } else {
                        jsonObject = new JSONObject(result);
                    }

                } else {
                    jsonObject = null;
                }

            } catch (Exception e) {
                Log.e(e.getClass().getName(), e.getMessage(), e);
                Log.e("ERROR :", "Error connecting to network");
                jsonObject = null;
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            spinner.setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.loading_question)).setVisibility(View.INVISIBLE);
            retrieveQuestion(result);
        }
    }

    // Async class to download profile image
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... param) {
            // TODO Auto-generated method stub
            return downloadBitmap(param[0]);
        }

        @Override
        protected void onPreExecute() {
            Log.i("Async-Example", "onPreExecute Called");

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Log.i("Async-Example", "onPostExecute Called");
            View navHeaderView = navigationView.getHeaderView(0);
            CircleImageView profileImageView = (CircleImageView) navHeaderView.findViewById(R.id.imageView);
            CircleImageView playerImageView = (CircleImageView) findViewById(R.id.imageView);
            if (result != null) {
                profileImageView.setImageBitmap(result);
                playerImageView.setImageBitmap(result);
                // Save in shared preferences
                editor.putString("user_image", encodeToBase64(result));
                editor.apply();
            }
        }

        /**
         * Download bitmap image form URL
         *
         * @param photoUrl of the image
         * @return download bitmap image
         */
        private Bitmap downloadBitmap(String photoUrl) {
            // initilize the default HTTP client object
            URL url;
            HttpURLConnection connection;

            try {
                url = new URL(photoUrl);
                connection = (HttpURLConnection) url.openConnection();
            } catch (Exception e) {
                Log.w("ImageDownloadError", getStringFromID(R.string.image_download_error));
                return null;
            }

            // Making a Http get request
            try {
                connection.setRequestMethod("GET");
                connection.connect();
                int statusCode = connection.getResponseCode();

                //check 200 OK for success
                if (statusCode != HttpURLConnection.HTTP_OK) {
                    Log.w("ImageDownloader", getStringFromID(R.string.profile_image_error));
                    return null;
                }

                if (connection.getInputStream() != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = connection.getInputStream();
                        // Decoding stream data back into image Bitmap that android understands
                        return BitmapFactory.decodeStream(inputStream);
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        connection.disconnect();
                    }
                }
            } catch (Exception e) {
                // You Could provide a more explicit error message for IOException
                connection.disconnect();
                Log.e("ImageDownloader", getStringFromID(R.string.profile_image_error));
            }

            return null;
        }
    }


}

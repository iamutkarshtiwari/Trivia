package io.github.iamutkarshtiwari.trivia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
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
import com.google.firebase.auth.ActionCodeResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.w3c.dom.Text;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.iamutkarshtiwari.trivia.models.User;
import io.github.iamutkarshtiwari.trivia.models.UserPrefs;

public class Trivia extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {


    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_OUT = 9002;
    private static final int RC_CATEGORY = 9003;
    private static final int RC_PREFERENCE = 9004;
    private static final String MY_PREFS_NAME = "Trivia";
    private static String correctOption = "";
    private static ArrayList<String> options = new ArrayList<>();


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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

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

        // Loads user prefs
        loadPreferences();

        // Next question
        LinearLayout app_layer = (LinearLayout) findViewById(R.id.next_question);
        app_layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextQuestion();
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

        // Attach listeners to option buttons
        for (int i = 0; i < 4; i++) {
            String optionID = "option" + (i + 1);
            int resID = getResources().getIdentifier(optionID, "id", getPackageName());

            View includedLayout = findViewById(resID);
            Button option = (Button)includedLayout.findViewById(R.id.option);
            option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Send clicks to class listener
                    checkAnswer(v);
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_CATEGORY) {
            loadPreferences();
        }
    }

    @Override
    public void onClick(View view) {

//        switch (v.getId()) {
//
//            case R.id.next_question:
//                break;
//
//            case R.id.twoButton:
//                // do your code
//                break;
//
//            case R.id.threeButton:
//                // do your code
//                break;
//
//            default:
//                break;
//        }
    }



    public void checkAnswer(View view) {
        Log.e("CLICK : ", view.getResources().getResourceName(view.getId()));
        Button input = (Button) view;
        String pressedOption = input.getText().toString();

        int index = 1;
        for (String option: options) {
            int resID = getResources().getIdentifier("option" + index, "id", getPackageName());
            View includedLayout = findViewById(resID);

            Button optionButton = (Button)includedLayout.findViewById(R.id.option);
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

    }

    public void nextQuestion() {
        toggleNetworkMessage(View.INVISIBLE);
        toggleQuestionDetailsVisibilty(View.INVISIBLE);
        findViewById(R.id.category).setVisibility(View.INVISIBLE);
        resetOptionAlpha();

        // Stop previous countdown timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countdownProgress.setProgress(0);
            progressValue.setVisibility(View.INVISIBLE);
        }

        String params = "";
        if (currentUserPrefs.getCategories().size() > 0) {
            params += "&category=" + getRandomItem(currentUserPrefs.getCategories());
        }
        params += "&type=multiple";

        final String url = "https://opentdb.com/api.php?amount=1" + params;
        new HttpGetRequest().execute(url);
    }

    /**
     * Returns random item from passed ArrayList
     * @param list list from which random item is picked
     * @param <T> generic datatype
     * @return random item from the list
     */
    private <T> T getRandomItem(ArrayList<T> list)
    {
        Random random = new Random();
        int listSize = list.size();
        int randomIndex = random.nextInt(listSize);
        return list.get(randomIndex);
    }


    /**
     * Parse the JSON response from triviaDB
     * @param jsonObject containing the response
     */
    public void processValue(JSONObject jsonObject) {
        if (jsonObject != null) {
            String question = "";
            String category = "";
            options = new ArrayList<>();
            try {
                Log.e("JSON ", jsonObject.getJSONArray("results").getJSONObject(0).getString("question"));
                question = Jsoup.parse(jsonObject.getJSONArray("results").getJSONObject(0).getString("question")).text();
                category = Jsoup.parse(jsonObject.getJSONArray("results").getJSONObject(0).getString("category")).text();
                if (category.length() > 15 && category.substring(0, 13).equalsIgnoreCase("Entertainment")) {
                    category = category.substring(15);
                } else if (category.length() > 8 && category.substring(0, 8).equalsIgnoreCase("Science:")) {
                    category = category.substring(9);
                }
                correctOption = jsonObject.getJSONArray("results").getJSONObject(0).getString("correct_answer");
                options.add(correctOption);
                for (int i = 1; i < 4; i++) {
                    options.add(jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("incorrect_answers").getString(i - 1));
                }
                Collections.shuffle(options);

            } catch (JSONException e) {
                Log.e("ERROR: ", "Could not parse question");
                // Hide question-option panels
                toggleQuestionPanelVisibilty(View.INVISIBLE);
                toggleNetworkMessage(View.VISIBLE);
                return;
            }

            TextView questionView = (TextView) findViewById(R.id.question);
            questionView.setText(question);

            TextView categoryView = (TextView) findViewById(R.id.category);
            categoryView.setText(category);

            for (int i = 0; i < 4; i++) {
                String optionID = "option" + (i + 1);
                int resID = getResources().getIdentifier(optionID, "id", getPackageName());

                View includedLayout = findViewById(resID);
                Button option = (Button)includedLayout.findViewById(R.id.option);
                option.setText(Jsoup.parse(options.get(i)).text());
                // Fix the alpha from previous answer result
                option.setAlpha(1.0f);

                ImageView symbol = (ImageView) includedLayout.findViewById(R.id.symbol);
                symbol.setVisibility(View.INVISIBLE);
            }

            // Display question details
            toggleNetworkMessage(View.INVISIBLE);
            toggleQuestionPanelVisibilty(View.VISIBLE);
            toggleQuestionDetailsVisibilty(View.VISIBLE);
            enableClickOnOptions(true);
            findViewById(R.id.category).setVisibility(View.VISIBLE);

            // Start countdown timer
            countDownTimer = Timer(41);
            countDownTimer.start();
            progressValue.setVisibility(View.VISIBLE);

        } else {
            // Hide question-option panels
            toggleQuestionPanelVisibilty(View.INVISIBLE);
            toggleNetworkMessage(View.VISIBLE);
        }
    }

    /**
     * Countdown timer for questions
     * @param secs seconds
     */
    private CountDownTimer Timer(final int secs) {
        return new CountDownTimer(secs * 1000, 1000) {
            // 500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                countdownProgress.setProgress((int)seconds);
                progressValue.setText(String.format("%d", seconds));
                // format the textview to show the easily readable format

            }
            @Override
            public void onFinish() {
                // Move to next question
                countdownProgress.setProgress(0);
                progressValue.setText(String.format("%d", 0));
                progressValue.setVisibility(View.INVISIBLE);
//                View progressView = findViewById(R.id.circularProgressBar);
//                progressView.setVisibility(View.INVISIBLE);
                nextQuestion();
            }
        };
    }


    /**
     * Resets alpha values of options to default
     */
    public void resetOptionAlpha() {
        for (int i = 0; i < 4; i++) {
            String optionID = "option" + (i + 1);
            int resID = getResources().getIdentifier(optionID, "id", getPackageName());
            View includedLayout = findViewById(resID);
            includedLayout.setAlpha(1.0f);
        }
    }

    /**
     * Toggels the network error message
     * @param flag to toggle visibility
     */
    public void toggleNetworkMessage(int flag) {
        TextView networkText = ((TextView) findViewById(R.id.network_issue));
        networkText.setVisibility(flag);
    }


    /**
     * Toggles Question + Options's visibility
     * @param flag to toggle visibility
     */
    public void toggleQuestionPanelVisibilty(int flag) {

        RelativeLayout questionPanel = (RelativeLayout) findViewById(R.id.question_panel);
        questionPanel.setVisibility(flag);

        TableLayout optionPanel = (TableLayout) findViewById(R.id.option_grid);
        optionPanel.setVisibility(flag);
    }

    /**
     * Disables clicks on options until next set of question
     * @param flag to toggle click listener
     */
    public void enableClickOnOptions(boolean flag) {
        for (int i = 0; i < 4; i++) {
            String optionID = "option" + (i + 1);
            int resID = getResources().getIdentifier(optionID, "id", getPackageName());
            View includedLayout = findViewById(resID);
            Button option = (Button) includedLayout.findViewById(R.id.option);
            option.setClickable(flag);
        }
    }

    /**
     * Toggles Question + Options text visibility
     * @param flag to toggle visibility
     */
    public void toggleQuestionDetailsVisibilty(int flag) {
        findViewById(R.id.question).setVisibility(flag);
        findViewById(R.id.option1).setVisibility(flag);
        findViewById(R.id.option2).setVisibility(flag);
        findViewById(R.id.option3).setVisibility(flag);
        findViewById(R.id.option4).setVisibility(flag);
    }


    /**
     * Disable clicks on this view
     * @param view of which clicks are to be disabled
     */
    public void disableTouch(View view) {
        view.setClickable(false);
    }

    /**
     * Enable clicks on this view
     * @param view of which clicks are to be enabled
     */
    public void enableTouch(View view) {
        view.setClickable(true);
    }


    /**
     * Asyncronous class to make HTTP request to Trivia questions API
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
            disableTouch(findViewById(R.id.next_question));
//            changeQuestionDetailsBGColor(R.color.questionPanelDim);
        }

        @Override
        protected JSONObject doInBackground(String... params){
            String stringUrl = params[0];
            String result;
            String inputLine;
            JSONObject jsonObject = null;
            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection =(HttpURLConnection)
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
                    jsonObject = new JSONObject(result);

                } else {
                    jsonObject = null;
                }

            }
            catch(Exception e){
                Log.e("ERROR :", "Error connecting to network");
                jsonObject = null;
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject result){
            spinner.setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.loading_question)).setVisibility(View.INVISIBLE);
            processValue(result);
            enableTouch(findViewById(R.id.next_question));
//            changeQuestionDetailsBGColor(R.color.white);
        }
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
            editor.commit();

            // Display user name and email in navigation header view
            View navHeaderView = navigationView.getHeaderView(0);
            final TextView userName = (TextView) navHeaderView.findViewById(R.id.name);

            if (pref.getString("user_name", "").length() == 0) {
                if (currentUser.getDisplayName() != null) {
                    editor.putString("user_name", currentUser.getDisplayName());
                    editor.commit();
                    userName.setText(pref.getString("user_name", ""));
                } else {
                    // Fetch user's name from database
                    mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);
                            editor.putString("user_name", user.getName());
                            editor.commit();
                            userName.setText(pref.getString("user_name", ""));
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
        } else if (id == R.id.nav_leaderboard) {
        } else if (id == R.id.nav_category) {
            Intent intent = new Intent(getApplicationContext(), Category.class);
            startActivityForResult(intent, RC_CATEGORY);

        } else if (id == R.id.nav_preferences) {
            Intent intent = new Intent(getApplicationContext(), Preferences.class);
            startActivityForResult(intent, RC_PREFERENCE);
        } else if (id == R.id.nav_logout) {
            // Confirmation alert
            AlertDialog.Builder builder = new AlertDialog.Builder(Trivia.this);
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

    public void loadPreferences() {
        currentUserPrefs = new UserPrefs();

        currentUserPrefs.setName(pref.getString("user_name", ""));
        currentUserPrefs.setEmail(pref.getString("user_email", ""));
        currentUserPrefs.setCategories(pref.getString("user_categories", ""));
        currentUserPrefs.setDifficulty(pref.getString("user_difficulty", ""));
        currentUserPrefs.setTypes(pref.getString("user_question_types", ""));


    }

    public void clearPreferences() {
        editor.putString("user_name", "");
        editor.putString("user_email", "");

        // Clear difficulty choices
        editor.putString("user_difficulty", "");

        // Clear category selections
        editor.putString("user_categories", "");

        // Clear question types
        editor.putString("user_question_types", "");

        // Clear image base64 string
        editor.putString("user_image", "");

        editor.commit();
    }

    private void signOut() {
        // Firebase sign out
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

    public void sendToLogin() {
        Intent intent = new Intent(Trivia.this, LoginActivity.class);
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
                editor.commit();
            }
//            simpleWaitDialog.dismiss();

        }

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


            //forming a HttoGet request
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

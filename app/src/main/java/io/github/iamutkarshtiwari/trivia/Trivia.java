package io.github.iamutkarshtiwari.trivia;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class Trivia extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final int RC_SIGN_OUT = 9002;
    private FirebaseAuth mAuth;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    NavigationView navigationView;


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

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUri = user.getPhotoUrl();

            View navHeaderView = navigationView.getHeaderView(0);

            TextView userName = (TextView) navHeaderView.findViewById(R.id.name);
            userName.setText(name);

            TextView userEmail = (TextView) navHeaderView.findViewById(R.id.email);
            userEmail.setText(email);


//            Log.e("Image ERROR: ", name);
            Log.e("Image ERROR: ", email);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("message");

            myRef.setValue("Hello, World!");



            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();

            // Downloads user profile image
            try {
                String photoURL = photoUri.toString();
                new ImageDownloader().execute(photoURL);
            } catch (Exception e) {
                Log.e("Image ERROR: ", Log.getStackTraceString(e));
                createToast(R.string.profile_image_error, Toast.LENGTH_SHORT);
            }


        }


    }

    /**
     * Converts bitmap image to base64
     * @param image bitmap image
     * @return encode base64 string
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
     * @param input base64 string
     * @return Bitmap image
     */
    public Bitmap decodeToBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }



    /**
     * Toast creator
     * @param messageID Message to be shown
     * @param length Toast duration
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
            // Handle the camera action
        } else if (id == R.id.nav_category) {

        } else if (id == R.id.nav_type) {

        } else if (id == R.id.nav_difficulty) {

            return true;
        } else if (id == R.id.nav_logout) {
            //Home is name of the activity
            AlertDialog.Builder builder = new AlertDialog.Builder(Trivia.this);
            builder.setMessage(getStringFromID(R.string.wanna_logout));
            builder.setPositiveButton(getStringFromID(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mAuth.signOut();
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

    /**
     * Gets string from ID
     * @param ID string ID
     * @return String value
     */
    public String getStringFromID(int ID) {
        return String.format(getString(ID));
    }

    public void sendToLogin() {
        Intent intent = new Intent(Trivia.this, LoginActivity.class);
        startActivity(intent);
        finish();
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
//            simpleWaitDialog = ProgressDialog.show(ImageDownladerActivity.this,
//                    "Wait", "Downloading Image");

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Log.i("Async-Example", "onPostExecute Called");
            View navHeaderView = navigationView.getHeaderView(0);
            CircleImageView profileImageView = (CircleImageView) navHeaderView.findViewById(R.id.imageView);
            if (result != null) {
                profileImageView.setImageBitmap(result);
            }
//            simpleWaitDialog.dismiss();

        }

        private Bitmap downloadBitmap(String photoUrl) {
            // initilize the default HTTP client object
            URL url;
            HttpURLConnection connection;

            try {
                url = new URL(photoUrl);
                connection = (HttpURLConnection)url.openConnection();
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

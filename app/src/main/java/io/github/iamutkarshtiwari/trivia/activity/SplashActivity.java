package io.github.iamutkarshtiwari.trivia.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.github.iamutkarshtiwari.trivia.R;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            /**
             * This method will be executed once the timer is over
             */
            @Override
            public void run() {
                // Sends to home activity
                sendToHomeActivity();
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    public void sendToHomeActivity() {
        Intent intent = new Intent(this, TriviaActivity.class);
        startActivity(intent);
        finish();
    }

}

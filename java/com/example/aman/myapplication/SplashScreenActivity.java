package com.example.aman.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Gagan Sidhu on 5/7/2016.
 */
public class SplashScreenActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        TextView txtViewTagLine = (TextView) findViewById(R.id.txt1);
       // Typeface tf = Typeface.createFromAsset(getAssets(),
      //          "fonts/gotham.ttf");
       // txtViewTagLine.setTypeface(tf);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);

        StartAnimations();
       // onShakeImage();
    }
    /*public void onShakeImage() {
        Animation shake;
        shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);

        TextView txt1,txt2,txt3;
        txt1 = (TextView) findViewById(R.id.ballon1);
        txt2 = (TextView) findViewById(R.id.ballon2);
        txt3 = (TextView) findViewById(R.id.ballon3);
        txt1.setAnimation(shake);
        txt2.setAnimation(shake);
        txt3.setAnimation(shake);
    }*/
    private void StartAnimations() {
//        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
//        anim.reset();

//        l.clearAnimation();
//        l.startAnimation(anim);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();

        TextView txtViewTagLine = (TextView) findViewById(R.id.txt1);

        txtViewTagLine.clearAnimation();
        txtViewTagLine.startAnimation(anim);


    }
}

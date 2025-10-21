package com.example.personalnoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logoImageView = findViewById(R.id.logo_image_view);
        TextView appTitleTextView = findViewById(R.id.app_title_text_view);

        Animation popup = AnimationUtils.loadAnimation(this, R.anim.popup);
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        Animation fadeSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up);
        Animation textScaleDown = AnimationUtils.loadAnimation(this, R.anim.text_scale_down);

        popup.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                logoImageView.startAnimation(rotate);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                logoImageView.startAnimation(scaleIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        scaleIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                navigateToNextScreen();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        fadeSlideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                appTitleTextView.startAnimation(textScaleDown);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        logoImageView.startAnimation(popup);
        appTitleTextView.startAnimation(fadeSlideUp);
    }

    private void navigateToNextScreen() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        finish();
    }
}

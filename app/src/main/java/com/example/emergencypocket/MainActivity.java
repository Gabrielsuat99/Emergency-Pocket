package com.example.emergencypocket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;



import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class MainActivity extends AppCompatActivity {

    private Button btnLogout;
    private Button btnMypst, btnHos, btnAbt;
    private TextView usernameTextView;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        usernameTextView = findViewById(R.id.usernameTextView);
        btnLogout = findViewById(R.id.logout);
        btnMypst = findViewById(R.id.Mypst);

        btnAbt = findViewById(R.id.about);





        ValueAnimator fadeInAnimator = ValueAnimator.ofFloat(0f, 1f);
        fadeInAnimator.setDuration(1000); // Set the duration of the fade-in animation
        fadeInAnimator.setInterpolator(new LinearInterpolator());
        fadeInAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                usernameTextView.setAlpha(alpha);
            }
        });

// Create the fade-out animation
        ValueAnimator fadeOutAnimator = ValueAnimator.ofFloat(1f, 0f);
        fadeOutAnimator.setDuration(10000); // Set the duration of the fade-out animation
        fadeOutAnimator.setInterpolator(new LinearInterpolator());
        fadeOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                usernameTextView.setAlpha(alpha);
            }
        });

// Create the AnimatorSet to combine the fade-in and fade-out animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(fadeInAnimator, fadeOutAnimator);
        animatorSet.setStartDelay(100); // Set the delay before starting the animation
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.addListener(new AnimatorSet.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet.start(); // Restart the animation
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animatorSet.start();

        // Retrieve the username from the intent
        String username = getIntent().getStringExtra("name");

// Check if username is not null or empty
        if (username != null && !username.isEmpty()) {
            db = FirebaseDatabase.getInstance().getReference().child("users").child(username);

            db.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("Name").getValue(String.class);
                        if (name != null) {
                            usernameTextView.setText("Welcome " + name);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle potential database errors
                    Toast.makeText(MainActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                }
            });
        }




        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Logout Successful", Toast.LENGTH_SHORT).show();
                Intent logoutN = new Intent(getApplicationContext(), Login.class);
                startActivity(logoutN);
            }
        });

        btnMypst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent MYposition = new Intent(getApplicationContext(), MapPosition.class);
                MYposition.putExtra("name", username);
                startActivity(MYposition);
            }
        });



        btnAbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent about = new Intent(getApplicationContext(), AboutUS.class);
                about.putExtra("name", username);
                startActivity(about);
            }
        });

    }
}






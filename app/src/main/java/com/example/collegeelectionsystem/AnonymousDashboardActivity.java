package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class AnonymousDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_dashboard);

        Button btnVote = findViewById(R.id.btnVote);
        Button btnResults = findViewById(R.id.btnResults);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnVote.setOnClickListener(v -> startActivity(new Intent(this, VotingActivity.class)));
        btnResults.setOnClickListener(v -> startActivity(new Intent(this, ResultsActivity.class)));

        btnLogout.setOnClickListener(v -> {
            // Clear anonymous session (optional: SharedPreferences if you stored anything)
            Intent intent = new Intent(this, AnonymousLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear back stack
            startActivity(intent);
            finish();
        });
    }
}

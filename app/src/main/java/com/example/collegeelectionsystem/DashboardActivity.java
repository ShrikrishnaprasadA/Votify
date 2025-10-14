package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        Button btnCandidates = findViewById(R.id.btnCandidates);
        Button btnViewParties = findViewById(R.id.btnViewParties);
        Button btnVoting = findViewById(R.id.btnVoting);
        Button btnAnnouncements = findViewById(R.id.btnAnnouncements);
        Button btnResults = findViewById(R.id.btnResults);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Show user’s email/name (for now just email)
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvWelcome.setText("Welcome, " + user.getEmail());
        }

        // Navigation (later these go to real pages)
        btnCandidates.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, CandidatesActivity.class));
        });
        btnViewParties.setOnClickListener(v ->
                startActivity(new Intent(this, PartiesActivity.class))
        );

        btnVoting.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, VotingActivity.class));
        });

        btnAnnouncements.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AnnouncementsActivity.class));
        });

        btnResults.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, ResultsActivity.class));
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        });
    }
}

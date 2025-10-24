package com.example.collegeelectionsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashSet;
import java.util.Set;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        CardView btnCandidates = findViewById(R.id.btnCandidates);
        CardView btnVoting = findViewById(R.id.btnVoting);
        CardView btnAnnouncements = findViewById(R.id.btnAnnouncements);
        CardView btnResults = findViewById(R.id.btnResults);
        CardView btnViewParties = findViewById(R.id.btnViewParties);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        TextView tvWelcome = findViewById(R.id.tvWelcome);

        // Welcome text (email or generic)
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            tvWelcome.setText(email != null ? "Logged in as " + email : "Welcome Back!");
        }

        // Subscribe to FCM topics (announcements, results)
        FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                .addOnCompleteListener(task -> {
                    // optional logging
                });
        FirebaseMessaging.getInstance().subscribeToTopic("results")
                .addOnCompleteListener(task -> {
                    // optional logging
                });

        // Quick-action clicks (CardViews)
        btnCandidates.setOnClickListener(v -> openCandidates());
        btnVoting.setOnClickListener(v -> openVoting());
        btnAnnouncements.setOnClickListener(v -> openAnnouncements());
        btnResults.setOnClickListener(v -> openResults());
        btnViewParties.setOnClickListener(v -> openParties());

        btnLogout.setOnClickListener(v -> performLogout());

        // Bottom navigation behaviour — match IDs from your bottom_nav_menu.xml
        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavItemSelected);

        // Load some stats (optional — comment out if you don't want Firestore calls)
        loadStats();
    }
    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home — optionally scroll to top
            findViewById(R.id.scrollView).scrollTo(0, 0);
            return true;
        } else if (id == R.id.nav_candidates) {
            openCandidates();
            return true;
        } else if (id == R.id.nav_vote) {
            openVoting();
            return true;
        } else if (id == R.id.nav_news) {
            openAnnouncements();
            return true;
        } else if (id == R.id.nav_results) {
            openResults();
            return true;
        }

        return false;
    }



    // --- navigation helpers ---
    private void openCandidates() {
        startActivity(new Intent(this, CandidatesActivity.class));
    }

    private void openVoting() {
        startActivity(new Intent(this, VotingActivity.class));
    }

    private void openAnnouncements() {
        startActivity(new Intent(this, AnnouncementsActivity.class));
    }

    private void openResults() {
        startActivity(new Intent(this, ResultsActivity.class));
    }

    private void openParties() {
        startActivity(new Intent(this, PartiesActivity.class));
    }

    // If you implement profile later, update this
    private void openProfile() {
        Toast.makeText(this, "Profile not implemented", Toast.LENGTH_SHORT).show();
    }

    // --- logout ---
    private void performLogout() {
        if (mAuth != null) {
            mAuth.signOut();
        }

        // Unsubscribe to avoid receiving notifications after logout
        FirebaseMessaging.getInstance().unsubscribeFromTopic("announcements");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("results");

        Intent intent = new Intent(DashboardActivity.this, StudentLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- statistics loader (optional) ---
    private void loadStats() {
        // Registered users count
        db.collection("users").get().addOnSuccessListener(snap -> {
            int usersCount = snap.size();
            // If you'd like to display it: create a TextView with id in XML and set text here.
            // e.g. TextView tvTotalVoters = findViewById(R.id.tvTotalVoters); tvTotalVoters.setText(String.valueOf(usersCount));
        }).addOnFailureListener(e -> {
            // ignore or log
        });

        // Candidate count and unique positions
        db.collection("candidate").get().addOnSuccessListener(snap -> {
            int candidatesCount = snap.size();
            Set<String> positions = new HashSet<>();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                String pos = doc.getString("position");
                if (pos != null) positions.add(pos);
            }
            int positionsCount = positions.size();
            // Put these into UI TextViews if you add ids to the layout.
        }).addOnFailureListener(e -> {
            // ignore
        });

        // Days until election (if you store electionDate in settings/election)
        db.collection("settings").document("election").get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                com.google.firebase.Timestamp ts = doc.getTimestamp("electionDate");
                if (ts != null) {
                    long diff = ts.toDate().getTime() - System.currentTimeMillis();
                    long days = Math.max(0L, diff / (24L * 60L * 60L * 1000L));
                    // set in UI if you add a TextView id
                }
            }
        }).addOnFailureListener(e -> {
            // ignore
        });
    }
}

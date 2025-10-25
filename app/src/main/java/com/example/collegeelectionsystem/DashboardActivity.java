package com.example.collegeelectionsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
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

    private static final String PREFS = "votify_prefs";
    private static final String KEY_FCM_SUBSCRIBED = "fcm_subscribed";

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

        // Set selected bottom nav item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Welcome text (email or generic)
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            tvWelcome.setText(email != null ? "Logged in as " + email : "Welcome Back!");
        } else {
            tvWelcome.setText("Welcome Back!");
        }

        // Subscribe to FCM topics once (remember in prefs)
        subscribeToTopicsOnce();

        // Quick-action clicks (CardViews)
        btnCandidates.setOnClickListener(v -> openCandidates());
        btnVoting.setOnClickListener(v -> openVoting());
        btnAnnouncements.setOnClickListener(v -> openAnnouncements());
        btnResults.setOnClickListener(v -> openResults());
        btnViewParties.setOnClickListener(v -> openParties());

        btnLogout.setOnClickListener(v -> performLogout());

        // Bottom navigation behaviour — match IDs from your bottom_nav_menu.xml
        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavItemSelected);

        // Load some stats (optional)
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
        Intent i = new Intent(this, CandidatesActivity.class);
        // bring to front if exists rather than creating duplicate
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
    }

    private void openVoting() {
        Intent i = new Intent(this, VotingActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
    }

    private void openAnnouncements() {
        Intent i = new Intent(this, AnnouncementsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
    }

    private void openResults() {
        Intent i = new Intent(this, ResultsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
    }

    private void openParties() {
        Intent i = new Intent(this, PartiesActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
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

        // Clear the subscription flag so next login can resubscribe
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_FCM_SUBSCRIBED).apply();

        Intent intent = new Intent(DashboardActivity.this, StudentLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- statistics loader (optional) ---
    private void loadStats() {
        if (db == null) return;

        // Registered users count
        db.collection("users").get().addOnSuccessListener(snap -> {
            int usersCount = snap.size();
            // If you'd like to display it: create a TextView with id in XML and set text here.
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

    // Subscribe to FCM topics only once per device-install/login cycle
    private void subscribeToTopicsOnce() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean already = prefs.getBoolean(KEY_FCM_SUBSCRIBED, false);
        if (already) return;

        FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                .addOnCompleteListener(task -> {
                    // optional logging
                });

        FirebaseMessaging.getInstance().subscribeToTopic("results")
                .addOnCompleteListener(task -> {
                    // when done, mark subscribed (even if one failed we mark; adjust logic if you need strict success)
                    prefs.edit().putBoolean(KEY_FCM_SUBSCRIBED, true).apply();
                });
    }
}

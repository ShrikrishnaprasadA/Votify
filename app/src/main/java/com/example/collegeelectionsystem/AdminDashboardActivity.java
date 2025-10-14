package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnAddCandidate = findViewById(R.id.btnAddCandidate);
        Button btnAddParty = findViewById(R.id.btnAddParty);
        Button btnAddAnnouncement = findViewById(R.id.btnAddAnnouncement);
        Button btnManageResults = findViewById(R.id.btnManageResults);
        Button btnGenerateTokens = findViewById(R.id.btnGenerateTokens);
        Button btnToggleResults = findViewById(R.id.btnToggleResults);

        db = FirebaseFirestore.getInstance();

        // Navigation
        btnAddCandidate.setOnClickListener(v -> startActivity(new Intent(this, AddCandidateActivity.class)));
        btnAddParty.setOnClickListener(v -> startActivity(new Intent(this, AddPartyActivity.class)));
        btnAddAnnouncement.setOnClickListener(v -> startActivity(new Intent(this, AddAnnouncementActivity.class)));
        btnManageResults.setOnClickListener(v -> startActivity(new Intent(this, ResultsActivity.class)));
        btnGenerateTokens.setOnClickListener(v -> startActivity(new Intent(this, GenerateTokenActivity.class)));

        // Publish / Unpublish Results
        btnToggleResults.setOnClickListener(v -> toggleResults());
    }

    private void toggleResults() {
        db.collection("settings").document("election").get().addOnSuccessListener(doc -> {
            boolean current = doc.exists() && Boolean.TRUE.equals(doc.getBoolean("resultsPublished"));
            boolean newValue = !current;

            db.collection("settings").document("election")
                    .update("resultsPublished", newValue)
                    .addOnSuccessListener(aVoid -> {
                        if (newValue) {
                            archiveCurrentResults(); // ⬅️ archive snapshot
                            Toast.makeText(this, "Results published ✅", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Results unpublished ❌", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void archiveCurrentResults() {
        // aggregate votes -> write to election_results/{yyyyMMdd_HHmm}/positions/{position}/tallies
        String archiveId = new java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault()).format(new java.util.Date());
        db.collection("votes").get().addOnSuccessListener(sn -> {
            // Map<position, Map<candidateId, count>>
            java.util.Map<String, java.util.Map<String,Integer>> map = new java.util.HashMap<>();
            sn.forEach(d -> {
                String pos = d.getString("position"); String cid = d.getString("candidateId");
                if (pos==null || cid==null) return;
                map.putIfAbsent(pos, new java.util.HashMap<>());
                java.util.Map<String,Integer> inner = map.get(pos);
                inner.put(cid, inner.getOrDefault(cid,0)+1);
            });

            // write each position as a subcollection
            map.forEach((position, tallies) -> {
                db.collection("election_results").document(archiveId)
                        .collection("positions").document(position)
                        .set(new java.util.HashMap<String,Object>(){{ put("tallies", tallies); }});
            });

            // also mark metadata
            db.collection("election_results").document(archiveId)
                    .set(new java.util.HashMap<String,Object>(){{
                        put("createdAt", com.google.firebase.Timestamp.now());
                        put("label", "Election "+archiveId);
                    }}, com.google.firebase.firestore.SetOptions.merge());
        });
    }

}

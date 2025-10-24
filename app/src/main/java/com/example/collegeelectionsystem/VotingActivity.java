package com.example.collegeelectionsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VotingActivity: shows multiple positions (each with candidates).
 * On submit:
 *  - ensure voterId (uid or anonymous token id)
 *  - fetch existing votes for voter
 *  - if conflicts (already voted for any position) -> show message and abort
 *  - otherwise write all votes in a WriteBatch with timestamp
 */
public class VotingActivity extends AppCompatActivity {

    private RecyclerView recyclerPositions;
    private Button btnSubmit;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private PositionAdapter adapter;

    // SharedPreferences key where anonymous login should have stored the token document id
    private static final String PREFS = "votify_prefs";
    private static final String KEY_ANON_TOKEN_DOC_ID = "anon_token_doc_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        recyclerPositions = findViewById(R.id.recyclerPositions);
        btnSubmit = findViewById(R.id.btnSubmitVote);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerPositions.setLayoutManager(new LinearLayoutManager(this));

        loadCandidatesGroupedByPosition();

        btnSubmit.setOnClickListener(v -> submitVotes());
    }

    private void loadCandidatesGroupedByPosition() {
        db.collection("candidate").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Candidate> all = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Candidate c = doc.toObject(Candidate.class);
                    c.setId(doc.getId());
                    all.add(c);
                }

                // Group by position
                Map<String, List<Candidate>> grouped = new HashMap<>();
                for (Candidate c : all) {
                    String pos = c.getPosition() != null ? c.getPosition() : "Others";
                    grouped.putIfAbsent(pos, new ArrayList<>());
                    grouped.get(pos).add(c);
                }

                List<String> positions = new ArrayList<>(grouped.keySet());
                // You may want to sort positions; for now keep insertion order
                adapter = new PositionAdapter(this, positions, grouped);
                recyclerPositions.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Error loading candidates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getVoterIdOrNull() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String anonId = prefs.getString(KEY_ANON_TOKEN_DOC_ID, null);
            return anonId; // may be null if anonymous login did not save token id
        }
    }

    private void submitVotes() {
        Map<String, Candidate> selected = adapter.getSelectedMap();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one candidate", Toast.LENGTH_SHORT).show();
            return;
        }

        String voterId = getVoterIdOrNull();
        if (voterId == null) {
            Toast.makeText(this, "Unable to identify voter. For anonymous login ensure token is used.", Toast.LENGTH_LONG).show();
            return;
        }

        // disable button while processing
        btnSubmit.setEnabled(false);

        // Fetch all votes by this voter to detect already-voted positions
        db.collection("votes").whereEqualTo("voterId", voterId).get()
                .addOnSuccessListener(querySnapshot -> {
                    // Build set of positions already voted by this voter
                    java.util.Set<String> already = new java.util.HashSet<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String pos = doc.getString("position");
                        if (pos != null) already.add(pos);
                    }

                    // Check conflicts
                    List<String> conflicts = new ArrayList<>();
                    for (String pos : selected.keySet()) {
                        if (already.contains(pos)) conflicts.add(pos);
                    }

                    if (!conflicts.isEmpty()) {
                        // user already voted for these positions — abort and notify
                        String msg = "You have already voted for: " + String.join(", ", conflicts);
                        Toast.makeText(VotingActivity.this, msg, Toast.LENGTH_LONG).show();
                        btnSubmit.setEnabled(true);
                        return;
                    }

                    // No conflicts — write all votes in a batch
                    WriteBatch batch = db.batch();
                    for (Map.Entry<String, Candidate> e : selected.entrySet()) {
                        String pos = e.getKey();
                        Candidate c = e.getValue();
                        if (c == null) continue;

                        Map<String, Object> vote = new HashMap<>();
                        vote.put("candidateId", c.getId());
                        vote.put("position", pos);
                        vote.put("voterId", voterId);
                        vote.put("timestamp", Timestamp.now());

                        // create new doc ref
                        com.google.firebase.firestore.DocumentReference docRef = db.collection("votes").document();
                        batch.set(docRef, vote);
                    }

                    // Commit batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(VotingActivity.this, "Votes submitted successfully!", Toast.LENGTH_SHORT).show();

                                // Optionally: disable further voting in UI for these positions
                                btnSubmit.setEnabled(false);
                                // Optionally navigate away or show summary
                                // finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(VotingActivity.this, "Error submitting votes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                btnSubmit.setEnabled(true);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(VotingActivity.this, "Error checking previous votes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);
                });
    }
}

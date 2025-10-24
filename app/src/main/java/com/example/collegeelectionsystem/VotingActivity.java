package com.example.collegeelectionsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VotingActivity: shows multiple positions (each with candidates).
 * Submits all selected votes in a single WriteBatch after checking for conflicts.
 */
public class VotingActivity extends AppCompatActivity {

    private RecyclerView recyclerPositions;    // matches activity_voting.xml -> recyclerVoting
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

        // IMPORTANT: this id must match the RecyclerView id in your activity_voting.xml
        recyclerPositions = findViewById(R.id.recyclerVoting);
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
                    // ensure candidate has its document id
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
            return prefs.getString(KEY_ANON_TOKEN_DOC_ID, null); // may be null if anonymous login did not save token id
        }
    }

    private void submitVotes() {
        // Guard: adapter may not be initialized yet
        if (adapter == null) {
            Toast.makeText(this, "Please wait — candidates are still loading.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Candidate> selected = adapter.getSelectedMap();
        if (selected == null || selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one candidate", Toast.LENGTH_SHORT).show();
            return;
        }

        String voterId = getVoterIdOrNull();
        if (voterId == null || voterId.isEmpty()) {
            Toast.makeText(this, "Unable to identify voter. For anonymous login ensure token is used.", Toast.LENGTH_LONG).show();
            return;
        }

        // disable button while processing to avoid duplicates
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
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < conflicts.size(); i++) {
                            if (i > 0) sb.append(", ");
                            sb.append(conflicts.get(i));
                        }
                        String msg = "You have already voted for: " + sb.toString();
                        Toast.makeText(VotingActivity.this, msg, Toast.LENGTH_LONG).show();
                        btnSubmit.setEnabled(true);
                        return;
                    }

                    // No conflicts — write all votes in a batch
                    WriteBatch batch = db.batch();
                    boolean addedAny = false;
                    for (Map.Entry<String, Candidate> e : selected.entrySet()) {
                        String pos = e.getKey();
                        Candidate c = e.getValue();
                        if (c == null) continue;
                        if (c.getId() == null || c.getId().isEmpty()) {
                            // safety: skip candidates missing an id (shouldn't happen if candidate.setId(doc.getId()) was done)
                            continue;
                        }

                        Map<String, Object> vote = new HashMap<>();
                        vote.put("candidateId", c.getId());
                        vote.put("position", pos);
                        vote.put("voterId", voterId);
                        vote.put("timestamp", Timestamp.now());

                        // create new doc ref
                        com.google.firebase.firestore.DocumentReference docRef = db.collection("votes").document();
                        batch.set(docRef, vote);
                        addedAny = true;
                    }

                    if (!addedAny) {
                        Toast.makeText(VotingActivity.this, "No valid votes to submit.", Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                        return;
                    }

                    // Commit batch atomically
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(VotingActivity.this, "Votes submitted successfully!", Toast.LENGTH_SHORT).show();
                                // Disable submit or finish activity
                                btnSubmit.setEnabled(false);

                                // OPTIONAL: disable selection in adapter so UI reflects vote (you would need to add a method in adapter)
                                // adapter.disableSelectedPositions(selected.keySet());
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

package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VotingActivity extends AppCompatActivity {

    private VotingAdapter adapter;
    private List<Candidate> candidateList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        RecyclerView recyclerVoting = findViewById(R.id.recyclerVoting);
        recyclerVoting.setLayoutManager(new LinearLayoutManager(this));
        Button btnSubmitVote = findViewById(R.id.btnSubmitVote);

        candidateList = new ArrayList<>();
        adapter = new VotingAdapter(this, candidateList);
        recyclerVoting.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadCandidates();

        btnSubmitVote.setOnClickListener(v -> submitVotes());
    }

    private void loadCandidates() {
        db.collection("candidate").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                candidateList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Candidate candidate = doc.toObject(Candidate.class);
                    // Store candidateId (document ID) inside object
                    candidate.setId(doc.getId());
                    candidateList.add(candidate);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error loading candidates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitVotes() {
        Map<String, Candidate> selectedVotes = adapter.getSelectedVotes();
        if (selectedVotes.isEmpty()) {
            Toast.makeText(this, "Please select at least one candidate", Toast.LENGTH_SHORT).show();
            return;
        }

        String voterId;
        if (mAuth.getCurrentUser() != null) {
            // Normal student voter
            voterId = mAuth.getCurrentUser().getUid();
        } else {
            // Anonymous voter (fallback)
            voterId = "anonymous_" + System.currentTimeMillis();
        }

        for (String position : selectedVotes.keySet()) {
            Candidate c = selectedVotes.get(position);

            if (c == null) continue;

            // Save vote with proper candidateId, position, voterId, timestamp
            Map<String, Object> vote = new HashMap<>();
            vote.put("candidateId", c.getId());
            vote.put("position", c.getPosition());
            vote.put("voterId", voterId);
            vote.put("timestamp", Timestamp.now());

            db.collection("votes").add(vote)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Vote submitted for " + position, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}

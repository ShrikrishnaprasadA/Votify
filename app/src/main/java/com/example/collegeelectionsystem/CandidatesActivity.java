package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CandidatesActivity extends AppCompatActivity {

    private CandidateAdapter adapter;
    private List<Candidate> candidateList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidates);

        RecyclerView recyclerCandidates = findViewById(R.id.recyclerCandidates);
        recyclerCandidates.setLayoutManager(new LinearLayoutManager(this));

        candidateList = new ArrayList<>();
        adapter = new CandidateAdapter(this, candidateList);
        recyclerCandidates.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadCandidates();
    }

    private void loadCandidates() {
        db.collection("candidate").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                candidateList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Candidate candidate = doc.toObject(Candidate.class);
                    candidateList.add(candidate);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error loading candidates", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SearchCandidatesActivity extends AppCompatActivity {

    private EditText etQuery, etParty, etPost;
    private Button btnSearch;
    private RecyclerView recycler;
    private CandidateAdapter adapter;
    private List<Candidate> list = new ArrayList<>();
    private FirebaseFirestore db;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_candidates);

        etQuery = findViewById(R.id.etQuery);
        etParty = findViewById(R.id.etParty);
        etPost  = findViewById(R.id.etPost);
        btnSearch = findViewById(R.id.btnSearch);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CandidateAdapter(this, list);
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        btnSearch.setOnClickListener(v -> runSearch());
    }

    private void runSearch() {
        String q = etQuery.getText().toString().trim();
        String party = etParty.getText().toString().trim();
        String post  = etPost.getText().toString().trim();

        Query query = db.collection("candidate");
        if (!TextUtils.isEmpty(party)) query = query.whereEqualTo("party", party);
        if (!TextUtils.isEmpty(post))  query = query.whereEqualTo("position", post);

        // Simple client-side contains on name if provided
        query.get().addOnSuccessListener(sn -> {
            list.clear();
            sn.forEach(d -> {
                Candidate c = d.toObject(Candidate.class);
                c.setId(d.getId());
                if (TextUtils.isEmpty(q) || (c.getName()!=null && c.getName().toLowerCase().contains(q.toLowerCase())))
                    list.add(c);
            });
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(this, "Search error: "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

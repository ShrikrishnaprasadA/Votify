package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {

    private LinearLayout resultsContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        resultsContainer = findViewById(R.id.resultsContainer);
        db = FirebaseFirestore.getInstance();

        // ✅ Check if results are published
        db.collection("settings").document("election").get().addOnSuccessListener(doc -> {
            Boolean published = doc.getBoolean("resultsPublished");

            if (published != null && published) {
                loadResults();
            } else {
                showMessage("Results are not published yet.");
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error checking results status", Toast.LENGTH_SHORT).show()
        );
        String archiveId = getIntent().getStringExtra("archiveId");
        if (archiveId != null) {
            loadArchivedResults(archiveId); // bypass publish gate
            return;
        }
    }

    private void loadArchivedResults(String archiveId) {
        db.collection("election_results").document(archiveId).collection("positions").get()
                .addOnSuccessListener(sn -> {
                    if (sn.isEmpty()) { showMessage("No data in archive."); return; }
                    for (com.google.firebase.firestore.QueryDocumentSnapshot d : sn) {
                        String position = d.getId();
                        Map<String, Long> tallies = (Map<String, Long>) d.get("tallies");
                        if (tallies == null || tallies.isEmpty()) continue;

                        // fetch candidate names for these IDs
                        db.collection("candidate").get().addOnSuccessListener(cs -> {
                            java.util.List<com.github.mikephil.charting.data.PieEntry> entries = new java.util.ArrayList<>();
                            java.util.List<CandidateResult> results = new java.util.ArrayList<>();
                            for (com.google.firebase.firestore.QueryDocumentSnapshot cdoc : cs) {
                                String cid = cdoc.getId();
                                if (tallies.containsKey(cid)) {
                                    String name = cdoc.getString("name");
                                    String party = cdoc.getString("party");
                                    int votes = tallies.get(cid).intValue();
                                    entries.add(new com.github.mikephil.charting.data.PieEntry(votes, name+" ("+party+")"));
                                    results.add(new CandidateResult(name, party, votes));
                                }
                            }
                            if (!entries.isEmpty()) {
                                CandidateResult winner = java.util.Collections.max(results, java.util.Comparator.comparingInt(cr -> cr.votes));
                                displayChart(position, entries, winner.name, winner.party, winner.votes);
                            }
                        });
                    }
                });
    }


    private void showMessage(String message) {
        TextView tvMessage = new TextView(this);
        tvMessage.setText(message);
        tvMessage.setTextSize(18f);
        tvMessage.setPadding(0, 50, 0, 0);
        resultsContainer.addView(tvMessage);
    }

    private void loadResults() {
        db.collection("votes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Map<String, Integer>> positionVotes = new HashMap<>();

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String position = doc.getString("position");
                    String candidateId = doc.getString("candidateId");

                    if (position == null || candidateId == null) continue;

                    positionVotes.putIfAbsent(position, new HashMap<>());
                    Map<String, Integer> candidateMap = positionVotes.get(position);
                    if (candidateMap == null) continue;

                    candidateMap.put(candidateId, candidateMap.getOrDefault(candidateId, 0) + 1);
                }

                if (positionVotes.isEmpty()) {
                    showMessage("No votes have been recorded yet.");
                } else {
                    for (String position : positionVotes.keySet()) {
                        Map<String, Integer> candidateVotes = positionVotes.get(position);
                        loadCandidatesForPosition(position, candidateVotes);
                    }
                }

            } else {
                Toast.makeText(this, "Error loading votes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCandidatesForPosition(String position, Map<String, Integer> candidateVotes) {
        db.collection("candidate").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<PieEntry> entries = new ArrayList<>();
                List<CandidateResult> results = new ArrayList<>();

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String candidateId = doc.getId();
                    String name = doc.getString("name");
                    String party = doc.getString("party");
                    String candidatePosition = doc.getString("position");

                    if (candidateVotes.containsKey(candidateId) && candidatePosition.equals(position)) {
                        int votes = candidateVotes.get(candidateId);
                        String label = name + " (" + party + ")";
                        entries.add(new PieEntry(votes, label));

                        results.add(new CandidateResult(name, party, votes));
                    }
                }

                if (!entries.isEmpty()) {
                    CandidateResult winner = Collections.max(results, Comparator.comparingInt(c -> c.votes));
                    displayChart(position, entries, winner.name, winner.party, winner.votes);
                }
            }
        });
    }

    private void displayChart(String position, List<PieEntry> entries,
                              String winnerName, String winnerParty, int maxVotes) {

        // Title
        TextView tvTitle = new TextView(this);
        tvTitle.setText("Results for " + position);
        tvTitle.setTextSize(18f);
        tvTitle.setPadding(0, 30, 0, 10);
        resultsContainer.addView(tvTitle);

        // Pie Chart
        PieChart pieChart = new PieChart(this);
        pieChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600
        ));

        PieDataSet dataSet = new PieDataSet(entries, "Candidates");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // ✅ More variety of colors
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();

        resultsContainer.addView(pieChart);

        // Winner Text
        if (winnerName != null) {
            TextView tvWinner = new TextView(this);
            tvWinner.setText(String.format(Locale.getDefault(),
                    "Winner: %s (%s) with %d votes",
                    winnerName, winnerParty, maxVotes));
            tvWinner.setTextSize(16f);
            tvWinner.setPadding(0, 10, 0, 20);
            resultsContainer.addView(tvWinner);
        }
    }

    private static class CandidateResult {
        String name;
        String party;
        int votes;

        CandidateResult(String name, String party, int votes) {
            this.name = name;
            this.party = party;
            this.votes = votes;
        }
    }
}

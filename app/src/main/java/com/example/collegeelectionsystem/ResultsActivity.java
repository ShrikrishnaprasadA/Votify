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
import com.google.firebase.Timestamp;
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

        // If activity started with an archiveId, show archived results (bypass publish gate)
        String archiveId = getIntent().getStringExtra("archiveId");
        if (archiveId != null && !archiveId.isEmpty()) {
            loadArchivedResults(archiveId);
            return;
        }

        // Normal current-results flow: check published flag
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
    }

    private void showMessage(String message) {
        resultsContainer.removeAllViews();
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

                    if (candidateVotes.containsKey(candidateId) && position.equals(candidatePosition)) {
                        int votes = candidateVotes.get(candidateId);
                        String label = (name != null ? name : "Unknown") + " (" + (party != null ? party : "Independent") + ")";
                        entries.add(new PieEntry(votes, label));
                        results.add(new CandidateResult(name, party, votes));
                    }
                }

                if (!entries.isEmpty()) {
                    CandidateResult winner = Collections.max(results, Comparator.comparingInt(c -> c.votes));
                    displayChart(position, entries, winner.name, winner.party, winner.votes);
                }
            } else {
                // optional: show error or fallback
            }
        });
    }

    private void displayChart(String position, List<PieEntry> entries,
                              String winnerName, String winnerParty, int maxVotes) {

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Results for " + position);
        tvTitle.setTextSize(18f);
        tvTitle.setPadding(0, 30, 0, 10);
        resultsContainer.addView(tvTitle);

        PieChart pieChart = new PieChart(this);
        pieChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600
        ));

        PieDataSet dataSet = new PieDataSet(entries, "Candidates");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();

        resultsContainer.addView(pieChart);

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

    // ----- ARCHIVE support (safe conversions) -----
    private void loadArchivedResults(String archiveId) {
        // Read positions subcollection under election_results/{archiveId}/positions
        db.collection("election_results").document(archiveId)
                .collection("positions")
                .get()
                .addOnSuccessListener(sn -> {
                    if (sn.isEmpty()) {
                        showMessage("No data in archive.");
                        return;
                    }

                    // For each archived position, tallies is stored as a map (candidateId -> number)
                    // We'll convert safely to Map<String,Integer> for rendering
                    for (QueryDocumentSnapshot posDoc : sn) {
                        String position = posDoc.getId();
                        Object talliesObj = posDoc.get("tallies");
                        Map<String, Integer> tallies = convertObjectToIntegerMap(talliesObj);

                        if (tallies == null || tallies.isEmpty()) continue;

                        // Now fetch candidate details to build chart entries
                        db.collection("candidate").get().addOnSuccessListener(cs -> {
                            List<PieEntry> entries = new ArrayList<>();
                            List<CandidateResult> results = new ArrayList<>();

                            for (QueryDocumentSnapshot cdoc : cs) {
                                String cid = cdoc.getId();
                                if (tallies.containsKey(cid)) {
                                    String name = cdoc.getString("name");
                                    String party = cdoc.getString("party");
                                    int votes = tallies.get(cid);
                                    entries.add(new PieEntry(votes, (name != null ? name : "Unknown") + " (" + (party != null ? party : "Independent") + ")"));
                                    results.add(new CandidateResult(name, party, votes));
                                }
                            }

                            if (!entries.isEmpty()) {
                                CandidateResult winner = Collections.max(results, Comparator.comparingInt(c -> c.votes));
                                displayChart(position, entries, winner.name, winner.party, winner.votes);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> showMessage("Error loading archive: " + e.getMessage()));
    }

    /**
     * Convert a Firestore-returned Object into Map<String,Integer> safely.
     * Firestore may return Map<String,Long> or Map<String, Double> or Map<String,Object>.
     */
    private Map<String, Integer> convertObjectToIntegerMap(Object obj) {
        if (obj == null) return null;
        if (!(obj instanceof Map)) return null;

        Map<?, ?> raw = (Map<?, ?>) obj;
        Map<String, Integer> out = new HashMap<>();

        for (Map.Entry<?, ?> en : raw.entrySet()) {
            Object k = en.getKey();
            Object v = en.getValue();
            if (k == null || v == null) continue;

            String key = String.valueOf(k);

            // handle possible numeric types: Long, Integer, Double
            if (v instanceof Number) {
                out.put(key, ((Number) v).intValue());
            } else {
                // try parsing as integer from String
                try {
                    int parsed = Integer.parseInt(String.valueOf(v));
                    out.put(key, parsed);
                } catch (NumberFormatException ex) {
                    // skip non-numeric value
                }
            }
        }
        return out;
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

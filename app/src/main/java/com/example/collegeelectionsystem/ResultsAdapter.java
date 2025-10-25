package com.example.collegeelectionsystem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.VH> {

    private final Context ctx;
    private final List<PositionResults> items;

    public static class PositionResults {
        public String position;
        public List<CandidateResult> candidateResults;
        public PositionResults(String position, List<CandidateResult> candidateResults) {
            this.position = position;
            this.candidateResults = candidateResults;
        }
    }

    public ResultsAdapter(Context ctx, List<PositionResults> items) {
        this.ctx = ctx;
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PositionResults pr = items.get(position);
        holder.tvPosition.setText(pr.position);

        // Build pie entries
        List<PieEntry> entries = new ArrayList<>();
        int totalVotes = 0;
        for (CandidateResult cr : pr.candidateResults) {
            entries.add(new PieEntry(cr.votes, cr.name + " (" + cr.party + ")"));
            totalVotes += cr.votes;
        }

        if (entries.isEmpty()) {
            holder.pieChart.clear();
            holder.tvWinner.setText("No votes yet");
            holder.llCandidateList.removeAllViews();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setValueTextSize(12f);

        // Simple color palette (expand if needed)
        int[] colors = new int[]{
                Color.parseColor("#2196F3"),
                Color.parseColor("#F44336"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#00BCD4")
        };
        List<Integer> colorList = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            colorList.add(colors[i % colors.length]);
        }
        dataSet.setColors(colorList);

        PieData pieData = new PieData(dataSet);
        holder.pieChart.setData(pieData);
        holder.pieChart.getDescription().setEnabled(false);
        holder.pieChart.getLegend().setEnabled(false);
        holder.pieChart.setCenterText(totalVotes + " votes");
        holder.pieChart.invalidate();

        // Determine winner
        CandidateResult winner = pr.candidateResults.get(0);
        for (CandidateResult cr : pr.candidateResults) {
            if (cr.votes > winner.votes) winner = cr;
        }
        holder.tvWinner.setText(String.format(Locale.getDefault(), "Winner: %s (%s) — %d votes",
                winner.name, winner.party, winner.votes));

        // Fill candidate list (simple textual rows)
        holder.llCandidateList.removeAllViews();
        for (CandidateResult cr : pr.candidateResults) {
            TextView row = new TextView(ctx);
            row.setText(String.format(Locale.getDefault(), "%s — %s — %d votes",
                    cr.name, cr.party == null ? "Independent" : cr.party, cr.votes));
            row.setTextSize(14f);
            row.setTextColor(Color.DKGRAY);
            row.setPadding(6, 6, 6, 6);
            holder.llCandidateList.addView(row);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvPosition, tvWinner;
        PieChart pieChart;
        LinearLayout llCandidateList;

        VH(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            pieChart = itemView.findViewById(R.id.pieChart);
            tvWinner = itemView.findViewById(R.id.tvWinner);
            llCandidateList = itemView.findViewById(R.id.llCandidateList);
        }
    }
}

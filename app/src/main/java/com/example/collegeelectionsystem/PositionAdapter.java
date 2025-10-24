package com.example.collegeelectionsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter shows positions. Each item contains a RadioGroup of candidates for that position.
 * Exposes getSelectedMap() returning Map<position, Candidate>.
 */
public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private final Context context;
    private final List<String> positions; // list of position names
    private final Map<String, List<Candidate>> groupedCandidates; // position -> list of candidates
    private final Map<String, Candidate> selectedMap = new HashMap<>(); // position -> selected candidate

    public PositionAdapter(Context ctx, List<String> positions, Map<String, List<Candidate>> grouped) {
        this.context = ctx;
        this.positions = positions;
        this.groupedCandidates = grouped;
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_position, parent, false);
        return new PositionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int positionIndex) {
        String positionName = positions.get(positionIndex);
        holder.tvPositionTitle.setText(positionName);

        // Clear existing buttons (for view reuse)
        holder.rgCandidates.removeAllViews();

        List<Candidate> list = groupedCandidates.get(positionName);
        if (list == null) return;

        // Create RadioButtons programmatically
        for (int i = 0; i < list.size(); i++) {
            Candidate c = list.get(i);
            RadioButton rb = new RadioButton(context);
            rb.setId(View.generateViewId());
            rb.setText(c.getName() + " (" + (c.getParty() == null ? "Independent" : c.getParty()) + ")");
            rb.setTag(c); // store candidate object for retrieval on click
            // style small padding
            rb.setPadding(8, 12, 8, 12);
            holder.rgCandidates.addView(rb);

            // If previously selected (e.g., after rotation), restore selection
            Candidate sel = selectedMap.get(positionName);
            if (sel != null && sel.getId() != null && sel.getId().equals(c.getId())) {
                rb.setChecked(true);
            }
        }

        // listen for selection changes
        holder.rgCandidates.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton chosen = group.findViewById(checkedId);
            if (chosen != null) {
                Object tag = chosen.getTag();
                if (tag instanceof Candidate) {
                    selectedMap.put(positionName, (Candidate) tag);
                }
            } else {
                selectedMap.remove(positionName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return positions.size();
    }

    public Map<String, Candidate> getSelectedMap() {
        return selectedMap;
    }

    static class PositionViewHolder extends RecyclerView.ViewHolder {
        TextView tvPositionTitle;
        RadioGroup rgCandidates;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPositionTitle = itemView.findViewById(R.id.tvPositionTitle);
            rgCandidates = itemView.findViewById(R.id.rgCandidates);
        }
    }
}

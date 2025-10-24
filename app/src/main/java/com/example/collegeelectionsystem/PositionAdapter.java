package com.example.collegeelectionsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter shows positions. Each item contains a list of candidate cards (with placeholder image, name, radio).
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

        // clear previous children
        holder.candidateContainer.removeAllViews();

        List<Candidate> list = groupedCandidates.get(positionName);
        if (list == null || list.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(context);

        for (Candidate c : list) {
            View candidateView = inflater.inflate(R.layout.item_candidate_vote, holder.candidateContainer, false);

            ImageView imgCandidate = candidateView.findViewById(R.id.imgCandidate);
            TextView tvName = candidateView.findViewById(R.id.tvCandidateName);
            TextView tvParty = candidateView.findViewById(R.id.tvCandidateParty);
            RadioButton radio = candidateView.findViewById(R.id.radioSelect);

            tvName.setText(c.getName() != null ? c.getName() : "Unknown");
            tvParty.setText(c.getParty() != null ? c.getParty() : "Independent");

            // Always use placeholder resource (no remote images)
            imgCandidate.setImageResource(R.drawable.ic_person_placeholder);

            // radio default off
            radio.setChecked(false);

            radio.setOnClickListener(v -> {
                // Uncheck other radios in this position
                for (int i = 0; i < holder.candidateContainer.getChildCount(); i++) {
                    View child = holder.candidateContainer.getChildAt(i);
                    RadioButton r = child.findViewById(R.id.radioSelect);
                    if (r != radio) r.setChecked(false);
                }
                // save selection
                selectedMap.put(positionName, c);
            });

            // restore previous selection if exists
            Candidate sel = selectedMap.get(positionName);
            if (sel != null && sel.getId() != null && sel.getId().equals(c.getId())) {
                radio.setChecked(true);
            }

            holder.candidateContainer.addView(candidateView);
        }
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
        LinearLayout candidateContainer;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPositionTitle = itemView.findViewById(R.id.tvPositionTitle);
            candidateContainer = itemView.findViewById(R.id.candidateContainer);
        }
    }
}

package com.example.collegeelectionsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VotingAdapter extends RecyclerView.Adapter<VotingAdapter.VotingViewHolder> {

    private Context context;
    private List<Candidate> candidateList;
    private Map<String, Candidate> selectedVotes = new HashMap<>(); // position → candidate

    public VotingAdapter(Context context, List<Candidate> candidateList) {
        this.context = context;
        this.candidateList = candidateList;
    }

    @NonNull
    @Override
    public VotingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_candidate_vote, parent, false);
        return new VotingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VotingViewHolder holder, int position) {
        Candidate candidate = candidateList.get(position);

        holder.tvName.setText(candidate.getName());
        holder.tvParty.setText("Party: " + candidate.getParty());
        holder.tvPosition.setText("Position: " + candidate.getPosition());

        holder.radioSelect.setChecked(selectedVotes.containsKey(candidate.getPosition())
                && selectedVotes.get(candidate.getPosition()).equals(candidate));

        holder.radioSelect.setOnClickListener(v -> {
            selectedVotes.put(candidate.getPosition(), candidate);
            notifyDataSetChanged(); // refresh UI to uncheck others in same position
        });
    }

    @Override
    public int getItemCount() {
        return candidateList.size();
    }

    public Map<String, Candidate> getSelectedVotes() {
        return selectedVotes;
    }

    static class VotingViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvParty, tvPosition;
        RadioButton radioSelect;

        public VotingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvParty = itemView.findViewById(R.id.tvParty);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            radioSelect = itemView.findViewById(R.id.radioSelect);
        }
    }
}

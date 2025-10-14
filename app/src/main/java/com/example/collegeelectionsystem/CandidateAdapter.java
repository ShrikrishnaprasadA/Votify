package com.example.collegeelectionsystem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder> {

    private final Context context;
    private final List<Candidate> candidateList;

    public CandidateAdapter(Context context, List<Candidate> candidateList) {
        this.context = context;
        this.candidateList = candidateList;
    }

    @NonNull
    @Override
    public CandidateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_candidate, parent, false);
        return new CandidateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CandidateViewHolder holder, int position) {
        Candidate candidate = candidateList.get(position);

        holder.tvName.setText(candidate.getName());
        holder.tvParty.setText("Party: " + candidate.getParty());
        holder.tvPosition.setText("Position: " + candidate.getPosition());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CandidateDetailsActivity.class);
            intent.putExtra("name", candidate.getName());
            intent.putExtra("party", candidate.getParty());
            intent.putExtra("position", candidate.getPosition());
            intent.putExtra("year", candidate.getYear());
            intent.putExtra("department", candidate.getDepartment());
            intent.putExtra("agenda", candidate.getAgenda());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return candidateList.size();
    }

    public static class CandidateViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvParty, tvPosition;

        public CandidateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvParty = itemView.findViewById(R.id.tvParty);
            tvPosition = itemView.findViewById(R.id.tvPosition);
        }
    }
}

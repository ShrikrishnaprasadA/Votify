package com.example.collegeelectionsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private final Context context;
    private final List<Announcement> announcementList;

    public AnnouncementAdapter(Context context, List<Announcement> announcementList) {
        this.context = context;
        this.announcementList = announcementList;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement a = announcementList.get(position);
        holder.tvTitle.setText(a.getTitle());
        holder.tvType.setText("Type: " + a.getType());
        holder.tvContent.setText(a.getContent());

        String when = "--";
        if (a.getTimestamp() != null) {
            when = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(a.getTimestamp().toDate());
        }
        holder.tvTimestamp.setText(when);
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvType, tvContent, tvTimestamp;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}

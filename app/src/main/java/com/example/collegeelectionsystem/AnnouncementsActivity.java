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

public class AnnouncementsActivity extends AppCompatActivity {

    private AnnouncementAdapter adapter;
    private List<Announcement> announcementList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        RecyclerView recyclerAnnouncements = findViewById(R.id.recyclerAnnouncements);
        recyclerAnnouncements.setLayoutManager(new LinearLayoutManager(this));

        announcementList = new ArrayList<>();
        adapter = new AnnouncementAdapter(this, announcementList);
        recyclerAnnouncements.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadAnnouncements();
    }

    private void loadAnnouncements() {
        db.collection("announcements")
                .orderBy("timestamp") // if timestamp exists
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        announcementList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Announcement ann = new Announcement();
                            ann.setTitle(doc.getString("title"));
                            ann.setType(doc.getString("type"));
                            ann.setContent(doc.getString("content"));
                            // null-safe timestamp
                            if (doc.getTimestamp("timestamp") != null) {
                                ann.setTimestamp(doc.getTimestamp("timestamp"));
                            }
                            announcementList.add(ann);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error loading announcements", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

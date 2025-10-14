package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddAnnouncementActivity extends AppCompatActivity {

    private EditText etTitle, etType, etContent;
    private Button btnSave;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_announcement);

        etTitle = findViewById(R.id.etAnnouncementTitle);
        etType = findViewById(R.id.etAnnouncementType);
        etContent = findViewById(R.id.etAnnouncementContent);
        btnSave = findViewById(R.id.btnSaveAnnouncement);

        db = FirebaseFirestore.getInstance();

        btnSave.setOnClickListener(v -> saveAnnouncement());
    }

    private void saveAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String type = etType.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() || type.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> announcement = new HashMap<>();
        announcement.put("title", title);
        announcement.put("type", type);
        announcement.put("content", content);
        announcement.put("timestamp", Timestamp.now());

        db.collection("announcements").add(announcement)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Announcement added successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

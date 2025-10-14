package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddPartyActivity extends AppCompatActivity {

    private EditText etName, etColor, etDescription;
    private Button btnSave;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_party);

        etName = findViewById(R.id.etPartyName);
        etColor = findViewById(R.id.etPartyColor);
        etDescription = findViewById(R.id.etPartyDescription);
        btnSave = findViewById(R.id.btnSaveParty);

        db = FirebaseFirestore.getInstance();

        btnSave.setOnClickListener(v -> saveParty());
    }

    private void saveParty() {
        String name = etName.getText().toString().trim();
        String color = etColor.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || color.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> party = new HashMap<>();
        party.put("name", name);
        party.put("color", color);
        party.put("description", description);

        db.collection("parties").add(party)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Party added successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

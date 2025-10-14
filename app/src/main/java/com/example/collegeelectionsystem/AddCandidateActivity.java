package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCandidateActivity extends AppCompatActivity {

    private EditText etName, etEmail, etParty, etPosition, etYear, etDepartment, etAgenda;
    private Button btnSave;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_candidate);

        etName = findViewById(R.id.etCandidateName);
        etEmail = findViewById(R.id.etCandidateEmail);
        etParty = findViewById(R.id.etCandidateParty);
        etPosition = findViewById(R.id.etCandidatePosition);
        etYear = findViewById(R.id.etCandidateYear);
        etDepartment = findViewById(R.id.etCandidateDepartment);
        etAgenda = findViewById(R.id.etCandidateAgenda);
        btnSave = findViewById(R.id.btnSaveCandidate);

        db = FirebaseFirestore.getInstance();

        btnSave.setOnClickListener(v -> saveCandidate());
    }

    private void saveCandidate() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String party = etParty.getText().toString().trim();
        String position = etPosition.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String agenda = etAgenda.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || party.isEmpty() || position.isEmpty()
                || year.isEmpty() || department.isEmpty() || agenda.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> candidate = new HashMap<>();
        candidate.put("name", name);
        candidate.put("email", email);
        candidate.put("party", party);
        candidate.put("position", position);
        candidate.put("year", year);
        candidate.put("department", department);
        candidate.put("agenda", agenda);

        db.collection("candidate").add(candidate)
                .addOnSuccessListener(documentReference ->{
                        Toast.makeText(this, "Candidate added successfully!", Toast.LENGTH_SHORT).show();
                        etName.setText("");
                        etEmail.setText("");
                        etParty.setText("");
                        etPosition.setText("");
                        etYear.setText("");
                        etDepartment.setText("");
                        etAgenda.setText("");
                        btnSave.setText("");

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

}

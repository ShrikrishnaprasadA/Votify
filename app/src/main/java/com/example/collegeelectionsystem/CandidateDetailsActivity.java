package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CandidateDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_details);

        TextView tvName = findViewById(R.id.tvName);
        TextView tvParty = findViewById(R.id.tvParty);
        TextView tvPosition = findViewById(R.id.tvPosition);
        TextView tvYear = findViewById(R.id.tvYear);
        TextView tvDepartment = findViewById(R.id.tvDepartment);
        TextView tvAgenda = findViewById(R.id.tvAgenda);

        // Get data from intent
        tvName.setText(getIntent().getStringExtra("name"));
        tvParty.setText("Party: " + getIntent().getStringExtra("party"));
        tvPosition.setText("Position: " + getIntent().getStringExtra("position"));
        tvYear.setText("Year: " + getIntent().getStringExtra("year"));
        tvDepartment.setText("Department: " + getIntent().getStringExtra("department"));
        tvAgenda.setText("Agenda: " + getIntent().getStringExtra("agenda"));
    }
}

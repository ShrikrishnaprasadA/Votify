package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StudentRegisterStep2Activity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPhone, etDepartment, etYear, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private String studentId; // passed from Step 1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register_step2);

        // Get Student ID from Step 1
        studentId = getIntent().getStringExtra("studentId");

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDepartment = findViewById(R.id.etDepartment);
        etYear = findViewById(R.id.etYear);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnCompleteRegistration = findViewById(R.id.btnCompleteRegistration);
        btnCompleteRegistration.setOnClickListener(v -> registerUser());
        TextView tvbacktologin=findViewById(R.id.tvBackToLoginStep2);
        tvbacktologin.setOnClickListener(v->{
            startActivity(new Intent(StudentRegisterStep2Activity.this,StudentLoginActivity.class));
        });
    }

    private void registerUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || department.isEmpty() || year.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@learner.manipal.edu")) {
            Toast.makeText(this, "Email must end with @learner.manipal.edu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Create FirebaseAuth User
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                assert mAuth.getCurrentUser() != null;
                String uid = mAuth.getCurrentUser().getUid();

                // Prepare user data
                Map<String, Object> user = new HashMap<>();
                user.put("studentId", studentId);
                user.put("firstName", firstName);
                user.put("lastName", lastName);
                user.put("email", email);
                user.put("phone", phone);
                user.put("department", department);
                user.put("year", year);
                user.put("role", "student");

                // Save to Firestore
                db.collection("users").document(uid).set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(StudentRegisterStep2Activity.this, DashboardActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

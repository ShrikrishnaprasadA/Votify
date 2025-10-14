package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminEmail, etAdminPassword;
    private Button btnAdminLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnAdminLogin.setOnClickListener(v -> loginAdmin());
    }

    private void loginAdmin() {
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = mAuth.getCurrentUser().getUid();

                    db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String role = doc.getString("role");
                            if ("admin".equals(role)) {
                                Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, AdminDashboardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Not authorized as admin", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

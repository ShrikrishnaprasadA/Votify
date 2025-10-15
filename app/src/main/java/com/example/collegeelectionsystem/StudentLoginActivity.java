package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class StudentLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // UI
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView btnBack = findViewById(R.id.btnBack);
        TextView anonymous,admin;
        anonymous=findViewById(R.id.btnAnonymousLogin);
        admin=findViewById(R.id.btnAdminLogin);

        anonymous.setOnClickListener(v->{
            startActivity(new Intent(StudentLoginActivity.this, AnonymousLoginActivity.class));
        });
        admin.setOnClickListener(v->{
            startActivity(new Intent(StudentLoginActivity.this, AdminLoginActivity.class));
        });
        btnLogin.setOnClickListener(v -> loginUser());

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(StudentLoginActivity.this, StudentRegisterStep1Activity.class));
            finish();
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@learner.manipal.edu")) {
            Toast.makeText(this, "Email must end with @learner.manipal.edu", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Firebase Login
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                            .addOnCompleteListener(work -> {
                                if (work.isSuccessful()) {
                                    System.out.println("✅ Subscribed to announcements");
                                }
                            });

                    FirebaseMessaging.getInstance().subscribeToTopic("results")
                            .addOnCompleteListener(work -> {
                                if (work.isSuccessful()) {
                                    System.out.println("✅ Subscribed to results");
                                }
                            });

                    startActivity(new Intent(StudentLoginActivity.this, DashboardActivity.class));
                    finish();
                }
            } else {
                Toast.makeText(this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

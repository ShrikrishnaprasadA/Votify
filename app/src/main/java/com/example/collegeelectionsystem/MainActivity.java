package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // UI buttons
        Button btnStudentLogin = findViewById(R.id.btnStudentLogin);
        Button btnStudentRegister = findViewById(R.id.btnStudentRegister);
        Button btnAdminLogin = findViewById(R.id.btnAdminLogin);
        Button btnAnonymousLogin = findViewById(R.id.btnAnonymousLogin);

        // Navigation
        btnStudentLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StudentLoginActivity.class));
        });

        btnStudentRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StudentRegisterStep1Activity.class));
        });

        btnAdminLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AdminLoginActivity.class));
        });

        btnAnonymousLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AnonymousLoginActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Session check: if already logged in, go to Dashboard
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
        }
    }
}

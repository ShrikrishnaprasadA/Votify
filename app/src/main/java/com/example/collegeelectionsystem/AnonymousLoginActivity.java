package com.example.collegeelectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

public class AnonymousLoginActivity extends AppCompatActivity {

    private EditText etToken;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_login);

        etToken = findViewById(R.id.etToken);
        Button btnLogin = findViewById(R.id.btnAnonymousLogin);
        TextView admin,student;
        student=findViewById(R.id.btnloginActivity);
        admin=findViewById(R.id.btnAdminLogin);
        db = FirebaseFirestore.getInstance();
        student.setOnClickListener(v->{
            Intent intent =new Intent(AnonymousLoginActivity.this,StudentLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        admin.setOnClickListener(v->{
            Intent intent =new Intent(AnonymousLoginActivity.this, AdminLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        btnLogin.setOnClickListener(v -> validateToken());
    }

    private void validateToken() {
        String tokenInput = etToken.getText().toString().trim();
        if (tokenInput.isEmpty()) {
            Toast.makeText(this, "Please enter a token", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("tokens")
                .whereEqualTo("token", tokenInput)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Boolean used = doc.getBoolean("used");
                            if (used != null && !used) {
                                // ✅ Valid token → mark as used
                                db.collection("tokens").document(doc.getId())
                                        .update("used", true)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Anonymous login successful!", Toast.LENGTH_SHORT).show();
                                            FirebaseMessaging.getInstance().subscribeToTopic("announcements");
                                            FirebaseMessaging.getInstance().subscribeToTopic("results");

                                            // Navigate to Anonymous Dashboard
                                            Intent i = new Intent(this, AnonymousDashboardActivity.class);
                                            startActivity(i);
                                            finish();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Error updating token", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(this, "Token already used", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Invalid token", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

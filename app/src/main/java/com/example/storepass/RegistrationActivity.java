package com.example.storepass;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextNewUsername, editTextNewPassword;
    private Button buttonRegisterUser;
    private MyDatabaseHelper dbHelper; // Add this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        dbHelper = new MyDatabaseHelper(this); // Initialize database helper

        editTextNewUsername = findViewById(R.id.editTextNewUsername);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonRegisterUser = findViewById(R.id.buttonRegisterUser);

        buttonRegisterUser.setOnClickListener(view -> {
            String username = editTextNewUsername.getText().toString();
            String password = editTextNewPassword.getText().toString();

            dbHelper.addUser(username, password);
            Toast.makeText(RegistrationActivity.this, "User Registered", Toast.LENGTH_SHORT).show();
        });
    }
}

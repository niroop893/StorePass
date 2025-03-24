package com.example.storepass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.storepass.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Button buttonLogin;
    private Button buttonRegister;
    private Button buttonViewData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        // Initialize buttons
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonViewData = findViewById(R.id.buttonViewData);

        // Set onClick listeners for the buttons
        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }

        if (buttonRegister != null) {
            buttonRegister.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            });
        }

        if (buttonViewData != null) {
            buttonViewData.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, TabularActivity.class);
                startActivity(intent);
            });
        }
    }
}
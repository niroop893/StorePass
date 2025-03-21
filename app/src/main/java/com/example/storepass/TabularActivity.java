package com.example.storepass;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class TabularActivity extends AppCompatActivity {

    private ListView listVendor, listUsername, listPassword;
    private List<String> vendorList = new ArrayList<>();
    private List<String> userList = new ArrayList<>();
    private List<String> passwordList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabular);

        listVendor = findViewById(R.id.listVendor);
        listUsername = findViewById(R.id.listUsername);
        listPassword = findViewById(R.id.listPassword);

        // Sample data
        vendorList.add("Vendor A");
        vendorList.add("Vendor B");
        vendorList.add("Vendor C");

        userList.add("User1");
        userList.add("User2");
        userList.add("User3");

        passwordList.add("123456");
        passwordList.add("abcdef");
        passwordList.add("password");

        // Set adapters
        listVendor.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, vendorList));
        listUsername.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList));
        listPassword.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, passwordList));
    }
}

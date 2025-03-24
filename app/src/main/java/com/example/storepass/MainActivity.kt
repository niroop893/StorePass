package com.example.storepass

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storepass.data.Credential
import com.example.storepass.data.DatabaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var credentialsList: RecyclerView
    private lateinit var adapter: CredentialAdapter
    private var userId: Long = -1
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get user ID from intent
        userId = intent.getLongExtra("USER_ID", -1)
        username = intent.getStringExtra("USERNAME") ?: ""

        if (userId == -1L) {
            finish()
            return
        }

        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up RecyclerView
        credentialsList = findViewById(R.id.credentialsRecyclerView)
        credentialsList.layoutManager = LinearLayoutManager(this)
        adapter = CredentialAdapter(emptyList()) { credential ->
            showCredentialDetails(credential)
        }
        credentialsList.adapter = adapter

        // Set up FAB for adding new credentials
        val addButton = findViewById<FloatingActionButton>(R.id.addCredentialFab)
        addButton.setOnClickListener {
            showAddCredentialDialog()
        }

        // Load credentials
        loadCredentials()
    }

    private fun loadCredentials() {
        try {
            val credentials = dbHelper.getCredentials(userId)
            adapter.updateCredentials(credentials)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading credentials: ${e.message}", e)
            Toast.makeText(this, "Error loading credentials. Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAddCredentialDialog() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_credential, null)
            val serviceInput = dialogView.findViewById<EditText>(R.id.serviceInput)
            val usernameInput = dialogView.findViewById<EditText>(R.id.usernameInput)
            val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)

            AlertDialog.Builder(this)
                .setTitle("Add New Credential")
                .setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val service = serviceInput.text.toString()
                    val username = usernameInput.text.toString()
                    val password = passwordInput.text.toString()

                    if (service.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                        try {
                            val id = dbHelper.addCredential(userId, service, username, password)
                            if (id != -1L) {
                                Toast.makeText(this, "Credential added successfully", Toast.LENGTH_SHORT).show()
                                loadCredentials()
                            } else {
                                Toast.makeText(this, "Failed to add credential", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Error adding credential: ${e.message}", e)
                            Toast.makeText(this, "Error adding credential. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error showing add credential dialog: ${e.message}", e)
            Toast.makeText(this, "Error showing dialog. Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showCredentialDetails(credential: Credential) {
        try {
            AlertDialog.Builder(this)
                .setTitle(credential.service)
                .setMessage("""
                    Username: ${credential.username}
                    Password: ${credential.password}
                """.trimIndent())
                .setPositiveButton("OK", null)
                .setNeutralButton("Edit") { _, _ ->
                    showEditCredentialDialog(credential)
                }
                .show()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error showing credential details: ${e.message}", e)
            Toast.makeText(this, "Error showing details. Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showEditCredentialDialog(credential: Credential) {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_credential, null)
            val serviceInput = dialogView.findViewById<EditText>(R.id.serviceInput)
            val usernameInput = dialogView.findViewById<EditText>(R.id.usernameInput)
            val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)

            // Pre-fill with existing values
            serviceInput.setText(credential.service)
            usernameInput.setText(credential.username)
            passwordInput.setText(credential.password)

            AlertDialog.Builder(this)
                .setTitle("Edit Credential")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val service = serviceInput.text.toString()
                    val username = usernameInput.text.toString()
                    val password = passwordInput.text.toString()

                    if (service.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                        try {
                            val success = dbHelper.updateCredential(credential.id, service, username, password)
                            if (success) {
                                Toast.makeText(this, "Credential updated successfully", Toast.LENGTH_SHORT).show()
                                loadCredentials()
                            } else {
                                Toast.makeText(this, "Failed to update credential. Please try again.", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Error updating credential: ${e.message}", e)
                            Toast.makeText(this, "Error updating credential. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error showing edit credential dialog: ${e.message}", e)
            Toast.makeText(this, "Error showing dialog. Please try again.", Toast.LENGTH_LONG).show()
        }
    }
}

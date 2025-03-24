package com.example.storepass.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USERS_TABLE)
        db.execSQL(CREATE_CREDENTIALS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CREDENTIALS")
        onCreate(db)
    }

    fun addCredential(userId: Long, service: String, username: String, password: String): Long {
        val db = this.writableDatabase
        val encryptedPassword = encrypt(password)
        
        val values = ContentValues().apply {
            put(COLUMN_USER_ID_FK, userId)
            put(COLUMN_SERVICE, service)
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, encryptedPassword)
        }
        
        return db.insert(TABLE_CREDENTIALS, null, values)
    }

    fun getCredentials(userId: Long): List<Credential> {
        val credentials = mutableListOf<Credential>()
        val db = this.readableDatabase
        
        val cursor = db.query(
            TABLE_CREDENTIALS,
            null,
            "$COLUMN_USER_ID_FK = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREDENTIAL_ID))
            val service = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVICE))
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val encryptedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val decryptedPassword = decrypt(encryptedPassword)
            
            credentials.add(Credential(id, service, username, decryptedPassword))
        }
        cursor.close()
        return credentials
    }

    fun updateCredential(credentialId: Long, service: String, username: String, password: String): Boolean {
        val db = this.writableDatabase
        val encryptedPassword = encrypt(password)
        
        val values = ContentValues().apply {
            put(COLUMN_SERVICE, service)
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, encryptedPassword)
        }
        
        return db.update(
            TABLE_CREDENTIALS,
            values,
            "$COLUMN_CREDENTIAL_ID = ?",
            arrayOf(credentialId.toString())
        ) > 0
    }

    fun deleteCredential(credentialId: Long): Boolean {
        val db = this.writableDatabase
        return db.delete(
            TABLE_CREDENTIALS,
            "$COLUMN_CREDENTIAL_ID = ?",
            arrayOf(credentialId.toString())
        ) > 0
    }

    fun authenticateUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )
        val result = cursor.count > 0
        cursor.close()
        return result
    }

    fun getUserId(username: String): Long {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        
        var userId = -1L
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
        }
        cursor.close()
        return userId
    }

    fun registerUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        
        return db.insert(TABLE_USERS, null, values)
    }

    private fun encrypt(text: String): String {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(text.toByteArray(Charset.forName("UTF-8")))
            
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error encrypting password: ${e.message}", e)
            throw e
        }
    }

    private fun decrypt(encryptedText: String): String {
        try {
            val encryptedData = Base64.decode(encryptedText, Base64.DEFAULT)
            
            val ivSize = 12
            val iv = ByteArray(ivSize)
            System.arraycopy(encryptedData, 0, iv, 0, ivSize)
            
            val encryptedBytes = ByteArray(encryptedData.size - ivSize)
            System.arraycopy(encryptedData, ivSize, encryptedBytes, 0, encryptedBytes.size)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val ivParameterSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivParameterSpec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error decrypting password: ${e.message}", e)
            throw e
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            )
            return keyGenerator.generateKey()
        }
        
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    companion object {
        const val DATABASE_NAME = "StorePass.db"
        const val DATABASE_VERSION = 1
        
        const val TABLE_USERS = "users"
        const val TABLE_CREDENTIALS = "credentials"
        
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
        
        const val COLUMN_CREDENTIAL_ID = "credential_id"
        const val COLUMN_USER_ID_FK = "user_id"
        const val COLUMN_SERVICE = "service"
        
        private const val KEY_ALIAS = "StorePassSecretKey"
        
        private const val CREATE_USERS_TABLE = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """
        
        private const val CREATE_CREDENTIALS_TABLE = """
            CREATE TABLE $TABLE_CREDENTIALS (
                $COLUMN_CREDENTIAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID_FK INTEGER NOT NULL,
                $COLUMN_SERVICE TEXT NOT NULL,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                FOREIGN KEY($COLUMN_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """
    }
}

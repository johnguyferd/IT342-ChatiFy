package com.it342.chatifying // Make sure this matches exactly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() { // Renamed to match common conventions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Make sure this matches your XML filename

        val usernameInput = findViewById<EditText>(R.id.loginUsername)
        val passwordInput = findViewById<EditText>(R.id.loginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignupRedirect = findViewById<Button>(R.id.signupRedirect)

        btnLogin.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            // Login logic here

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnSignupRedirect.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java) // Changed to SignupActivity if that's the class name
            startActivity(intent)
        }
    }
}
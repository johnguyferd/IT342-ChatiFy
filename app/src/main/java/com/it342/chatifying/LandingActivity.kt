package com.it342.chatifying
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class LandingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val loginBtn = findViewById<Button>(R.id.landingLogin)
        val signupBtn = findViewById<Button>(R.id.landingSignup)

        loginBtn.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}

package com.example.nautilusapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val login = findViewById<Button>(R.id.loginButton)
        login.setOnClickListener(View.OnClickListener{
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        })

        val signup = findViewById<Button>(R.id.signUpButton)
        signup.setOnClickListener(View.OnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        })
    }
}
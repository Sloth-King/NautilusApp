package com.example.nautilusapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object Common{
    var connected: Boolean = false
    var me: String = ""
    val servorIpAdress = "10.34.3.127"
    val port = 5052
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun openNemoLog(view: View) {
        openFragment(LogFragment())
    }

    fun openMessages(view: View) {
        openFragment(MessagePreviewFragment())
    }

    fun openIdentification(view: View) {
        Toast.makeText(this, "id page not ready" , Toast.LENGTH_SHORT).show()
        //openFragment(IdentificationFragment())
    }

    fun openProfile(view: View) {
        Toast.makeText(this, "Profile page not ready" , Toast.LENGTH_SHORT).show()
        //openFragment(ProfileFragment())
    }
}
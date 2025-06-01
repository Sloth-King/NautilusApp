package com.example.nautilusapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import kotlin.text.Charsets.US_ASCII

object Common{
    var connected: Boolean = false
    var me: String = ""
    val servorIpAdress = "10.34.3.127"
    val port = 5050

    fun padding(msg: String,n: Int): String {
        if(n > 0){
            var res = "0"+msg
            return padding(res,n-1)
        }
        else{
            return msg
        }
    }

    fun requestIdUsers(users: String): String{
        val socket = Socket(servorIpAdress, port)
        val writer: OutputStream = socket.getOutputStream()
        val reader: InputStream = socket.getInputStream()

        var msg = (me).toByteArray(US_ASCII)
        var size = msg.size.toString()
        var padding = padding(size, 3 - size.length)
        var finalSize: ByteArray = padding.toByteArray((US_ASCII))
        writer.write(finalSize)
        writer.write(msg)

        writer.write(("2").toByteArray(US_ASCII))

        msg = (users).toByteArray(US_ASCII)
        size = msg.size.toString()
        padding = padding(size, 8 - size.length)
        finalSize = padding.toByteArray((US_ASCII))
        writer.write(finalSize)
        writer.write(msg)

        val sizeAnswer = ByteArray(8)
        reader.read(sizeAnswer,0,8)
        val answer = ByteArray(sizeAnswer.toString(US_ASCII).toInt())
        reader.read(answer,0,sizeAnswer.toString(US_ASCII).toInt())
        Log.d("MessagePreview",answer.toString(US_ASCII))
        return answer.toString(US_ASCII)
    }
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
        
        openFragment(LogFragment())
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
        //Toast.makeText(this, "id page not ready" , Toast.LENGTH_SHORT).show()
        openFragment(IdentificationFragment())
    }

    fun openProfile(view: View) {
        //Toast.makeText(this, "Profile page not ready" , Toast.LENGTH_SHORT).show()
        openFragment(ProfileFragment())
    }
}